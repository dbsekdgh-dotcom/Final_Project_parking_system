package com.example.demo.domain.parking.chatbot.dtos.response;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
@Builder
public class ChatbotResponseDto {
    private String sessionId;
    private String answer;
}
