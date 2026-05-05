package com.example.demo.domain.parking.chatbot.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatbotAiRequestDto {
    private String sessionId;
    private String screenId;
    private String userQuestion;
}
