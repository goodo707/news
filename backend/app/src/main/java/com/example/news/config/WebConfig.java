package com.example.news.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 글로벌 CORS 설정. Next.js dev 서버 등 다른 origin 에서의 API 호출을 허용한다.
 *
 * <p>허용 origin 은 {@code news.cors.allowed-origins} 프로퍼티로 외부화하여
 * 환경별(dev/staging/prod)로 코드 변경 없이 변경 가능.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${news.cors.allowed-origins}")
    private String[] allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOrigins(allowedOrigins)
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true);
    }
}
