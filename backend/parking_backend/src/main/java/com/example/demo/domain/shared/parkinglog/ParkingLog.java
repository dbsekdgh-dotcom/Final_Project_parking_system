package com.example.demo.domain.shared.parkinglog;

import com.example.demo.domain.shared.parkinglog.enums.ParkingStatus;
import com.example.demo.domain.shared.parkinglog.enums.ParkingTypeSnapshot;
import com.example.demo.domain.shared.parkinglog.enums.PaymentStatus;
import com.example.demo.domain.shared.vehicle.Vehicle;
import jakarta.persistence.*;
import jdk.jfr.Timestamp;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = "vehicle")
@Table(name = "parking_log")
public class ParkingLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long parkingLogId;
    @Comment(" DETECTED : 저장되어있는 차량ID")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicleID")
    private Vehicle vehicle;
    @Comment(" ENTERED : 주차된 자리 ID")
    private Long parkingSpaceId;
    @Comment(" DETECTED : 인식된 차량번호")
    private String carNumberSnapshot;
    @Comment(" DETECTED : 인식된 시간")
    @Timestamp
    private LocalDateTime entryTime;
    @Comment(" EXIT_REQUESTED : 출차 대기 시간")
    private LocalDateTime exitTime;
    @Comment(" DETECTED : 인식한 카메라 ID")
    private Long entryCameraId;
    @Comment(" EXIT_REQUESTED : 출차 대기 인식 카메라")
    private Long exitCameraId;
    @Comment(" DETECTED : 입주민,외부,회원,예약 방문")
    @Enumerated(EnumType.STRING)
    @Column(name = "parking_type_snapshot",nullable = false)
    private ParkingTypeSnapshot parkingTypeSnapshot;
    @Comment(" 사전정산, 일반정산 완료(SUCCESS) 상태 : 사용자가 실제로 결제한 요금")
    private Integer fee;
    @Comment(" DETECTED : NONE, 회차시간>NOW() : UNPAID, 정산완료(SUCCESS) : PAID")
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.NONE;
    @Comment(" DETECTED : INSERT시점, EXITED = FORCE_EXITED : 세션종료")
    @Enumerated(EnumType.STRING)
    @Column(name = "parking_status")
    private ParkingStatus parkingStatus = ParkingStatus.DETECTED;
    @Comment(" DETECTED : 적용 되어야하는 요금 정책 ID")
    private Long parkingFeePolicyId;
    @Comment(" EXIT_REQUESTED,사전정산시 : 사용자가 지불해야 하는 돈 ( calculatedFee = fee )" +
            "En")
    private Long calculatedFee;
    @Comment(" ENTERED : 실제로 들어온 시간")
    private LocalDateTime enteredAt;
    @Comment(" EXITED, FORCE_EXITED : 실제로 나간시간")
    private LocalDateTime exitedAt;
    @Comment(" 결제완료(SUCCESS) 시간")
    private LocalDateTime paidAt;
    @Comment(" 회차시간 DETECTED : 현재시간 + graceMinutesSnapshot," +
            "결제완료(SUCCESS): 결제완료시간 + graceMinutesSnapshot")
    private LocalDateTime freeExitUntil;
    @Comment(" DETECTED : 관리자가 설정한 회차시간 스냅샷 ")
    private Long graceMinutesSnapshot;
    @Comment(" DETECTED : ")
    private String entryPlateImage;
    private String exitPlateImage;
}
