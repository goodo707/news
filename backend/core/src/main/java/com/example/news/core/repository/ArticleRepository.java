package com.example.news.core.repository;

import com.example.news.core.domain.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ArticleRepository extends JpaRepository<Article, String> {

    List<Article> findByCategoryIdOrderByPubDateDesc(Long categoryId);

    @Modifying
    @Query(value = "DELETE FROM article WHERE article_id IN " +
                   "(SELECT article_id FROM article ORDER BY pub_date ASC LIMIT :n)",
           nativeQuery = true)
    int deleteOldestN(@Param("n") int n);
}
