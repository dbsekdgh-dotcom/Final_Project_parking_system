package com.example.demo.domain.shared.reservation;

import com.example.demo.domain.shared.household.Household;
import com.example.demo.domain.shared.reservation.enums.Purpose;
import com.example.demo.domain.shared.reservation.enums.Status;
import com.example.demo.domain.shared.user.User;
import com.example.demo.domain.shared.vehicle.Vehicle;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString(exclude = {"user","vehicle"})
@Builder
@Table(name = "reservation")
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reservationId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;
    @Column(nullable = false)
    private String carNumber;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Status status =Status.PENDING;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Purpose purpose;
    @Column(nullable = false)
    private LocalDateTime visitStartAt;
    @Column(nullable = false)
    private LocalDateTime visitEndAt;
    private LocalDateTime actual_entry_at;
    @CreationTimestamp
    private LocalDateTime createdAt;
    private LocalDateTime cancelledAt;
    @Builder.Default
    @Column(name = "is_free", nullable = false)
    private boolean isFree=true;

    /**
     * 상태 변경 메서드 (일반적인 상태 변경 시 사용)
     */
    public void updateStatus(Status status) {
        this.status = status;
    }

    /**
     * 예약 취소 처리 (상태 변경 + 취소 시간 기록)
     */
    public void cancel(Status cancelledStatus) {
        this.status = cancelledStatus;
        this.cancelledAt = LocalDateTime.now();
    }
}
