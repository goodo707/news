package com.example.news.rss.service;

import com.example.news.core.domain.Article;
import com.example.news.core.domain.Category;
import com.example.news.core.repository.ArticleRepository;
import com.example.news.core.repository.CategoryRepository;
import com.example.news.rss.domain.RssCategory;
import com.example.news.rss.parser.ArticleDraft;
import com.example.news.rss.parser.RssParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

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

        cleanupOldArticles();
    }

    @Transactional
    public void cleanupOldArticles() {
        long total = articleRepository.count();
        if (total <= MAX_ARTICLES) return;

        int toDelete = (int) (total - MAX_ARTICLES);
        List<Article> oldest = articleRepository
            .findAllByOrderByPubDateAsc(PageRequest.of(0, toDelete));
        articleRepository.deleteAllInBatch(oldest);
        log.info("[rss] cleanup: {}건 삭제 (전 {} → 후 {})",
            oldest.size(), total, MAX_ARTICLES);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int collectOne(RssCategory cat) {
        Optional<Category> categoryOpt = categoryRepository.findByName(cat.getCategoryName());
        if (categoryOpt.isEmpty()) {
            log.error("[rss] category 미존재: {}", cat.getCategoryName());
            return 0;
        }
        Long categoryId = categoryOpt.get().getId();

        List<ArticleDraft> drafts = rssParser.parse(cat.getFeedUrl());
        int newCount = 0;
        String now = ISO_LOCAL.format(ZonedDateTime.now());

        for (ArticleDraft d : drafts) {
            if (d.articleId() == null) continue;
            if (articleRepository.existsById(d.articleId())) continue;
            articleRepository.save(new Article(
                d.articleId(),
                d.title(),
                d.link(),
                d.author(),
                categoryId,
                d.pubDate(),
                now
            ));
            newCount++;
        }

        log.info("[rss] {} 수집 완료: 신규 {}건 / 응답 {}건",
            cat.getCategoryName(), newCount, drafts.size());
        return newCount;
    }
}
