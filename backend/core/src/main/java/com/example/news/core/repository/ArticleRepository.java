package com.example.news.core.repository;

import com.example.news.core.domain.Article;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleRepository extends JpaRepository<Article, String> {
}
