package com.example.demo.domain.auth.admin.dtos.response;

import com.example.demo.domain.auth.admin.entity.Admin;
import com.example.demo.domain.auth.admin.enums.AdminStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AdminListResponse {
    private Long adminId;
    private String loginId;
    private String name;
    private AdminStatus status;
    private LocalDateTime lastLoginAt;
    private boolean isLoggedIn; // 현재 로그인중(true) / 로그아웃(false)

    public static AdminListResponse of(Admin admin, boolean isLoggedIn){
        return AdminListResponse.builder()
                .adminId(admin.getAdminId())
                .loginId(admin.getLoginId())
                .name(admin.getName())
                .status(admin.getStatus())
                .lastLoginAt(admin.getLastLoginAt())
                .isLoggedIn(isLoggedIn)
                .build();
    }
}
