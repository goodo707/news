package com.yonhap.news.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI yonhapOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("연합뉴스 사전과제 API")
                .description("뉴스 열람 + 푸시 시스템 API")
                .version("v1.0"));
    }
}
