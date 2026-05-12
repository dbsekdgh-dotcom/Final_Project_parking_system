package com.example.demo.domain.chat.dtos.request;

import lombok.Getter;

import java.util.List;

@Getter
public class CreateGroupRoomRequest {
    private String roomName;
    private List<Long> memberAdminIds;
}
