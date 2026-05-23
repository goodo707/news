package com.example.news.rss.scheduler;

import com.example.news.rss.service.RssCollectorService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "news.rss.scheduler.enabled", havingValue = "true")
@RequiredArgsConstructor
public class RssScheduler {

    private final RssCollectorService rssCollectorService;

    @Scheduled(fixedRate = 600_000)
    public void collectRss() {
        rssCollectorService.collectAll();
    }
}
