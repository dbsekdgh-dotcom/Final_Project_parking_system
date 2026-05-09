package com.example.demo.domain.parking.chatbot.dtos.request;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
@Setter
@Builder
public class ChatbotRequestDto {
    private String sessionId;
    private String screenId;
    private String userQuestion;
}
