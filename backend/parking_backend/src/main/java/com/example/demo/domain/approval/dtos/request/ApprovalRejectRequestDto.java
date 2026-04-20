package com.example.demo.domain.approval.dtos.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ApprovalRejectRequestDto {
    private String rejectReason;
}
