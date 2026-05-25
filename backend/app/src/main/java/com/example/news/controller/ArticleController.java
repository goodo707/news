package com.example.news.controller;

import com.example.news.core.domain.Article;
import com.example.news.core.domain.ArticleRead;
import com.example.news.core.repository.ArticleReadRepository;
import com.example.news.core.repository.ArticleRepository;
import com.example.news.core.repository.CategoryRepository;
import com.example.news.core.util.TimeFormats;
import com.example.news.dto.ArticleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleRepository articleRepository;
    private final ArticleReadRepository articleReadRepository;
    private final CategoryRepository categoryRepository;
    private final Clock clock;

    @GetMapping
    @Transactional(readOnly = true)
    public List<ArticleResponse> list(@RequestParam String category) {
        Long categoryId = categoryRepository.findByName(category)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "카테고리를 찾을 수 없습니다: " + category))
            .getId();

        List<Article> articles = articleRepository.findByCategoryIdOrderByPubDateDesc(categoryId);
        List<String> articleIds = articles.stream().map(Article::getArticleId).toList();

        // N+1 회피: 기사별로 isRead 조회 대신 한 번에 read 상태를 가져와 메모리 Set 으로 매칭.
        // JOIN 대신 두 쿼리로 분리한 이유 — Article 은 외부(RSS) 데이터, read 는 사용자 행동 데이터로 lifecycle 분리.
        Set<String> readIds = articleReadRepository.findAllById(articleIds).stream()
            .map(ArticleRead::getArticleId)
            .collect(Collectors.toSet());

        return articles.stream()
            .map(a -> new ArticleResponse(
                a.getArticleId(), a.getTitle(), a.getLink(), a.getAuthor(),
                a.getPubDate(), readIds.contains(a.getArticleId())
            ))
            .toList();
    }

    @PostMapping("/{articleId}/read")
    @Transactional
    public void markRead(@PathVariable String articleId) {
        // 존재하지 않는 article 에 대한 read 기록 차단 — 1000건 cleanup 으로 삭제된 ID 또는 잘못된 ID 방어
        if (!articleRepository.existsById(articleId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "기사를 찾을 수 없습니다: " + articleId);
        }
        String now = LocalDateTime.now(clock).format(TimeFormats.ISO_LOCAL_DATE_TIME);
        articleReadRepository.save(new ArticleRead(articleId, now));
    }
}
