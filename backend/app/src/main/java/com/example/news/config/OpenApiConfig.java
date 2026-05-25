package com.example.news.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger UI 상단에 표시될 API 메타데이터(제목/설명/버전).
 * springdoc-openapi 가 컨트롤러를 자동 스캔해 만드는 스펙 위에 이 정보를 덧붙인다.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI newsOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("뉴스 열람 및 푸시 API")
                .description("뉴스 기사 열람 + 푸시 알림 시스템 API")
                .version("v1.0"));
    }
}
