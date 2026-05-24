package com.example.news.core.repository;

import com.example.news.core.domain.ArticleRead;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleReadRepository extends JpaRepository<ArticleRead, String> {
}
