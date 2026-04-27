package com.example.demo.global.config.user;

import com.example.demo.domain.auth.user.handler.OAuth2FailureHandler;
import com.example.demo.domain.auth.user.handler.OAuth2SuccessHandler;
import com.example.demo.domain.auth.user.service.CustomOAuth2UserService;
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
                .securityMatcher("/api/user/**", "/api/report/**",
                        "/api/notifications/**","/api/dashboard/**", "/login/**", "/oauth2/**", "/", "/oauth-redirect/**")

                // 1. CSRF 설정: 쿠키 방식을 쓸 때는 CSRF 공격에 취약할 수 있으므로 나중에 방어 로직이 필요할 수 있습니다.
                // 현재는 개발 편의를 위해 disable 유지합니다.
                .csrf(csrf -> csrf.disable())

                // 2. CORS 설정 적용 (하단 Bean 참조)
                .cors(cors -> cors.configurationSource(userCorsConfigurationSource()))

                // 3. 세션 정책: JWT를 쓰므로 세션을 생성하지 않음 (STATELESS)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .logout(logout -> logout.disable())

                /**
                 * 4. JWT 필터 설정
                 * 이제 JwtAuthenticationFilter 내부 로직은 Header가 아니라 Cookie를 검사하도록 수정되어야 합니다.
                 * 필터 배치는 기존과 동일하게 UsernamePasswordAuthenticationFilter 이전에 둡니다.
                 */
                .addFilterBefore(new JwtAuthenticationFilter(adminJWTUtil),
                        UsernamePasswordAuthenticationFilter.class)

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 인증 없이 접근 가능한 경로
                        .requestMatchers("/", "/login/**", "/oauth2/**", "/oauth-redirect/**", "/api/user/auth/refresh",
                                "/api/user/auth/local/signup", "/api/user/auth/local/check-email", "/api/user/auth/local/login",
                                "/api/user/auth/local/find-email", "/api/user/auth/local/send-code", "/api/user/auth/local/verify-code",
                                "/api/user/auth/local/reset-password", "/api/user/auth/local/send-recover-code",
                                "/api/user/auth/local/verify-recover-code", "/api/user/auth/local/recover",
                                "/api/user/auth/social-recover", "/api/user/ai/naver/**","/api/user/space/**").permitAll()

                        .requestMatchers("/api/notifications/**").authenticated()
                        .requestMatchers("/api/dashboard/**").authenticated()
                        .requestMatchers("/api/user/apply/**").authenticated()
                        .requestMatchers("/api/report/**").authenticated()
                        .requestMatchers("/api/user/reservations/**").authenticated()
                        .requestMatchers("/api/user/subscriptions/**").authenticated()
                        .requestMatchers("/api/user/auth/local/logout").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/user/auth/local/withdraw").authenticated()

                        .anyRequest().authenticated()
                )

                /**
                 * 5. OAuth2 로그인 설정
                 * SuccessHandler 내에서 액세스 토큰을 'HttpOnly 쿠키'로 구워주는 로직이 들어가야 합니다.
                 */
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

        // [중요] 쿠키 통신을 위해 프론트엔드 도메인을 명확히 명시 (와일드카드 * 사용 불가)
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5202"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

        // [중요] 모든 헤더를 허용하되, 인증 관련 헤더를 브라우저가 신뢰할 수 있도록 설정
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // [중요] 브라우저가 서버로부터 받은 Set-Cookie 헤더를 읽고 저장할 수 있도록 노출
        configuration.setExposedHeaders(Arrays.asList("Set-Cookie", "Authorization"));

        // [핵심] 쿠키 전송 허용 (withCredentials: true 대응)
        configuration.setAllowCredentials(true);

        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}