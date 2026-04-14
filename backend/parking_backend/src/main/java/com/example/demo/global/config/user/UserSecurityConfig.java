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
@Order(2)
public class UserSecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final OAuth2FailureHandler oAuth2FailureHandler;
    private final AdminJWTUtil adminJWTUtil;

    @Bean
    public SecurityFilterChain userSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                // 이 보안 필터 체인이 관리할 요청 경로 매처
                .securityMatcher("/api/user/**", "/api/report/**", "/login/**", "/oauth2/**", "/", "/oauth-redirect/**")

                .csrf(csrf -> csrf.disable())

                .cors(cors -> cors.configurationSource(userCorsConfigurationSource()))

                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .logout(logout -> logout.disable())

                // JWT 필터 설정: 필터 체인 순서 조정
                .addFilterBefore(new JwtAuthenticationFilter(adminJWTUtil),
                        org.springframework.security.web.authentication.logout.LogoutFilter.class)
                .addFilterBefore(new JwtAuthenticationFilter(adminJWTUtil),
                        UsernamePasswordAuthenticationFilter.class)

                .authorizeHttpRequests(auth -> auth
                        // 0. CORS 프리플라이트 요청 허용
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 1. 인증 없이 접근 가능한 경로 (로그인, 회원가입 등)
                        .requestMatchers("/", "/login/**", "/oauth2/**", "/oauth-redirect/**", "/api/user/auth/refresh",
                                "/api/user/auth/local/signup", "/api/user/auth/local/check-email", "/api/user/auth/local/login",
                                "/api/user/auth/local/find-email", "/api/user/auth/local/send-code", "/api/user/auth/local/verify-code",
                                "/api/user/auth/local/reset-password", "/api/user/auth/local/send-recover-code",
                                "/api/user/auth/local/verify-recover-code", "/api/user/auth/local/recover",
                                "/api/user/auth/social-recover").permitAll()

                        // 2. 입주민 신청 관련 경로: 인증 필요
                        .requestMatchers("/api/user/apply/**").authenticated()

                        // 3. 신고 관련 경로: 인증 필요
                        .requestMatchers("/api/report/**").authenticated()

                        // 4. 기타 회원 인증 관련 경로
                        .requestMatchers("/api/user/auth/local/logout").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/user/auth/local/withdraw").authenticated()

                        // 5. 나머지 모든 요청도 인증 필요
                        .anyRequest().authenticated()
                )

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

        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5202"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Set-Cookie"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}