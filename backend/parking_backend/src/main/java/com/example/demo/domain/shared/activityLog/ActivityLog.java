package com.example.demo.domain.shared.activityLog;

import com.example.demo.domain.shared.activityLog.enums.ActivityType;
import com.example.demo.domain.shared.household.Household;
import com.example.demo.domain.shared.parkinglog.ParkingLog;
import com.example.demo.domain.shared.payment.Payment;
import com.example.demo.domain.shared.reservation.Reservation;
import com.example.demo.domain.shared.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@ToString(exclude = {"parkingLog", "reservation", "payment", "household"})
@Table(name = "activity_log")
public class ActivityLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long activityId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivityType activityType;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parking_log_id")
    private ParkingLog parkingLog;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id")
    private Reservation reservation;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private Payment payment;
    private String carNumber;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "household_id")
    private Household household;
    private String message;
    @CreationTimestamp
    private LocalDateTime createdAt;

    public static ActivityLog ofEntry(ParkingLog parkingLog, Household household){
        return ActivityLog.builder()
                .activityType(ActivityType.ENTRY)
                .parkingLog(parkingLog)
                .carNumber(parkingLog.getCarNumberSnapshot())
                .household(household)
                .message("입차 완료")
                .build();
    }
    public static ActivityLog ofExit(ParkingLog parkingLog, Household household){
        return ActivityLog.builder()
                .activityType(ActivityType.EXIT)
                .parkingLog(parkingLog)
                .carNumber(parkingLog.getCarNumberSnapshot())
                .household(household)
                .message("출차 완료")
                .build();
    }

}
