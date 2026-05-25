package com.example.news.rss.service;

import com.example.news.core.domain.Article;
import com.example.news.core.repository.ArticleRepository;
import com.example.news.core.repository.CategoryRepository;
import com.example.news.push.dispatcher.PushDispatchService;
import com.example.news.rss.parser.RssParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.time.Clock;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RssCollectorServiceCleanupTest {

    private static final long MAX_ARTICLES = 1000;

    @Mock RssParser rssParser;
    @Mock ArticleRepository articleRepository;
    @Mock CategoryRepository categoryRepository;
    @Mock PushDispatchService pushDispatchService;
    @Mock Clock clock;
    @InjectMocks RssCollectorService rssCollectorService;

    private Article articleWithId(String id) {
        return new Article(
            id, "title", "https://www.yna.co.kr/view/" + id,
            "author", 1L, "2026-05-25T12:00:00", "2026-05-25T12:00:00"
        );
    }

    @Test
    void 빈_테이블이면_삭제_호출_없음() {
        given(articleRepository.count()).willReturn(0L);

        rssCollectorService.cleanupOldArticles();

        verify(articleRepository, never()).deleteAllInBatch(any());
    }

    @Test
    void 정확히_MAX_이면_삭제_호출_없음() {
        given(articleRepository.count()).willReturn(MAX_ARTICLES);

        rssCollectorService.cleanupOldArticles();

        verify(articleRepository, never()).deleteAllInBatch(any());
    }

    @Test
    void MAX_초과시_초과분_만큼_pub_date_오래된_순_삭제() {
        long total = MAX_ARTICLES + 50;
        given(articleRepository.count()).willReturn(total);

        List<Article> oldest = IntStream.range(0, 50)
            .mapToObj(i -> articleWithId("AKR-old-" + i))
            .toList();
        given(articleRepository.findAllByOrderByPubDateAsc(PageRequest.of(0, 50)))
            .willReturn(oldest);

        rssCollectorService.cleanupOldArticles();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Article>> captor = ArgumentCaptor.forClass(List.class);
        verify(articleRepository).deleteAllInBatch(captor.capture());
        assertThat(captor.getValue())
            .hasSize(50)
            .extracting(Article::getArticleId)
            .startsWith("AKR-old-0", "AKR-old-1");
    }
}
