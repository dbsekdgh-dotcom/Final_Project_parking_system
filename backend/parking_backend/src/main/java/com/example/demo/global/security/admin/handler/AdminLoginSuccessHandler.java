package com.example.demo.global.security.admin.handler;

import com.example.demo.domain.auth.admin.dtos.response.AdminLoginResponse;
import com.example.demo.domain.auth.admin.repository.AdminRepository;
import com.example.demo.global.redis.RedisService;
import com.example.demo.global.security.admin.AdminAuthDto;
import com.example.demo.global.util.admin.AdminJWTUtil;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${cookie.secure:false}")
    private boolean cookieSecure;

    private final AdminRepository adminRepository;
    private final AdminJWTUtil adminJWTUtil;
    private final RedisService redisService;
    private final Gson gson;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication)throws IOException, ServletException {
        log.info("----------- [Admin Login Success Handler] -----------");

        // 인증 객체 꺼내기
        AdminAuthDto adminAuthDto = (AdminAuthDto) authentication.getPrincipal();
        String loginId = adminAuthDto.getUsername();

        // DB의 lsatLoginAt 업데이트
        adminRepository.findByLoginId(adminAuthDto.getUsername()).ifPresent(admin -> {
            admin.setLastLoginAt(LocalDateTime.now());
            adminRepository.save(admin);
            log.info("관리자 ["+admin.getLoginId()+"] 마지막 로그인 시간 업데이트 완료");
        });
        // 토큰 생성 (분단위 설정)
        Map<String,Object> claims = adminAuthDto.getClaims();
        String accessToken=adminJWTUtil.generateToken(claims,30); //30분
        String refreshToken=adminJWTUtil.generateToken(claims,60*24); //24시간
        redisService.saveRefreshToken(loginId,refreshToken,60*24); //Redis에 Refresh Token 저장
        log.info(">>>>>>>>>> Redis템플릿에 [{}]의 Refresh Token 기록 완료",loginId);
        // Refresh Token을 위한 HttpOnly 쿠키 생성
        // jakarta.servlet.http.Cookie 대신 Spring의 ResponseCookie를 쓰면 설정이 더 편함.
        String cookieString = org.springframework.http.ResponseCookie.from("refreshToken",refreshToken)
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(24 * 60 * 60)
                .sameSite("Lax")
                .build()
                .toString();
        response.addHeader(HttpHeaders.SET_COOKIE, cookieString); // 응답 헤더에 쿠키 추가

        // responseDto에 담기
        AdminLoginResponse loginResponse = AdminLoginResponse.builder()
                .accessToken(accessToken)
                .loginId(adminAuthDto.getUsername()) //loginId
                .adminName((String)claims.get("name"))
                .build();
        // JSON 변환 및 응답 전송
        response.setContentType("application/json; charset=UTF-8");
        PrintWriter pw = response.getWriter();
        //DTO 객체를 JSON 문자열로 변환
        String jsonStr=gson.toJson(loginResponse);
        pw.println(jsonStr);
        pw.close();
    }
}
