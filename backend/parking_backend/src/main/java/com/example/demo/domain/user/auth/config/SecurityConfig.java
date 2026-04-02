package com.example.demo.domain.user.auth.config;


import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

//    private final Cus
    //여기부터 추가함
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

    http

            .securityMatcher("/mypage/**")   //윤진추가내용(삭제예정


            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/mypage/**").permitAll()  // 👈 이거 핵심
                    .anyRequest().authenticated()
            );

    return http.build();
    }
//여기까지 삭제예정
}
