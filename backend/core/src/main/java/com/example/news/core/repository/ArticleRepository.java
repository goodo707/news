package com.example.news.core.repository;

import com.example.news.core.domain.Article;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ArticleRepository extends JpaRepository<Article, String> {

    List<Article> findAllByOrderByPubDateAsc(Pageable pageable);

    List<Article> findByCategoryIdOrderByPubDateDesc(Long categoryId);
}
