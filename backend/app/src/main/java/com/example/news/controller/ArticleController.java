package com.example.news.controller;

import com.example.news.core.domain.Article;
import com.example.news.core.domain.ArticleRead;
import com.example.news.core.repository.ArticleReadRepository;
import com.example.news.core.repository.ArticleRepository;
import com.example.news.core.repository.CategoryRepository;
import com.example.news.dto.ArticleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/articles")
@RequiredArgsConstructor
public class ArticleController {

    private static final DateTimeFormatter FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final ArticleRepository articleRepository;
    private final ArticleReadRepository articleReadRepository;
    private final CategoryRepository categoryRepository;
    private final Clock clock;

    @GetMapping
    public List<ArticleResponse> list(@RequestParam String category) {
        Long categoryId = categoryRepository.findByName(category)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "category not found: " + category))
            .getId();

        List<Article> articles = articleRepository.findByCategoryIdOrderByPubDateDesc(categoryId);
        List<String> articleIds = articles.stream().map(Article::getArticleId).toList();

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
    public void markRead(@PathVariable String articleId) {
        if (!articleRepository.existsById(articleId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "article not found: " + articleId);
        }
        String now = LocalDateTime.now(clock).format(FORMATTER);
        articleReadRepository.save(new ArticleRead(articleId, now));
    }
}
