package com.example.demo.domain.parking.log;

import com.example.demo.domain.payment.dtos.internal.ParkingLogRefundDto;
import com.example.demo.domain.payment.dtos.response.FeeCalculationResponseDto;
import com.example.demo.domain.parking.camera.Camera; // Camera 엔티티 가정
import com.example.demo.domain.parking.policy.ParkingFeePolicy; // Policy 엔티티 가정
import com.example.demo.domain.parking.log.enums.ParkingStatus;
import com.example.demo.domain.parking.log.enums.ParkingTypeSnapshot;
import com.example.demo.domain.parking.log.enums.PaymentStatus;
import com.example.demo.domain.parking.space.ParkingSpace;
import com.example.demo.domain.payment.ticketpolicy.enums.DiscountType;
import com.example.demo.domain.vehicle.Vehicle;
import com.example.demo.global.exception.BusinessException;
import com.example.demo.global.exception.ErrorCode;
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
    private String latestOrderId;

    //오버라이드
    public Integer getGraceMinutesSnapshot(){
        return graceMinutesSnapshot !=null ? graceMinutesSnapshot : 0 ;
    }

    public void requestPayment(FeeCalculationResponseDto feeCalculationResponseDto){
        this.rawFee=feeCalculationResponseDto.getRawFee();
        this.totalDiscountMinutes=feeCalculationResponseDto.getTotalDiscountMinutes();
        this.totalDiscountAmount=feeCalculationResponseDto.getTotalDiscountAmount();
        this.calculatedFee=feeCalculationResponseDto.getCalculatedFee();
        this.paymentRequestedAt=feeCalculationResponseDto.getPaymentRequestedAt();
    }
    public void enter(LocalDateTime freeExitUntil){
        if (!this.parkingStatus.canTransitTo(ParkingStatus.ENTERED)) {
            throw new com.example.demo.global.exception.BusinessException(
                    com.example.demo.global.exception.ErrorCode.INVALID_REQUEST);
        }
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
    public void exitRequested(Long exitCameraId,String imagePath){
        if (!this.parkingStatus.canTransitTo(ParkingStatus.EXIT_REQUESTED)){
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
        this.parkingStatus = ParkingStatus.EXIT_REQUESTED;
        this.exitCameraId = exitCameraId;
        this.exitPlateImage = imagePath;
        this.exitTime = LocalDateTime.now();
    }

    //관리자 상세모달 - 강제출차 case A: 단순 상태 변경 (ex. 사전정산 완료인 경우)
    public void updateStatusToForceExit(LocalDateTime now){
        this.parkingStatus=ParkingStatus.FORCE_EXITED; //상태변경
        //실제 출차 완료 시점(없을때만 기록)
        if(this.exitedAt == null ){ this.exitedAt =now; }
        //출차 시도 시간 (없을때만 기록)
        if(this.exitTime ==null){ this.exitTime = now; }
        //exit_time, exit_camera_id는 기존값 유지
    }

    //관리자 상세모달 - 강제출차 case B: 미결제 차량 강제 출차처리 (관리자 직권 요금 면제)
    public void  updateForFreeForceExit(Integer rawFee, LocalDateTime now){
        this.parkingStatus=ParkingStatus.FORCE_EXITED; //상태변경
        this.exitedAt=now; //실제 출차완료시점 기록

        //비용 데이터 업데이트
        this.rawFee=rawFee; //입차부터 현재까지 계산된 원금
        this.calculatedFee=0L; //청구금액
        this.totalDiscountAmount=rawFee; //원금만큼 전액 할인 처리
        this.fee = 0; //실제 납부 금액 0원 처리

        //결제 상태 및 시간 업데이트
        this.paymentStatus=PaymentStatus.PAID; //결제 완료로 간주
        this.paidAt = now; //결제 시점 기록
    }

    //할인 금액을 관리자가 수정할 수 있는 상태인지 확인
    public boolean isDiscountModifiable(){
        //결제완료된 건 수정 불가
        if(this.paymentStatus==PaymentStatus.PAID) return false;
        //이미출차완료 or 강제출차된 차량 수정 불가
        if(this.parkingStatus==ParkingStatus.EXITED || this.parkingStatus==ParkingStatus.FORCE_EXITED) return false;
        //입차처리(ENTERED) 이후 단계만 가능
        if(this.parkingStatus==ParkingStatus.DETECTED || this.parkingStatus==ParkingStatus.ENTRY_CANCELLED) return false;

        return true;
    }

    //관리자용 할인 수정 메서드
    public void updateAdminDiscount(Integer newAdminsDiscount,Integer currentStoreTotal){
        if(!isDiscountModifiable()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
        // 전달받은 상가 할인합계와 새 관리자 할인을 더함
        int nextTotalDiscount = currentStoreTotal + newAdminsDiscount;
        //원금 초과방지
        this.totalDiscountAmount = Math.min(nextTotalDiscount,this.rawFee);
        //최종 금액 재계산
        this.calculatedFee =(long) Math.max(0, this.rawFee - this.totalDiscountAmount);
        //상태값 초기화
        this.paymentRequestedAt = null;
        this.paymentStatus = (this.calculatedFee > 0) ? PaymentStatus.UNPAID : PaymentStatus.NONE;
    }

    // EXIT_REQUESTED에서 방치된 차량 ENTERED로 되돌리기
    public void revertToEntered(){
        this.parkingStatus = ParkingStatus.ENTERED;
        this.exitCameraId=null;
        this.exitPlateImage=null;
        this.exitTime=null;
    }

    //환불 요청
    public void parkingLogRefund(ParkingLogRefundDto dto){
        this.paymentStatus=dto.getPaymentStatus();
        this.fee=dto.getFee();
        this.paidAt=dto.getPaidAt();
        this.freeExitUntil=dto.getFreeExitUntil();
        this.paymentRequestedAt=dto.getPaymentRequestedAt();
    }

    //강제출차 조회
    public void verifyForceExit(){
        if(this.parkingStatus==ParkingStatus.FORCE_EXITED){
            throw new BusinessException(ErrorCode.FORCE_EXITED);
        }
    }
    //결제 완료에 따른 주차 로그 업데이트
    public void completePayment(int additionalFee,int graceMinutes){
        this.fee+=additionalFee;
        this.paidAt=LocalDateTime.now();
        this.freeExitUntil=paidAt.plusMinutes(graceMinutes);
        this.paymentStatus=PaymentStatus.PAID;
    }

    public void applyStoreTicket(
            com.example.demo.domain.payment.ticketpolicy.enums.DiscountType discountType, Integer discountValue){
        switch (discountType){
            case TIME -> this.totalDiscountMinutes += discountValue;
            case AMOUNT -> {
                this.totalDiscountAmount = Math.min(
                    this.totalDiscountAmount + discountValue,
                    this.rawFee > 0 ? this.rawFee : Integer.MAX_VALUE);
                if (this.rawFee > 0){
                    this.calculatedFee = (long) Math.max(0, this.rawFee - this.totalDiscountAmount);
                }
            }
            case FREE -> {
                if (this.rawFee > 0) {
                    this.totalDiscountAmount = this.rawFee;
                    this.calculatedFee = 0L;
                }
            }
            case RATE -> {
                if (this.rawFee > 0) {
                    int amt = this.rawFee * discountValue / 100;
                    this.totalDiscountAmount = Math.min(
                            this.totalDiscountAmount + amt, this.rawFee);
                    this.calculatedFee = (long) Math.max(0, this.rawFee - this.totalDiscountAmount);
                }
            }
        }
        if (this.rawFee > 0 && this.paymentStatus != PaymentStatus.PAID){
            this.paymentRequestedAt = null;
        }
    }


}