package com.example.demo.domain.parking.chatbot.dtos.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
@Builder
public class ChatbotResponseDto {
    @JsonProperty("session_id")
    private String sessionId;
    private String answer;
    private String action;
    @JsonProperty("target_path")
    private String targetPath;
    @JsonProperty("target_screen_id")
    private String targetScreenId;
}
