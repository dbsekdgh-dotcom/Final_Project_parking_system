package com.example.demo.domain.auth.user.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException; // ⭐ 추가
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Slf4j
@Component
public class OAuth2FailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {

        log.error("### [소셜 로그인 실패] 원인: {}", exception.getMessage());

        String errorType = "error";

        // ⭐ 수정 포인트: OAuth2AuthenticationException인 경우 에러 코드를 직접 확인
        if (exception instanceof OAuth2AuthenticationException oAuth2Exception) {
            String errorCode = oAuth2Exception.getError().getErrorCode();
            log.info("### [실패 핸들러] 감지된 에러 코드: {}", errorCode);

            if ("email_mismatch".equals(errorCode)) {
                errorType = "email_mismatch";
            } else if ("email_not_found".equals(errorCode)) {
                errorType = "email_not_found";
            }
        }

        // 리액트로 이동 (에러 코드를 정확히 실어서 보냄)
        String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:5202/oauth-redirect")
                .queryParam("error", errorType)
                .build().toUriString();

        log.info("### [실패 핸들러] 리액트로 이동: {}", targetUrl);
        response.sendRedirect(targetUrl);
    }
}