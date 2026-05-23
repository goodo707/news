package com.yonhap.news.rss.parser;

public record ArticleDraft(
    String articleId,
    String title,
    String link,
    String author,
    String pubDate
) {}
