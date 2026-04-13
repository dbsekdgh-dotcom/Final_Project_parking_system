package com.example.demo.domain.user.apply.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserStatusResponseDto {

    private String userStatus;

    private Long activeApprovalId;
}
