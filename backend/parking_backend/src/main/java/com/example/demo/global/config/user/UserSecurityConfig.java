package com.example.demo.global.config.user;

import com.example.demo.domain.user.auth.handler.OAuth2FailureHandler;
import com.example.demo.domain.user.auth.handler.OAuth2SuccessHandler;
import com.example.demo.domain.user.auth.service.CustomOAuth2UserService;
import com.example.demo.global.security.admin.JwtAuthenticationFilter;
import com.example.demo.global.util.admin.AdminJWTUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
@Order(2) // 기존 유지
public class UserSecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final OAuth2FailureHandler oAuth2FailureHandler;
    private final AdminJWTUtil adminJWTUtil;

    @Bean
    public SecurityFilterChain userSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                // [경로 매칭] 컨트롤러의 /api/v1/reports/** 경로와 정확히 일치함
                .securityMatcher(
                        "/",
                        "/login/**",
                        "/oauth2/**",
                        "/oauth-redirect/**",
                        "/api/v1/reports/**",
                        "/api/user/**"
                )
                // 1. CORS 설정을 가장 먼저 적용
                .cors(cors -> cors.configurationSource(userCorsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 2. JWT 필터 등록 (로그 확인용 System.out이 포함된 버전 권장)
                .addFilterBefore(new JwtAuthenticationFilter(adminJWTUtil), UsernamePasswordAuthenticationFilter.class)

                .authorizeHttpRequests(auth -> auth
                        // 브라우저의 사전 요청(OPTIONS)은 무조건 허용
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 로그인 및 인증 관련 경로는 모두 허용
                        .requestMatchers("/", "/login/**", "/oauth2/**", "/oauth-redirect/**").permitAll()
                        .requestMatchers("/api/user/auth/local/**", "/api/user/auth/refresh", "/api/user/auth/social-recover").permitAll()

                        // [중요] 리포트 API는 이제 토큰이 반드시 필요함 (authenticated)
                        // JwtAuthenticationFilter의 shouldNotFilter에서도 이 경로가 빠져있어야 함
                        .requestMatchers("/api/v1/reports/**").authenticated()

                        // 나머지 유저 관련 요청도 인증 필요
                        .anyRequest().authenticated())

                .oauth2Login(oauth -> oauth
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .successHandler(oAuth2SuccessHandler)
                        .failureHandler(oAuth2FailureHandler)
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource userCorsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // [CORS 설정] 리액트 앱 주소
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5202"));

        // [CORS 설정] PATCH 포함 모든 메서드 허용
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

        // [핵심 수정] 모든 헤더 허용 (Preflight 에러 방지의 핵심)
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // 클라이언트가 응답에서 Authorization 헤더를 읽을 수 있게 허용
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Set-Cookie"));

        // 인증정보(쿠키/토큰) 포함 허용
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // securityMatcher에 등록된 모든 경로에 대해 이 CORS 설정 적용
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}