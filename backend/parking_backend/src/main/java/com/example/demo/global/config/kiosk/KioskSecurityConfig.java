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

import org.springframework.beans.factory.annotation.Value;

@Configuration
@Order(3)
@RequiredArgsConstructor
public class KioskSecurityConfig {

    @Value("${frontend.admin.url}")
    private String adminUrl;
    @Value("${frontend.user.url}")
    private String userUrl;
    @Value("${frontend.kiosk.url}")
    private String kioskUrl;

    private final AdminJWTUtil adminJWTUtil;

    @Bean
    public SecurityFilterChain kioskFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/api/v1/**", "/api/payment/**", "/api/exit/**", "/api/store/**","/api/kiosk/**")
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(kioskCorsConfigurationSource()))
            .sessionManagement(session ->
            {
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
                session.sessionFixation().none();
            })
                .securityContext(ctx->
                        ctx.securityContextRepository(new org.springframework.security.web.context.NullSecurityContextRepository()))
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(HttpMethod.POST, "/api/store/login").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/store/test-hint").permitAll()
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
        configuration.setAllowedOriginPatterns(List.of(
                adminUrl,
                userUrl,
                kioskUrl,
                "https://admin.parking-system.store",
                "https://user.parking-system.store",
                "https://kiosk.parking-system.store"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
