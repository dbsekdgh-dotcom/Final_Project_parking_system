package com.example.demo.domain.shared.parkinglog;

import com.example.demo.domain.kiosk.payment.dtos.response.FeeCalculationResponseDto;
import com.example.demo.domain.shared.camera.Camera; // Camera 엔티티 가정
import com.example.demo.domain.shared.parkingfeepolicy.ParkingFeePolicy; // Policy 엔티티 가정
import com.example.demo.domain.shared.parkinglog.enums.ParkingStatus;
import com.example.demo.domain.shared.parkinglog.enums.ParkingTypeSnapshot;
import com.example.demo.domain.shared.parkinglog.enums.PaymentStatus;
import com.example.demo.domain.shared.parkingspace.ParkingSpace;
import com.example.demo.domain.shared.vehicle.Vehicle;
import jakarta.persistence.*;
import jdk.jfr.Timestamp;
import lombok.*;
import lombok.extern.java.Log;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString(exclude = {"vehicle", "parkingSpace"})
@Table(name = "parking_log")
public class ParkingLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long parkingLogId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id") // SQL과 일치시킴
    @Comment("DETECTED : 저장되어있는 차량ID")
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parking_space_id")
    @Comment("ENTERED : 주차된 자리 ID")
    private ParkingSpace parkingSpace;

    @Comment("DETECTED : 인식된 차량번호")
    @Column(name = "car_number_snapshot", length = 25, nullable = false)
    private String carNumberSnapshot;

    @Comment("DETECTED : 차단된 사용자인지 아닌지 확인")
    @Column(nullable = false)
    private Boolean isBlacklist;

    @Comment("DETECTED : 인식된 시간")
    @CreationTimestamp
    private LocalDateTime entryTime;

    @Comment("EXIT_REQUESTED : 출차 대기 시간")
    private LocalDateTime exitTime;

    // 연관관계로 설정하는 것이 좋습니다 (ID만 쓸 경우 @Column(name="...") 명시)
    @Comment("ENTERED : 인식한 카메라 ID")
    @Column(name = "entry_camera_id")
    private Long entryCameraId;

    @Comment("EXIT_REQUESTED : 출차 대기 인식 카메라")
    @Column(name = "exit_camera_id")
    private Long exitCameraId;

    @Enumerated(EnumType.STRING)
    @Column(name = "parking_type_snapshot", nullable = false)
    @Comment("DETECTED : 입주민,외부,회원,예약 방문")
    private ParkingTypeSnapshot parkingTypeSnapshot;

    @Comment("사전정산, 일반정산 완료(SUCCESS) 상태 : 사용자가 실제로 결제한 요금")
    private Integer fee=0;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    @Builder.Default
    @Comment("DETECTED : NONE, 회차시간>NOW() : UNPAID, 정산완료(SUCCESS) : PAID")
    private PaymentStatus paymentStatus = PaymentStatus.NONE;

    @Enumerated(EnumType.STRING)
    @Column(name = "parking_status")
    @Builder.Default
    @Comment("DETECTED : INSERT시점, EXITED = FORCE_EXITED : 세션종료")
    private ParkingStatus parkingStatus = ParkingStatus.DETECTED;

    @Column(name = "parking_fee_policy_id", nullable = false)
    @Comment("DETECTED : 적용 되어야하는 요금 정책 ID")
    private Long parkingFeePolicyId;

    @Comment("EXIT_REQUESTED, 사전정산시 : 사용자가 지불해야 하는 돈")
    private Long calculatedFee=0L;

    @Comment("ENTERED : 실제로 들어온 시간")
    private LocalDateTime enteredAt;

    @Comment("EXITED, FORCE_EXITED : 실제로 나간시간")
    private LocalDateTime exitedAt;

    @Comment("할인받은 시간")
    private Integer totalDiscountMinutes=0;

    @Comment("할인받은 요금")
    private Integer totalDiscountAmount=0; // 오타 수정: totla -> total

    @Comment("할인 받기전 순수요금 ENTERED ~ 결제 까지")
    private Integer rawFee=0;

    @Comment("결제완료(SUCCESS) 시간")
    private LocalDateTime paidAt;

    @Comment("회차시간 데드라인")
    private LocalDateTime freeExitUntil;

    @Column(name = "grace_minutes_snapshot", nullable = false)
    @Comment("DETECTED : 관리자가 설정한 회차시간 스냅샷")
    private Integer graceMinutesSnapshot; // SQL과 타입 일치 (Integer)

    @Column(name = "entry_plate_image", length = 512, nullable = false)
    @Comment("DETECTED : 입차시 차량번호 이미지 저장 경로")
    private String entryPlateImage;

    @Column(name = "exit_plate_image", length = 512)
    @Comment("EXIT_REQUESTED : 출차시 차량번호 이미지 저장 경로")
    private String exitPlateImage;

    @Column(name = "payment_requested_at")
    @Comment("요금 조회 및 결제 요청 시점 검증")
    private LocalDateTime paymentRequestedAt;

    public void requestPayment(FeeCalculationResponseDto feeCalculationResponseDto){
        this.rawFee=feeCalculationResponseDto.getRawFee();
        this.totalDiscountMinutes=feeCalculationResponseDto.getTotalDiscountMinutes();
        this.totalDiscountAmount=feeCalculationResponseDto.getTotalDiscountAmount();
        this.calculatedFee=feeCalculationResponseDto.getCalculatedFee();
    }
    public void enter(Camera camera, LocalDateTime freeExitUntil){
        if (!this.parkingStatus.canTransitTo(ParkingStatus.ENTERED)) {
            throw new com.example.demo.global.exception.BusinessException(
                    com.example.demo.global.exception.ErrorCode.INVALID_REQUEST);
        }
        this.entryCameraId=camera.getId();
        this.parkingStatus=ParkingStatus.ENTERED;
        this.enteredAt=LocalDateTime.now();
        this.freeExitUntil=freeExitUntil; // RESIDENT=null, SUBSCRIPTION=정기권만료일, 나머지=입차시간+grace
    }

    public void cancel(){
        if (!this.parkingStatus.canTransitTo(ParkingStatus.ENTRY_CANCELLED)) {
            throw new com.example.demo.global.exception.BusinessException(
                    com.example.demo.global.exception.ErrorCode.INVALID_REQUEST);
        }
        this.parkingStatus = ParkingStatus.ENTRY_CANCELLED;
    }
}