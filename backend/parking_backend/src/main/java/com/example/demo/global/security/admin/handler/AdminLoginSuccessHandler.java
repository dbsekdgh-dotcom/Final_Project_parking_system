package com.example.demo.global.security.admin.handler;

import com.example.demo.domain.admin.auth.dtos.response.AdminLoginResponse;
import com.example.demo.domain.admin.repository.AdminRepository;
import com.example.demo.global.security.admin.AdminAuthDto;
import com.example.demo.global.util.admin.AdminJWTUtil;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Log4j2
@Transactional
public class AdminLoginSuccessHandler implements AuthenticationSuccessHandler {
    private final AdminRepository adminRepository;
    private final AdminJWTUtil adminJWTUtil;
    private final Gson gson;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication)throws IOException, ServletException {
        log.info("----------- [Admin Login Success Handler] -----------");

        //1. 인증 객체 꺼내기
        AdminAuthDto adminAuthDto = (AdminAuthDto) authentication.getPrincipal();
        //2. DB의 lsatLoginAt 업데이트
        adminRepository.findByLoginId(adminAuthDto.getUsername()).ifPresent(admin -> {
            admin.setLastLoginAt(LocalDateTime.now());
            adminRepository.save(admin);
            log.info("관리자 ["+admin.getLoginId()+"] 마지막 로그인 시간 업데이트 완료");
        });
        //3. 토큰 생성 (분단위 설정)
        Map<String,Object> claims = adminAuthDto.getClaims();
        String accessToken=adminJWTUtil.generateToken(claims,30); //30분
        String refreshToken=adminJWTUtil.generateToken(claims,60*24); //24시간
        // Refresh Token을 위한 HttpOnly 쿠키 생성
        // jakarta.servlet.http.Cookie 대신 Spring의 ResponseCookie를 쓰면 설정이 더 편합니다.
        String cookieString = org.springframework.http.ResponseCookie.from("refreshToken",refreshToken)
                .httpOnly(true) // JS 접근 차단(XSS방어)
                .secure(false) // 로컬 테스트(http) 중이면 false, 배포시 true
                .path("/") // 모든 경로에서 사용가능
                .maxAge(24 * 60 * 60) // 쿠키 수명(24시간)
                .sameSite("Lax") // CSRF 방어
                .build()
                .toString();
        // 응답 헤더에 쿠키 추가
        response.addHeader(HttpHeaders.SET_COOKIE, cookieString);

        //4. responseDto에 담기
        AdminLoginResponse loginResponse = AdminLoginResponse.builder()
                .accessToken(accessToken)
                .loginId(adminAuthDto.getUsername()) //loginId
                .adminName((String)claims.get("name"))
                .build();
        //5. JSON 변환 및 응답 전송
        response.setContentType("application/json; charset=UTF-8");
        PrintWriter pw = response.getWriter();
        //DTO 객체를 JSON 문자열로 변환
        String jsonStr=gson.toJson(loginResponse);
        pw.println(jsonStr);
        pw.close();
    }
}
