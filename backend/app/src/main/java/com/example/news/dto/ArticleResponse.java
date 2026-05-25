package com.example.news.dto;

import jakarta.validation.constraints.NotNull;

public record ArticleResponse(
    @NotNull String articleId,
    @NotNull String title,
    @NotNull String link,
    String author,
    @NotNull String pubDate,
    boolean isRead
) {
}
