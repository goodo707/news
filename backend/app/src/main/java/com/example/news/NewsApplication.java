package com.example.news;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

// 멀티모듈 구성이라 base 패키지를 명시 — rss/push 모듈의 @Component 도 함께 스캔
@SpringBootApplication(scanBasePackages = "com.example.news")
@EnableScheduling
public class NewsApplication {

    public static void main(String[] args) {
        SpringApplication.run(NewsApplication.class, args);
    }
}
