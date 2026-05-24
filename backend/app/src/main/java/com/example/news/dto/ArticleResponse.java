package com.example.news.dto;

public record ArticleResponse(
    String articleId,
    String title,
    String link,
    String author,
    String pubDate,
    boolean isRead
) {
}
