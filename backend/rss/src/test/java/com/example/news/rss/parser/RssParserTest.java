package com.example.news.rss.parser;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RssParserTest {

    @Test
    void 정상_URL_에서_articleId_추출() {
        assertThat(RssParser.extractArticleId("https://www.yna.co.kr/view/AKR20260525123456789"))
            .contains("AKR20260525123456789");
    }

    @Test
    void query_string_은_articleId_에서_제외() {
        assertThat(RssParser.extractArticleId("https://www.yna.co.kr/view/AKR20260525?utm_source=x"))
            .contains("AKR20260525");
    }

    @Test
    void trailing_path_는_articleId_에서_제외() {
        assertThat(RssParser.extractArticleId("https://www.yna.co.kr/view/AKR20260525/related"))
            .contains("AKR20260525");
    }

    @Test
    void null_입력_empty() {
        assertThat(RssParser.extractArticleId(null)).isEmpty();
    }

    @Test
    void 빈_문자열_empty() {
        assertThat(RssParser.extractArticleId("")).isEmpty();
    }

    @Test
    void 공백만_있으면_empty() {
        assertThat(RssParser.extractArticleId("   ")).isEmpty();
    }

    @Test
    void view_경로_미포함_empty() {
        assertThat(RssParser.extractArticleId("https://example.com/abc")).isEmpty();
    }

    @Test
    void view_뒤_빈_articleId_empty() {
        assertThat(RssParser.extractArticleId("https://www.yna.co.kr/view/")).isEmpty();
    }

    @Test
    void view_뒤_query_만_있으면_empty() {
        assertThat(RssParser.extractArticleId("https://www.yna.co.kr/view/?x=1")).isEmpty();
    }
}
