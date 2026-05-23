package com.yonhap.news.rss.scheduler;

import com.yonhap.news.rss.service.RssCollectorService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RssScheduler {

    private final RssCollectorService rssCollectorService;

    @Scheduled(fixedRate = 600_000)
    public void collectRss() {
        rssCollectorService.collect();
    }
}
