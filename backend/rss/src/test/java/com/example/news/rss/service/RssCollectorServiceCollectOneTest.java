package com.example.news.rss.service;

import com.example.news.core.domain.Article;
import com.example.news.core.domain.Category;
import com.example.news.core.repository.ArticleRepository;
import com.example.news.core.repository.CategoryRepository;
import com.example.news.push.dispatcher.PushDispatchService;
import com.example.news.rss.domain.RssCategory;
import com.example.news.rss.parser.ArticleDraft;
import com.example.news.rss.parser.RssParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RssCollectorServiceCollectOneTest {

    private static final Long POLITICS_CATEGORY_ID = 1L;

    @Mock RssParser rssParser;
    @Mock ArticleRepository articleRepository;
    @Mock CategoryRepository categoryRepository;
    @Mock PushDispatchService pushDispatchService;

    private final Clock clock = Clock.fixed(
        Instant.parse("2026-05-25T12:00:00Z"),
        ZoneId.of("Asia/Seoul")
    );
    private RssCollectorService rssCollectorService;

    @BeforeEach
    void setUp() {
        rssCollectorService = new RssCollectorService(
            rssParser, articleRepository, categoryRepository, pushDispatchService, clock
        );
    }

    private Category politicsCategory() {
        Category c = new Category("정치");
        // 리플렉션 없이 ID 주입 어려움 — 실제로는 generated, 다만 mock 반환에 사용
        return c;
    }

    private ArticleDraft draft(String id) {
        return new ArticleDraft(id, "title-" + id, "https://www.yna.co.kr/view/" + id,
            "author", "2026-05-25T12:00:00");
    }

    private Article savedArticle(String id) {
        return new Article(id, "title-" + id, "https://www.yna.co.kr/view/" + id,
            "author", POLITICS_CATEGORY_ID, "2026-05-25T12:00:00", "2026-05-25T12:00:00");
    }

    @Test
    void category_가_존재하지_않으면_0_반환하고_parse_호출_안_됨() {
        given(categoryRepository.findByName("정치")).willReturn(Optional.empty());

        int newCount = rssCollectorService.collectOne(RssCategory.POLITICS);

        assertThat(newCount).isZero();
        verify(rssParser, never()).parse(anyString());
        verify(articleRepository, never()).save(any());
    }

    @Test
    void parse_결과가_비면_save_와_dispatch_호출_안_됨() {
        Category c = politicsCategory();
        given(categoryRepository.findByName("정치")).willReturn(Optional.of(c));
        given(rssParser.parse(RssCategory.POLITICS.getFeedUrl())).willReturn(List.of());

        int newCount = rssCollectorService.collectOne(RssCategory.POLITICS);

        assertThat(newCount).isZero();
        verify(articleRepository, never()).save(any());
        verify(pushDispatchService, never()).dispatch(any(), anyString());
    }

    @Test
    void 모두_신규_draft_면_전부_save_와_dispatch() {
        Category c = politicsCategory();
        given(categoryRepository.findByName("정치")).willReturn(Optional.of(c));
        given(rssParser.parse(RssCategory.POLITICS.getFeedUrl())).willReturn(List.of(
            draft("AKR-1"), draft("AKR-2"), draft("AKR-3")
        ));
        // 기존 저장된 article 없음
        given(articleRepository.findAllById(List.of("AKR-1", "AKR-2", "AKR-3")))
            .willReturn(List.of());
        given(articleRepository.save(any(Article.class)))
            .willAnswer(inv -> inv.getArgument(0));

        int newCount = rssCollectorService.collectOne(RssCategory.POLITICS);

        assertThat(newCount).isEqualTo(3);
        verify(articleRepository, times(3)).save(any(Article.class));
        verify(pushDispatchService, times(3)).dispatch(any(Article.class), eqStr("정치"));
    }

    @Test
    void 이미_존재하는_articleId_는_skip_되고_신규만_save() {
        Category c = politicsCategory();
        given(categoryRepository.findByName("정치")).willReturn(Optional.of(c));
        given(rssParser.parse(RssCategory.POLITICS.getFeedUrl())).willReturn(List.of(
            draft("AKR-1"), draft("AKR-2"), draft("AKR-3")
        ));
        // AKR-1, AKR-3 는 이미 DB 에 있음 (AKR-2 만 신규)
        given(articleRepository.findAllById(List.of("AKR-1", "AKR-2", "AKR-3")))
            .willReturn(List.of(savedArticle("AKR-1"), savedArticle("AKR-3")));
        given(articleRepository.save(any(Article.class)))
            .willAnswer(inv -> inv.getArgument(0));

        int newCount = rssCollectorService.collectOne(RssCategory.POLITICS);

        assertThat(newCount).isEqualTo(1);
        verify(articleRepository, times(1)).save(any(Article.class));
        verify(pushDispatchService, times(1)).dispatch(any(Article.class), eqStr("정치"));
    }

    @Test
    void dispatch_가_예외를_던져도_다음_draft_저장은_계속됨_부분실패격리() {
        Category c = politicsCategory();
        given(categoryRepository.findByName("정치")).willReturn(Optional.of(c));
        given(rssParser.parse(RssCategory.POLITICS.getFeedUrl())).willReturn(List.of(
            draft("AKR-1"), draft("AKR-2")
        ));
        given(articleRepository.findAllById(List.of("AKR-1", "AKR-2")))
            .willReturn(List.of());
        given(articleRepository.save(any(Article.class)))
            .willAnswer(inv -> inv.getArgument(0));
        // 첫 dispatch 에서 예외 → 다음 draft 처리 영향 없어야
        org.mockito.Mockito.doThrow(new RuntimeException("push 서버 장애"))
            .doNothing()
            .when(pushDispatchService).dispatch(any(Article.class), anyString());

        int newCount = rssCollectorService.collectOne(RssCategory.POLITICS);

        assertThat(newCount).isEqualTo(2);
        verify(articleRepository, times(2)).save(any(Article.class));
        verify(pushDispatchService, times(2)).dispatch(any(Article.class), anyString());
    }

    // Mockito 의 eq() 헬퍼 String 버전 (가독성)
    private static String eqStr(String s) {
        return org.mockito.ArgumentMatchers.eq(s);
    }
}
