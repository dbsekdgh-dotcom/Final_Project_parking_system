package com.example.demo.domain.vehicle.blacklist;

import com.example.demo.domain.vehicle.Vehicle;
import com.example.demo.domain.vehicle.blacklist.enums.BlacklistReasonType;
import com.example.demo.domain.vehicle.blacklist.enums.BlacklistStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "vehicle_blacklist")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class VehicleBlacklist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vehicle_blacklist_id")
    private Long id;

    /**
     * 등록 차량 (비회원 차량이면 NULL 가능)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    /**
     * 차단 차량 번호 (snapshot 개념)
     */
    @Column(name = "car_number", nullable = false, length = 25)
    private String carNumber;

    /**
     * 차단 사유 타입
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "reason_type", nullable = false)
    private BlacklistReasonType reasonType;

    /**
     * 상세 사유
     */
    @Column(name = "reason_detail", columnDefinition = "TEXT")
    private String reasonDetail;

    /**
     * 차단 시작
     */
    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    /**
     * 차단 종료 (3000년 = 영구)
     */
    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    /**
     * 상태
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BlacklistStatus status;

    /**
     * 생성일
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 해제일
     */
    @Column(name = "released_at")
    private LocalDateTime releasedAt;


    public boolean isActive() {
        return this.status == BlacklistStatus.ACTIVE;
    }

    public boolean isNowBlocked(LocalDateTime now) {
        return isActive()
                && now.isAfter(startDate)
                && now.isBefore(endDate);
    }

    public void release() {
        this.status = BlacklistStatus.RELEASED;
        this.releasedAt = LocalDateTime.now();
    }
}
