package com.example.news.rss.scheduler;

import com.example.news.rss.service.RssCollectorService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 10분 주기 RSS 수집의 단일 진입점.
 *
 * <p>{@code news.rss.scheduler.enabled=false} 로 테스트 환경에서 빈 등록 자체를 차단한다.
 * {@code initialDelay=5s} — UserDataInitializer 가 카테고리 적재를 마칠 시간 +
 * 부팅 로그와 외부 호출 로그 분리용 짧은 지연.
 */
@Component
@ConditionalOnProperty(name = "news.rss.scheduler.enabled", havingValue = "true")
@RequiredArgsConstructor
public class RssScheduler {

    private final RssCollectorService rssCollectorService;

    @Scheduled(
        fixedRateString = "${news.rss.scheduler.interval-ms:600000}",
        initialDelayString = "${news.rss.scheduler.initial-delay-ms:5000}"
    )
    public void collectRss() {
        rssCollectorService.collectAll();
        rssCollectorService.cleanupOldArticles();
    }
}
