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
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 체인 적용 범위 설정
                .securityMatcher("/api/user/**", "/login/**", "/oauth2/**", "/", "/oauth-redirect/**")

                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(userCorsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .logout(logout -> logout.disable())
                // JWT 필터 순서 조정: LogoutFilter 앞으로 당겨서 OAuth2 로직보다 먼저 쿠키를 읽게 함
                .addFilterBefore(new JwtAuthenticationFilter(adminJWTUtil), org.springframework.security.web.authentication.logout.LogoutFilter.class)
                // 일반 API 요청을 위해 기존 위치에도 유지
                .addFilterBefore(new JwtAuthenticationFilter(adminJWTUtil), UsernamePasswordAuthenticationFilter.class)

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/", "/login/**", "/oauth2/**", "/oauth-redirect/**", "/api/user/auth/refresh",
                                "/api/user/auth/local/signup", "/api/user/auth/local/check-email", "/api/user/auth/local/login",
                                "/api/user/auth/local/find-email","/api/user/auth/local/send-code", "/api/user/auth/local/verify-code",
                                "/api/user/auth/local/find-email", "/api/user/auth/local/reset-password", "/api/user/auth/local/send-recover-code",
                                "/api/user/auth/local/verify-recover-code","/api/user/auth/local/recover", "/api/user/auth/social-recover").permitAll()
                        .requestMatchers("/api/user/auth/local/logout").authenticated()
                        .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/user/auth/local/withdraw").authenticated()
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

        // 리액트 앱 주소 허용
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5202"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}