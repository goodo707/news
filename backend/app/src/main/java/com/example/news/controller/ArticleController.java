package com.example.news.controller;

import com.example.news.core.domain.ArticleRead;
import com.example.news.core.repository.ArticleReadRepository;
import com.example.news.core.repository.ArticleRepository;
import com.example.news.core.repository.CategoryRepository;
import com.example.news.dto.ArticleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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

    @GetMapping
    public List<ArticleResponse> list(@RequestParam String category) {
        Long categoryId = categoryRepository.findByName(category)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "category not found: " + category))
            .getId();

        Set<String> readIds = articleReadRepository.findAll().stream()
            .map(ArticleRead::getArticleId)
            .collect(Collectors.toSet());
ㅊ
        return articleRepository.findByCategoryIdOrderByPubDateDesc(categoryId).stream()
            .map(a -> new ArticleResponse(
                a.getArticleId(), a.getTitle(), a.getLink(), a.getAuthor(),
                a.getPubDate(), readIds.contains(a.getArticleId())
            ))
            .toList();
    }
}
