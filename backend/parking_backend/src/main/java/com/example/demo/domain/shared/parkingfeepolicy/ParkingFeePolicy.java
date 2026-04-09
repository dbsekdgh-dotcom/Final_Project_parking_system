package com.example.demo.domain.shared.parkingfeepolicy;

import com.example.demo.domain.admin.entity.Admin;
import com.example.demo.domain.shared.parkingfeepolicy.enums.ParkingType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "parking_fee_policy")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ParkingFeePolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "parking_fee_policy_id")
    private Long id;

    @Comment("등록한 관리자 ID")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    private Admin admin;

    @Enumerated(EnumType.STRING)
    @Column(name = "parking_type", nullable = false)
    @Comment("정책 구분 (VISIT, RESERVATION)")
    private ParkingType parkingType;

    @Builder.Default
    @Column(name = "grace_minutes", nullable = false)
    @ColumnDefault("0")
    @Comment("회차 인정 시간 (분)")
    private Integer graceMinutes = 0;

    @Builder.Default
    @Column(name = "base_fee", nullable = false)
    @ColumnDefault("0")
    @Comment("기본 요금")
    private Integer baseFee = 0;

    @Builder.Default
    @Column(name = "unit_minutes", nullable = false)
    @ColumnDefault("0")
    @Comment("추가 단위 시간 (분)")
    private Integer unitMinutes = 0;

    @Builder.Default
    @Column(name = "unit_fee", nullable = false)
    @ColumnDefault("0")
    @Comment("추가 단위 요금")
    private Integer unitFee = 0;

    @Builder.Default
    @Column(name = "daily_max_fee", nullable = false)
    @ColumnDefault("0")
    @Comment("일 최대 요금")
    private Integer dailyMaxFee = 0;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    @ColumnDefault("1") // BOOLEAN TRUE
    @Comment("현재 활성화 여부")
    private Boolean isActive = true;

    @Column(name = "effective_from", nullable = false)
    @Comment("적용 시작 시점")
    private LocalDateTime effectiveFrom;

    @Builder.Default
    @Column(name = "effective_to", nullable = false)
    @Comment("적용 종료 시점")
    private LocalDateTime effectiveTo = LocalDateTime.of(3000, 1, 1, 0, 0);

    @Version
    @Column(name = "version", nullable = false)
    @Comment("낙관적 락 버전")
    private Long version;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    @Comment("등록 일시")
    private LocalDateTime createdAt;
}
