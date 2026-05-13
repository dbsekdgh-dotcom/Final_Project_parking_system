package com.example.demo.domain.chat.dtos.response;

import com.example.demo.domain.chat.entity.AdminChatMessage;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ChatMessageResponse {
    private Long messageId;
    private Long roomId;
    private Long senderId;
    private String senderName;
    private String content;
    private LocalDateTime createdAt;

    public static ChatMessageResponse from(AdminChatMessage msg){
        return ChatMessageResponse.builder()
                .messageId(msg.getMessageId())
                .roomId(msg.getRoom().getRoomId())
                .senderId(msg.getSender().getAdminId())
                .senderName(msg.getSender().getName())
                .content(msg.getContent())
                .createdAt(msg.getCreatedAt())
                .build();
    }
}
