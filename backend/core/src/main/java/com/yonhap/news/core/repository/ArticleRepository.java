package com.yonhap.news.core.repository;

import com.yonhap.news.core.domain.Article;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleRepository extends JpaRepository<Article, String> {
}
