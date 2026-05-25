package com.example.news.rss.service;

import com.example.news.core.domain.Article;
import com.example.news.core.domain.Category;
import com.example.news.core.repository.ArticleRepository;
import com.example.news.core.repository.CategoryRepository;
import com.example.news.push.dispatcher.PushDispatchService;
import com.example.news.rss.domain.RssCategory;
import com.example.news.rss.parser.ArticleDraft;
import com.example.news.rss.parser.RssParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RssCollectorService {

    private static final long MAX_ARTICLES = 1000;

    private static final DateTimeFormatter ISO_LOCAL = DateTimeFormatter
        .ofPattern("yyyy-MM-dd'T'HH:mm:ss")
        .withZone(ZoneId.of("Asia/Seoul"));

    private final RssParser rssParser;
    private final ArticleRepository articleRepository;
    private final CategoryRepository categoryRepository;
    private final PushDispatchService pushDispatchService;
    private final Clock clock;

    public void collectAll() {
        int totalNew = 0;
        for (RssCategory cat : RssCategory.values()) {
            try {
                totalNew += collectOne(cat);
            } catch (Exception e) {
                log.error("[rss] {} 수집 실패", cat.getCategoryName(), e);
            }
        }
        log.info("[rss] 전체 수집 완료: 신규 {}건", totalNew);
    }

    @Transactional
    public void cleanupOldArticles() {
        long total = articleRepository.count();
        if (total <= MAX_ARTICLES) return;

        int toDelete = (int) (total - MAX_ARTICLES);
        int deleted = articleRepository.deleteOldestN(toDelete);
        log.info("[rss] cleanup: {}건 삭제 (전 {} → 후 {})",
            deleted, total, MAX_ARTICLES);
    }

    public int collectOne(RssCategory cat) {
        Optional<Category> categoryOpt = categoryRepository.findByName(cat.getCategoryName());
        if (categoryOpt.isEmpty()) {
            log.error("[rss] category 미존재: {}", cat.getCategoryName());
            return 0;
        }
        Long categoryId = categoryOpt.get().getId();

        List<ArticleDraft> drafts = rssParser.parse(cat.getFeedUrl());
        int newCount = 0;
        String now = ISO_LOCAL.format(ZonedDateTime.now(clock));

        List<String> draftIds = drafts.stream()
            .map(ArticleDraft::articleId)
            .filter(Objects::nonNull)
            .toList();
        Set<String> existing = draftIds.isEmpty()
            ? Set.of()
            : articleRepository.findAllById(draftIds).stream()
                .map(Article::getArticleId)
                .collect(Collectors.toSet());

        for (ArticleDraft d : drafts) {
            if (d.articleId() == null || existing.contains(d.articleId())) continue;
            Article saved = articleRepository.save(new Article(
                d.articleId(),
                d.title(),
                d.link(),
                d.author(),
                categoryId,
                d.pubDate(),
                now
            ));
            try {
                pushDispatchService.dispatch(saved, cat.getCategoryName());
            } catch (Exception e) {
                log.error("[push] 발송 실패: {}", saved.getArticleId(), e);
            }
            newCount++;
        }

        log.info("[rss] {} 수집 완료: 신규 {}건 / 응답 {}건",
            cat.getCategoryName(), newCount, drafts.size());
        return newCount;
    }
}
