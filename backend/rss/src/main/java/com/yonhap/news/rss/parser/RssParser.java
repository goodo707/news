package com.yonhap.news.rss.parser;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URLConnection;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class RssParser {

    private static final int CONNECT_TIMEOUT_MS = 5_000;
    private static final int READ_TIMEOUT_MS = 10_000;
    private static final DateTimeFormatter ISO_LOCAL = DateTimeFormatter
        .ofPattern("yyyy-MM-dd'T'HH:mm:ss")
        .withZone(ZoneId.of("Asia/Seoul"));

    public List<ArticleDraft> parse(String feedUrl) {
        try {
            URLConnection conn = URI.create(feedUrl).toURL().openConnection();
            conn.setConnectTimeout(CONNECT_TIMEOUT_MS);
            conn.setReadTimeout(READ_TIMEOUT_MS);

            SyndFeed feed;
            try (XmlReader reader = new XmlReader(conn)) {
                feed = new SyndFeedInput().build(reader);
            }

            List<ArticleDraft> drafts = new ArrayList<>();
            for (SyndEntry entry : feed.getEntries()) {
                toDraft(entry).ifPresent(drafts::add);
            }
            return drafts;
        } catch (Exception e) {
            log.error("[rss] feed 파싱 실패: {}", feedUrl, e);
            return List.of();
        }
    }

    private Optional<ArticleDraft> toDraft(SyndEntry entry) {
        String link = entry.getLink();
        Optional<String> articleId = extractArticleId(link);
        if (articleId.isEmpty()) {
            log.warn("[rss] item skip (articleId 추출 실패): {}", link);
            return Optional.empty();
        }

        String pubDate = entry.getPublishedDate() != null
            ? ISO_LOCAL.format(entry.getPublishedDate().toInstant())
            : ISO_LOCAL.format(Instant.now());

        return Optional.of(new ArticleDraft(
            articleId.get(),
            entry.getTitle(),
            link,
            entry.getAuthor(),
            pubDate
        ));
    }

    static Optional<String> extractArticleId(String link) {
        if (link == null || link.isBlank()) return Optional.empty();
        int viewIdx = link.indexOf("/view/");
        if (viewIdx < 0) return Optional.empty();
        String tail = link.substring(viewIdx + "/view/".length());
        int qIdx = tail.indexOf('?');
        if (qIdx >= 0) tail = tail.substring(0, qIdx);
        int slashIdx = tail.indexOf('/');
        if (slashIdx >= 0) tail = tail.substring(0, slashIdx);
        return tail.isBlank() ? Optional.empty() : Optional.of(tail);
    }
}
