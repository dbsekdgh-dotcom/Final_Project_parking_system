package com.example.demo.global.config.admin;

import com.example.demo.global.security.admin.AdminUserDetailService;
import com.example.demo.global.security.admin.JwtAuthenticationFilter;
import com.example.demo.global.security.admin.handler.AdminAccessDeniedHandler;
import com.example.demo.global.security.admin.handler.AdminLoginFailureHandler;
import com.example.demo.global.security.admin.handler.AdminLoginSuccessHandler;
import com.example.demo.global.util.admin.AdminJWTUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // 일반적으로 설정해둠
@RequiredArgsConstructor
@Log4j2
@Order(1)
public class
AdminSecurityConfig {
    private final AdminUserDetailService adminUserDetailService;
    private final AdminJWTUtil adminJWTUtil;
    private final AdminAccessDeniedHandler adminAccessDeniedHandler;
    private final AdminLoginSuccessHandler adminLoginSuccessHandler;
    private final AdminLoginFailureHandler adminLoginFailureHandler;

    @Bean
    public SecurityFilterChain adminFilterChain(HttpSecurity http) throws Exception{
        log.info("----------- [Admin Security Configuration Loading] -----------");

        http.securityMatcher("/admin/**");

        // cors 설정 (리액트와 통신을 위해, 가장 선순위로 설정해줘야함)
        http.cors(cors->cors.configurationSource(corsConfigurationSource()));

        // 세션 및 csrf 비활성화
        http.csrf(csrf->csrf.disable());
        http.sessionManagement(sessionConfig ->{
            sessionConfig.sessionCreationPolicy(SessionCreationPolicy.STATELESS); //세션 생성하지 않기
        });

        // 권한 설정 (인가)
        http.authorizeHttpRequests(auth -> auth
                // 1. 강제 출차 API만 누구나 접근 가능하게 맨 위에 추가 (임시)
//                .requestMatchers(HttpMethod.POST, "/admin/parking/logs/*/force-exit").permitAll()

                // 최상단에 로그아웃을 가장 먼저 배치
                .requestMatchers(HttpMethod.POST,"/admin/logout").permitAll()
                .requestMatchers("/admin/login","/admin/refresh").permitAll() // 로그인 경로는 누구나 접근 가능

                // 테스트하기위해 잠시 추가
//                .requestMatchers("/admin/parking/summary").permitAll()

                .requestMatchers("/admin/**").hasRole("ADMIN") // 나머지 관리자 APT는 권한 필요
                .anyRequest().permitAll()
        );

        // 로그인 설정 (핸들러 연결)
        http.formLogin(form->form
                .loginProcessingUrl("/admin/login") // 리액트에서 보낼 로그인 엔드포인트
                .usernameParameter("loginId")
                .passwordParameter("password")
                .successHandler(adminLoginSuccessHandler)
                .failureHandler(adminLoginFailureHandler)
        );

        //로그아웃 설정
        http.logout(logout->logout
                .disable()
//                .logoutUrl("/admin/logout")
//                .addLogoutHandler((request, response, authentication) ->{
//                    log.info("Security Logout Handler 동작");
//                })
//                .logoutSuccessHandler((request, response, authentication) -> {
//                    //성공 시 다른 페이지로 리다이렉트 하지않고 200 OK만 응답
//                    response.setStatus(HttpServletResponse.SC_OK);
//                })
        );

        // 이 필터체인은 무조건 관리자 서비스만 쓰라는 의미 (UserUserSecurityConfig와 겹칠때를 대비)
        http.userDetailsService(adminUserDetailService);
        // 예외 처리 (권한 부족 시) -- 추후 확장성 고려해 작성함
        http.exceptionHandling(ex->ex.accessDeniedHandler(adminAccessDeniedHandler));
        // JWT 필터 추가
        http.addFilterBefore(new JwtAuthenticationFilter(adminJWTUtil), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // 정적 자원(이미지,CSS 등)은 시큐리티 검사 제외
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer(){
        return (web)->web.ignoring()
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations());
    }

    //CrossOrigin 설정 -> 웹브라우저에서(리액트-프론트엔드) 요청이 들어와도 요청처리가 되도록 허용하기
    @Bean
    public CorsConfigurationSource corsConfigurationSource(){
        CorsConfiguration configuration = new CorsConfiguration();
        // 허용할 오리진(리액트 주소 등) 설정
        configuration.setAllowedOriginPatterns(List.of("http://localhost:5201"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization","Cache-Control","Content-Type"));
        configuration.setAllowedMethods(Arrays.asList("GET","POST","PUT","DELETE","HEAD","OPTIONS"));
        // 쿠키나 인증 정보를 포함한 요청을 허용할지 여부
        configuration.setAllowCredentials(true);
        // 브라우저가 Set-Cookie 헤더를 읽을 수 있도록 노출 설정
        configuration.addExposedHeader("Set-Cookie");
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**",configuration); // 모든 경로에 대해 위 설정 적용
        return source;
    }

}
