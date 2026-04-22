package com.example.demo.domain.management.dtos.response;

import com.example.demo.domain.resident.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AdminUserResponseDto {
    private Long userId;
    private String name;
    private String email;
    private String phone;
    private String type;
    private String status;
    private LocalDateTime createdAt;

    public static AdminUserResponseDto from(User user){
        return AdminUserResponseDto.builder()
                .userId(user.getUserId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .type(user.getHousehold() != null ? " 입주민 " : " 일반 ")
                .status(user.getStatus().name())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
