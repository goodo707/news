package com.yonhap.news.rss.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RssCategory {
    POLITICS("정치", "https://www.yna.co.kr/rss/politics.xml"),
    NORTH_KOREA("북한", "https://www.yna.co.kr/rss/northkorea.xml"),
    ECONOMY("경제", "https://www.yna.co.kr/rss/economy.xml"),
    INDUSTRY("산업", "https://www.yna.co.kr/rss/industry.xml"),
    SOCIETY("사회", "https://www.yna.co.kr/rss/society.xml");

    private final String categoryName;
    private final String feedUrl;
}
