package com.example.demo.domain.kiosk.parking.entity;

import com.example.demo.domain.kiosk.parking.enums.ParkingStatus;
import com.example.demo.domain.kiosk.parking.enums.ParkingTypeSnapshot;
import com.example.demo.domain.kiosk.parking.enums.PaymentStatus;
import jakarta.persistence.*;
import jdk.jfr.Timestamp;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Table(name = "Parking_Log")
public class ParkingLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long parkingLogId;
    private Long vehicleId;
    private Long parkingSpaceId;
    private String carNumberSnapshot;
    @Timestamp
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private Long entryCameraId;
    private Long exitCameraId;
    @Enumerated(EnumType.STRING)
    @Column(name = "parking_type_snapshot",nullable = false)
    private ParkingTypeSnapshot parkingTypeSnapshot;
    private Integer fee;
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.NONE;
    @Enumerated(EnumType.STRING)
    @Column(name = "parking_status")
    private ParkingStatus parkingStatus = ParkingStatus.DETECTED;
    private Long parkingFeePolicyId;
    private Long calculatedFee;
    private LocalDateTime enteredAt;
    private LocalDateTime exitedAt;
    private LocalDateTime paidAt;
    private LocalDateTime freeExitUntil;
    private Long GraceMinutesSnapshot;
    private String entryPlateImage;
    private String exitPlateImage;
}
