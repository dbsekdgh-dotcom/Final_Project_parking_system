package com.example.demo.domain.shared.approval;


import com.example.demo.domain.shared.approval.enums.ApprovalStatus;
import com.example.demo.domain.shared.approval.enums.ApprovalType;
import com.example.demo.domain.shared.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "approval")
public class Approval {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "approval_id")
    private Long approvalId;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_type", nullable = false)
    private ApprovalType approvalType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_user_id")
    private User requestUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ApprovalStatus status = ApprovalStatus.PENDING;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "processed_by_admin_id")
    private Long processedByAdminId;

    @Column(name = "reject_reason", columnDefinition = "TEXT")
    private String rejectReason;

    @Builder
    public Approval(ApprovalType approvalType, Long targetId, User requestUserId,
                    ApprovalStatus status, LocalDateTime processedAt) {
        this.approvalType = approvalType;
        this.targetId = targetId;
        this.requestUserId = requestUserId;
        this.status = (status != null) ? status : ApprovalStatus.PENDING;
        this.processedAt = processedAt; // 자동 승인 시간을 기록하기 위해 얘만 남김
    }

    @PrePersist
    public void onPrePersist() {
        this.createdAt = ZonedDateTime.now(ZoneId.of("Asia/Seoul")).toLocalDateTime();
    }

    /**
     * 🚩 결재 상태 변경을 위해 이 메서드를 추가해야 합니다!
     */
    public void updateStatus(ApprovalStatus status) {
        this.status = status;
    }

    /**
     * 사용자가 직접 신청을 취소할 때 사용
     */
    public void cancel() {
        this.status = ApprovalStatus.CANCELLED;
        this.processedAt = LocalDateTime.now();
        this.rejectReason = "사용자가 신청을 취소하였습니다.";
    }


}