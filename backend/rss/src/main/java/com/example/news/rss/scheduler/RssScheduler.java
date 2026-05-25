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

    // fixedDelay 사용 — 이전 cycle 종료 후 interval 만큼 대기.
    // RSS 외부 호출이 10분을 넘기는 비정상 케이스에도 누락분 catch-up 시도가 없어 외부 서버에 정중함.
    // 또한 미래에 scheduling pool size 가 늘어나도 중복 실행이 발생하지 않음.
    @Scheduled(
        fixedDelayString = "${news.rss.scheduler.interval-ms:600000}",
        initialDelayString = "${news.rss.scheduler.initial-delay-ms:5000}"
    )
    public void collectRss() {
        rssCollectorService.collectAll();
        rssCollectorService.cleanupOldArticles();
    }
}
