package com.example.demo.domain.reservation.policy;

import com.example.demo.domain.auth.admin.entity.Admin;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "reservation_event_policy")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ReservationEventPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservation_event_policy_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    private Admin admin; // 정책을 설정한 관리자

    @Column(name = "event_name", nullable = false, length = 200)
    private String eventName;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "daily_limit_per_household")
    private Integer dailyLimitPerHousehold;

    @Column(name = "monthly_limit_per_household")
    private Integer monthlyLimitPerHousehold;

    @Builder.Default
    @Column(name = "max_active_reservations", nullable = false)
    private Integer maxActiveReservations = 1;

    // ⭐ 추가된 필드: 방문 예약 시 부여되는 주차 허용 시간(분 단위)
    @Builder.Default
    @Column(name = "permitted_minutes", nullable = false)
    private Integer permittedMinutes = 60;

    @Builder.Default
    @Column(name = "no_show_penalty_enabled", nullable = false)
    private boolean noShowPenaltyEnabled = false;

    /**
     * 현재 정책이 유효한 기간인지 확인
     */
    public boolean isEffective(LocalDateTime now) {
        if (now.isBefore(startDate)) return false;
        return endDate == null || now.isBefore(endDate);
    }
}