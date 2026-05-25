package com.example.news.core.repository;

import com.example.news.core.domain.Article;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ArticleRepository extends JpaRepository<Article, String> {

    List<Article> findByCategoryIdOrderByPubDateDesc(Long categoryId);

    /**
     * pub_date 오래된 순으로 일부만 조회. cleanup 시 사용 (Pageable 로 LIMIT 처리).
     */
    List<Article> findAllByOrderByPubDateAsc(Pageable pageable);
}
