package com.example.demo.domain.chat.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UnreadNotificationResponse {
    private Long roomId;
    private String senderName;
    private String lastMessage;

    public  static UnreadNotificationResponse of(Long roomId, ChatMessageResponse msg){
        return new UnreadNotificationResponse(roomId, msg.getSenderName(), msg.getContent());
    }
}
