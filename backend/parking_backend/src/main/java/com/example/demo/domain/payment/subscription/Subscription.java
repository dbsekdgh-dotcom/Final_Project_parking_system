package com.example.demo.domain.payment.subscription;

import com.example.demo.domain.payment.Payment;
import com.example.demo.domain.payment.subscription.enums.Status;
import com.example.demo.domain.resident.User;
import com.example.demo.domain.vehicle.Vehicle;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString(exclude = {"user" ,"vehicle","payment"})
@Table(name = "subscription")
@Getter
public class Subscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long subscriptionId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name ="vehicle_id")
    private Vehicle vehicle;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    @Enumerated(EnumType.STRING)
    private Status status;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private Payment payment;
    @Column(nullable = false)
    private Integer price;
    @CreationTimestamp
    private LocalDateTime createdAt;
    private LocalDateTime activatedAt;
    private LocalDateTime cancelledAt;


    /**
     * 1. 상태를 ACTIVE로 변경하고 활성화 시점을 기록
     */
    public void activate() {
        this.status = Status.ACTIVE;
        this.activatedAt = LocalDateTime.now();
    }

    /**
     * 2. 현재 시점에 이 정기권이 실제로 '사용 가능한 상태'인지 확인
     * (입차 로직에서 이 메서드를 호출하여 SUBSCRIPTION 타입인지 판별합니다)
     */
    public boolean isCurrentlyUsable() {
        LocalDateTime now = LocalDateTime.now();
        return this.status == Status.ACTIVE &&
                (now.isEqual(startDate) || now.isAfter(startDate)) &&
                (now.isEqual(endDate) || now.isBefore(endDate));
    }

    /**
     * 3. 정기권 취소 처리
     */
    public void cancel() {
        this.status = Status.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
    }
    /**
     * 4. 정기권 환불 처리
     * 실제 환불 금액은 연결된 payment 엔티티에서 관리하므로 여기서는 상태만 변경합니다.
     */
    public void refund() {
        this.status = Status.REFUNDED;
        this.cancelledAt = LocalDateTime.now();
    }
    /**
     * 5. 기간 만료 처리
     */
    public void expire() {
        this.status = Status.EXPIRED;
    }

    public void updateStatus(Status status) {
        this.status = status;
        if (status == Status.CANCELLED || status == Status.REFUNDED) {
            this.cancelledAt = LocalDateTime.now();
        }
    }
}
