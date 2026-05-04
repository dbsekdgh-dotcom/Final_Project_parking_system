package com.example.demo.api.user.chatbot;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/chatbot")
public class ChatbotProxyController {

    private final RestTemplate restTemplate;

    @Value("${AI_SERVER_URL}")
    private String aiServerUrl;

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
