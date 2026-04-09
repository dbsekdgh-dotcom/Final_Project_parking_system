package com.example.demo.domain.admin.entity;

import com.example.demo.domain.admin.enums.ActionType;
import com.example.demo.domain.admin.enums.TargetType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.CurrentTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "admin_action_log")
public class AdminActionLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long actionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    @Comment("행위를 수행한 관리자ID")
    private Admin admin; //행위를 수행한 관리자

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Comment("대상 도메인")
    private TargetType targetType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Comment("수행 작업 유형")
    private ActionType actionType;

    @Comment("대상 도메인의 PK")
    private Long targetId;

    @Column(columnDefinition = "json",nullable = false)
    private String beforeData; //수정 전 JSON 데이터

    @Column(columnDefinition = "json",nullable = false)
    private String afterData; //수정 후 JSON 데이터

    @Builder.Default
    @Column(nullable = false)
    @Comment("되돌림 여부")
    private Boolean isReverted = false;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reverted_by_admin_id")
    @Comment("원복을 수행한 관리자")
    private Admin revertedByAdmin;

    private LocalDateTime revertedAt;

    @Column(columnDefinition = "json")
    @Comment("실제 변경된 필드 목록 및 값")
    private String changedFields;

    //원복처리를 위한 비즈니스 메서드
    public void markAsReverted(Admin revertAdmin){
        this.isReverted=true;
        this.revertedByAdmin=revertAdmin;
        this.revertedAt=LocalDateTime.now();
    }
}
