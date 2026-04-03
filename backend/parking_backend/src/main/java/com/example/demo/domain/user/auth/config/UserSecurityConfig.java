package com.example.demo.domain.user.auth.config;

import com.example.demo.domain.user.auth.filter.JWTCheckFilter;
import com.example.demo.domain.user.auth.handler.OAuth2SuccessHandler;
import com.example.demo.domain.user.auth.jwt.JWTUtil;
import com.example.demo.domain.user.auth.service.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final JWTUtil jwtUtil;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // ⭐ 핵심: 이 필터 체인이 작동할 범위를 제한합니다.
                // 관리자 API를 제외한 사용자 관련 API 경로를 모두 적어주세요.
                .securityMatcher("/api/user/**", "/login/**", "/oauth2/**", "/", "/oauth-redirect/**")

                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(userCorsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(new JWTCheckFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/", "/login/**", "/oauth2/**", "/oauth-redirect/**", "/api/user/auth/refresh",
                                "/api/user/auth/local/signup", "/api/user/auth/local/login").permitAll()
                        .anyRequest().authenticated())
                .oauth2Login(oauth -> oauth
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .successHandler(oAuth2SuccessHandler)
                );

        return http.build();
    }

    // 3. AdminSecurityConfig와 충돌을 피하기 위해 메서드 이름을 변경함
    @Bean
    public CorsConfigurationSource userCorsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 리액트 앱 주소 허용
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173"));
        // 모든 HTTP 메서드 허용
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        // 모든 헤더 허용 (Authorization 등 포함)
        configuration.setAllowedHeaders(Arrays.asList("*"));
        // 쿠키나 인증 정보를 포함한 요청 허용
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }


}