package com.example.demo.domain.notification.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationAiClient {

    private final RestTemplate restTemplate;

    @Value("${AI_SERVER_URL}")
    private String aiServerUrl;

    public  record NotificationContent(String title, String content){}

    public  NotificationContent generate(String notificationType, Map<String,Object> context){
        try {
            HttpHeaders headers=new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String,Object> body = Map.of(
                    "notification_type", notificationType,
                    "context",context
            );
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    aiServerUrl + "/api/v1/notification/generate",
                    new HttpEntity<>(body,headers),
                    Map.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null){
                Map<?,?> res = response.getBody();
                return new NotificationContent(
                        (String) res.get("title"),
                        (String) res.get("content")
                );
            }
        }catch (Exception e){
            log.warn("AI notification 생성 실패 (type={}): {}", notificationType, e.getMessage());
        }
        return null;
    }
}
