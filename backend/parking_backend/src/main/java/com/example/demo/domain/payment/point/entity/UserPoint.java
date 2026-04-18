package com.example.demo.domain.payment.point.entity;

import com.example.demo.domain.resident.User;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user_point")
@Builder
@AllArgsConstructor
public class UserPoint {

    @Id
    @Column(name="user_id")
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId  // user_id = PK +FK
    @JoinColumn(name="user_id")
    private User user;

    @Column(name="current_point",nullable = false)
    private int currentPoint;
}
