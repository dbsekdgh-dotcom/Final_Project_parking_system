package com.example.demo.domain.auth.admin.dtos.response;

import com.example.demo.domain.auth.admin.entity.AdminActionLog;
import com.example.demo.domain.auth.admin.enums.ActionType;
import com.example.demo.domain.auth.admin.enums.TargetType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ActionLogResponseDto {
    private Long actionId;
    private String adminName;
    private TargetType targetType;
    private ActionType actionType;
    private Long targetId;
    private String beforeData;
    private String afterData;
    private Boolean isReverted;
    private LocalDateTime createdAt;
    private String revertedByAdminName;
    private LocalDateTime revertedAt;

    public static ActionLogResponseDto from(AdminActionLog log) {
        return ActionLogResponseDto.builder()
                .actionId(log.getActionId())
                .adminName(log.getAdmin().getName())
                .targetType(log.getTargetType())
                .actionType(log.getActionType())
                .targetId(log.getTargetId())
                .beforeData(log.getBeforeData())
                .afterData(log.getAfterData())
                .isReverted(log.getIsReverted())
                .createdAt(log.getCreatedAt())
                .revertedByAdminName(log.getRevertedByAdmin() != null ? log.getRevertedByAdmin().getName() : null)
                .revertedAt(log.getRevertedAt())
                .build();
    }
}
