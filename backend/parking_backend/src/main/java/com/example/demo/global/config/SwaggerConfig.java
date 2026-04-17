package com.example.demo.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Comparator;
import java.util.stream.Collectors;

@Configuration
public class SwaggerConfig {


    // ✅ 태그를 숫자/알파벳 순으로 정렬해주는 커스텀 빈
    @Bean
    public OpenApiCustomizer sortTagsAlphabetically() {
        return openApi -> {
            if (openApi.getTags() != null) {
                openApi.setTags(openApi.getTags().stream()
                        .sorted(Comparator.comparing(io.swagger.v3.oas.models.tags.Tag::getName))
                        .collect(Collectors.toList()));
            }
        };
    }

    @Bean
    public OpenAPI userOpenAPI() {
        String jwtSchemeName = "jwtAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("사용자 전용 주차 시스템 API")
                        .description("인증이 필요한 API는 우측 상단 자물쇠를 이용하세요.")
                        .version("1.0.0"))
                .components(new Components()
                        .addSecuritySchemes(jwtSchemeName, new SecurityScheme()
                                .name(jwtSchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }

    @Bean
    public GroupedOpenApi userGroupApi(OpenApiCustomizer sortTagsAlphabetically) {
        return GroupedOpenApi.builder()
                .group("1. 사용자용 (User)")
                .pathsToMatch("/api/user/**")
                // ✅ 이 그룹에 정렬 로직 적용
                .addOpenApiCustomizer(sortTagsAlphabetically)
                .build();
    }

}