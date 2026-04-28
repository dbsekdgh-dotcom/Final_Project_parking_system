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


    // ✅ 태그 이름 앞 숫자를 추출해 수치 순으로 정렬 (1, 2, ..., 10, 11... 올바른 순서 보장)
    @Bean
    public OpenApiCustomizer sortTagsAlphabetically() {
        return openApi -> {
            if (openApi.getTags() != null) {
                openApi.setTags(openApi.getTags().stream()
                        .sorted(Comparator.comparingInt(tag -> {
                            try {
                                return Integer.parseInt(tag.getName().split("\\.")[0].trim());
                            } catch (NumberFormatException e) {
                                return Integer.MAX_VALUE;
                            }
                        }))
                        .collect(Collectors.toList()));
            }
        };
    }

    @Bean
    public OpenAPI userOpenAPI() {
        String jwtSchemeName = "jwtAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("주차 시스템 API")
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
    @Bean
    public GroupedOpenApi adminGroupApi(OpenApiCustomizer sortTagsAlphabetically){
        return GroupedOpenApi.builder()
                .group("2. 관리자용 (Admin)")
                .pathsToMatch("/api/admin/**")
                .addOpenApiCustomizer(sortTagsAlphabetically)
                .build();
    }

    @Bean
    public GroupedOpenApi kioskGroupApi(OpenApiCustomizer sortTagsAlphabetically){
        return GroupedOpenApi.builder()
                .group("3. 키오스크용 (Kiosk)")
                .pathsToMatch("/**") //모든 주소 포함
                .pathsToExclude("/api/user/**","/api/admin/**") //유저,관리자 주소 제외
                .addOpenApiCustomizer(sortTagsAlphabetically)
                .build();
    }

}