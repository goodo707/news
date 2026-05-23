package com.example.news.core.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "article_read")
@Getter
@NoArgsConstructor
public class ArticleRead {

    @Id
    @Column(name = "article_id")
    private String articleId;

    @Column(name = "read_at", nullable = false)
    private String readAt;

    public ArticleRead(String articleId, String readAt) {
        this.articleId = articleId;
        this.readAt = readAt;
    }
}
