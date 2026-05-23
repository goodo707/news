package com.yonhap.news.rss.init;

import com.yonhap.news.rss.service.RssCollectorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Order(2)
@RequiredArgsConstructor
public class RssInitialFetchRunner implements ApplicationRunner {

    private final RssCollectorService rssCollectorService;

    @Override
    public void run(ApplicationArguments args) {
        log.info("[rss] 초기 수집 시작");
        rssCollectorService.collectAll();
    }
}
