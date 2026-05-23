package com.example.news;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.example.news")
@EnableScheduling
public class NewsApplication {

    public static void main(String[] args) {
        SpringApplication.run(NewsApplication.class, args);
    }
}
