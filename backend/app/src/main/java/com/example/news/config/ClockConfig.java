package com.example.news.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

/**
 * 시간 의존성을 {@link Clock} 빈으로 추상화한다.
 *
 * <p>서비스/컨트롤러는 {@code LocalDateTime.now()} 대신 {@code LocalDateTime.now(clock)} 으로
 * 호출하여 테스트에서 시간을 고정({@link Clock#fixed})할 수 있도록 한다.
 * 모든 시간은 Asia/Seoul 기준으로 통일한다.
 */
@Configuration
public class ClockConfig {

    @Bean
    public Clock clock() {
        return Clock.system(ZoneId.of("Asia/Seoul"));
    }
}
