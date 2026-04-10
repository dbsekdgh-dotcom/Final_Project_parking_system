package com.example.demo.domain.user.mypage.point.entity;

import com.example.demo.domain.shared.payment.Payment;
import com.example.demo.domain.shared.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name="point_log")
public class PointLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="point_log_id")
    private Long pointLogId;

    //user_id FK
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id",nullable = false)
    private User user;

    //Payment_id FK
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="payment_id",nullable = false)
    private Payment payment;

    @Column(name="change_amount", nullable = false)
    private int changeAmount;

    @Column(name="before_point",nullable = false)
    private int beforePoint;

    @Column(name="after_point",nullable = false)
    private int afterPoint;

    @Enumerated(EnumType.STRING)
    @Column(name="reason",nullable = false)
    private PointReason reason;

    @Column(name="description")
    private String description;

    @CreationTimestamp
    @Column(name="created_at",nullable = false)
    private LocalDateTime createdAt;

}
