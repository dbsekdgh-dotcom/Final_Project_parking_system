package com.example.demo.domain.vehicle.blacklist;

import com.example.demo.domain.vehicle.blacklist.enums.BlacklistReasonType;
import com.example.demo.domain.vehicle.blacklist.enums.BlacklistStatus; // 이 패키지도 L 소문자인지 꼭 확인!
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "vehicle_blacklist")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class VehicleBlacklistEntity { // L을 대문자로 해서 'Blacklist'로 맞추는 게 정석이에요!

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name ="vehicle_blacklist_id")
    private Long id;

    @Column(name = "vehicle_id")
    private Long vehicleId;

    @Column(name = "car_number", nullable = false, length = 25)
    private String carNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason_type", nullable = false)
    private BlacklistReasonType reasonType;

    @Column(name = "reason_detail", columnDefinition = "TEXT")
    private String reasonDetail;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING) // status는 Enum이니까 이게 붙어야 해요!
    @Column(name = "status", nullable = false)
    private BlacklistStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "released_at")
    private LocalDateTime releasedAt;

    /**
     * 차단 해제 시 상태와 날짜를 업데이트하는 메서드
     */
    public void release(){
        LocalDateTime now = LocalDateTime.now();
        this.status = BlacklistStatus.RELEASED;
        this.releasedAt = now;
        this.endDate = now;
    }
}