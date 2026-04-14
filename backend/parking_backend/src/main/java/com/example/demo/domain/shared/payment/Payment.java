package com.example.demo.domain.shared.payment;

import com.example.demo.domain.kiosk.payment.dtos.response.FeeCalculationResponseDto;
import com.example.demo.domain.shared.parkinglog.ParkingLog;
import com.example.demo.domain.shared.payment.enums.PaymentMethod;
import com.example.demo.domain.shared.payment.enums.PaymentStatus;
import com.example.demo.domain.shared.payment.enums.PaymentType;
import com.example.demo.domain.shared.store.Store;
import com.example.demo.domain.shared.vehicle.Vehicle;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = {"parkingLog","store","vehicle"})
@Builder
@Getter
@Setter
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parking_log_id")
    private ParkingLog parkingLog;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;
    @Column(nullable = false)
    @Builder.Default
    @Comment("실제로 사용자가 지불(승인)한 금액으로 success시 snapshot과 일치해야 함")
    private Long amount = 0L;
    @Column(nullable = false)
    @Comment("할인이 적용된 후 사용자가 최종적으로 내야 할 청구 금액")
    private Long priceSnapshot;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.READY;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentType paymentType;
    @Comment("할인권 구매 시 수량")
    private Integer ticketQuantity;
    private String externalPaymentId;
    @Comment("실제 결제가 성공한 시간")
    private LocalDateTime paidAt;
    @CreationTimestamp
    private LocalDateTime createdAt;
    @Builder.Default
    @Column(nullable = false)
    @Comment("환불처리된 누적 금액")
    private Long refundedAmount=0L;

}
