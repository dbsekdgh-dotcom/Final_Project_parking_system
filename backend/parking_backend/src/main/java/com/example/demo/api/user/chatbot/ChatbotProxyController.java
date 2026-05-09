package com.example.demo.api.user.chatbot;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Tag(name = "14. 챗봇 (Chatbot)", description = "AI 챗봇 질의 프록시 API. 사용자의 쿠키 토큰을 AI 서버로 전달합니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/chatbot")
public class ChatbotProxyController {

    private final RestTemplate restTemplate;

    @Value("${AI_SERVER_URL}")
    private String aiServerUrl;

    @Operation(summary = "챗봇 질의", description = "사용자의 질문을 AI 서버로 전달하고 응답을 반환합니다.", security = @SecurityRequirement(name = "jwtAuth"))
    @PostMapping("/ask")
    public ResponseEntity<Map> ask(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 브라우저 쿠키(accessToken, refreshToken)를 AI 서버로 전달
        StringBuilder cookieHeader = new StringBuilder();
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                String name = cookie.getName();
                if ("accessToken".equals(name) || "refreshToken".equals(name)) {
                    if (cookieHeader.length() > 0) cookieHeader.append("; ");
                    cookieHeader.append(name).append("=").append(cookie.getValue());
                }
            }
        }
        if (cookieHeader.length() > 0) {
            headers.set("Cookie", cookieHeader.toString());
        }

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        return restTemplate.postForEntity(aiServerUrl + "/api/v1/parking/chatbot/ask", entity, Map.class);
    }
}
