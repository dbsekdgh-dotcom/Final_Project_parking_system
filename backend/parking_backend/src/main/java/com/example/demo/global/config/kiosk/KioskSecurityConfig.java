package com.example.demo.global.config.kiosk;

import com.example.demo.global.security.store.KioskJwtFilter;
import com.example.demo.global.util.admin.AdminJWTUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@Order(3)
@RequiredArgsConstructor
public class KioskSecurityConfig {

    private final AdminJWTUtil adminJWTUtil;

    @Bean
    public SecurityFilterChain kioskFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/api/v1/**", "/api/payment/**", "/api/exit/**", "/api/store/**","/api/kiosk/**")
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(kioskCorsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(HttpMethod.POST, "/api/store/login").permitAll()
                    .requestMatchers("/api/kiosk/**").permitAll()
                    .requestMatchers("/api/store/**").hasRole("STORE")
                    .anyRequest().permitAll()
            ).addFilterBefore(new KioskJwtFilter(adminJWTUtil),
                        UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource kioskCorsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        //configuration.setAllowedOrigins(List.of("http://localhost:5203"));
        configuration.setAllowedOriginPatterns(List.of(
                "http://localhost:5201",
                "http://localhost:5202",
                "http://localhost:5203",
                "https://d2rkjg49e7w39t.cloudfront.net",   // CloudFront 배포
                "https://parking-system.shop",              // 구매한 도메인
                "https://www.parking-system.shop",          // www 서브도메인
                "https://*.cloudfront.net"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
