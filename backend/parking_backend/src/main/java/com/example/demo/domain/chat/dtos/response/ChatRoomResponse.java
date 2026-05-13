package com.example.demo.domain.chat.dtos.response;

import com.example.demo.domain.chat.enums.RoomType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ChatRoomResponse {
    private Long roomId;
    private RoomType roomType;
    private String roomName;
    private long unreadCount;
    private String lastMessage;
    private LocalDateTime lastMessageAt;
    private List<MemberInfo> members;

    @Getter
    @Builder
    public static class MemberInfo{
        private Long adminId;
        private String name;
        private String loginId;
    }
}
