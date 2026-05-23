package com.example.news.core.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "article")
@Getter
@NoArgsConstructor
public class Article {

    @Id
    @Column(name = "article_id")
    private String articleId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String link;

    private String author;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @Column(name = "pub_date", nullable = false)
    private String pubDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private String createdAt;

    public Article(String articleId, String title, String link, String author,
                   Long categoryId, String pubDate, String createdAt) {
        this.articleId = articleId;
        this.title = title;
        this.link = link;
        this.author = author;
        this.categoryId = categoryId;
        this.pubDate = pubDate;
        this.createdAt = createdAt;
    }
}
