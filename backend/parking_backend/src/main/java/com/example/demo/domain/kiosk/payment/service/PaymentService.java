package com.example.demo.domain.kiosk.payment.service;

import com.example.demo.domain.kiosk.payment.dtos.request.FeeCalculationRequestDto;
import com.example.demo.domain.kiosk.payment.dtos.request.VehiclePaymentRequestDto;
import com.example.demo.domain.kiosk.payment.dtos.response.FeeCalculationResponseDto;
import com.example.demo.domain.kiosk.payment.dtos.response.VehiclePaymentResponseDto;
import com.example.demo.domain.shared.household.repository.HouseholdRepository;
import com.example.demo.domain.shared.parkingTicket.repository.ParkingTicketRepository;
import com.example.demo.domain.shared.parkingfeepolicy.ParkingFeePolicy;
import com.example.demo.domain.shared.parkingfeepolicy.repository.ParkingFeePolicyRepository;
import com.example.demo.domain.shared.parkinglog.ParkingLog;
import com.example.demo.domain.shared.parkinglog.enums.ParkingTypeSnapshot;
import com.example.demo.domain.shared.parkinglog.enums.PaymentStatus;
import com.example.demo.domain.shared.parkinglog.repository.ParkinglogRepository;
import com.example.demo.domain.shared.reservation.enums.Status;
import com.example.demo.domain.shared.reservation.repository.ReservationRepository;
import com.example.demo.domain.shared.subscription.repository.SubscriptionRepository;
import com.example.demo.domain.shared.vehicle.VehicleRepository;
import com.example.demo.global.exception.BusinessException;
import com.example.demo.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {
    private final ParkinglogRepository parkinglogRepository;
    private final ReservationRepository reservationRepository;
    private final ParkingFeePolicyRepository parkingFeePolicyRepository;
    private final ParkingTicketRepository parkingTicketRepository;
    private final VehicleRepository vehicleRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final HouseholdRepository householdRepository;


    //무료 요금 대상자인지 확인
    public VehiclePaymentResponseDto checkFreeExitEligibility(VehiclePaymentRequestDto vehiclePaymentRequestDto){
        ParkingLog parkingLog = (ParkingLog) parkinglogRepository.findById(vehiclePaymentRequestDto.getParkingLogId()).orElseThrow(() -> {
            throw new BusinessException(ErrorCode.VEHICLE_NOT_ENTERED);
        });

        String carNumber = parkingLog.getCarNumberSnapshot();
        PaymentStatus status = parkingLog.getPaymentStatus();
        Long parkingFeePolicyId = parkingLog.getParkingFeePolicyId();
        LocalDateTime freeExitTime = parkingLog.getFreeExitUntil();
        ParkingTypeSnapshot parkingTypeSnapshot = parkingLog.getParkingTypeSnapshot();

        //payment_status가 none인 경우(입주민, 정기권 구매자로 입차시 판단)
        if (PaymentStatus.NONE.equals(status)) {
            //데이터 무결성 오류(출차시간을 update하지 않음)
            if(freeExitTime==null)throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
            //회차 차량 처리(입차 후 아주 짧은 시간(예: 10분 내)에 나가는 '회차 차량'의 경우) && 입주민 또는 정기권 차량인 경우
            if (freeExitTime.isAfter(LocalDateTime.now())) {
                return VehiclePaymentResponseDto.builder().isFree(true).fee(0).message("무료 출차 대상입니다.").build();
            }
        }

        //사전정산 한 경우
        if (PaymentStatus.PAID.equals(status)) {
            //데이터 무결성 오류(출차시간을 update하지 않음)
            if (freeExitTime == null) throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
            //사전정산 후 출차시간 내에 출차하는 경우
            if (freeExitTime.isAfter(LocalDateTime.now())) {
                return VehiclePaymentResponseDto.builder().isFree(true).fee(0).message("사전정산 완료된 차량입니다.").build();
            }
        }
        return null;
    }

    //타입이 방문인 경우 입차확인&무료요금&방문예약일이 경과했는지 체크
    public boolean isValidReservation(String carNumber){
        int isFree=reservationRepository.getCountbyCarNumber(carNumber, Status.ENTERED, true);
        if(isFree>0){
            return true;
        }
        return false;
    }

    // 시간 계산
    public long calculateParkingTime(LocalDateTime calculationStartTime,Integer totalDiscountMinutes){
        //현재 주차시간 계산
        LocalDateTime enteredAt=calculationStartTime; //방문예약시간을 초과한 경우를 고려하여 parkingLog.getEnteredAt()대신 사용
        LocalDateTime exitTime=LocalDateTime.now();
        long parkingTime=Duration.between(enteredAt,exitTime).toMinutes();
        //시간 할인 적용
        int discountMinutes=(totalDiscountMinutes!=null)?totalDiscountMinutes:0;
        parkingTime=Math.max(0,(parkingTime-discountMinutes));
        return parkingTime;
    }

    //요금 계산
    public FeeCalculationResponseDto calculateBaseFee(FeeCalculationRequestDto request){
        // 1. 무료 주차 시간을 넘지 않은 경우 0
        long parkingTime=request.getParkingTime();
        int baseTime=request.getPolicy().getGraceMinutes();
        int dailyMaxFee=request.getPolicy().getDailyMaxFee();
        int unitMinutes=request.getPolicy().getUnitMinutes();
        int unitFee=request.getPolicy().getUnitFee();
        int baseFee=request.getPolicy().getBaseFee();
        int prepaidFee=request.getPrepaidFee();
        int totalDiscountAmount=request.getDiscountAmount();

        if(parkingTime<=baseTime) {
            return new FeeCalculationResponseDto(0,0);
        }

        // 2. 24시간 단위 요금 계산
        long fullDays=parkingTime/1440;
        int fullDaysFee=(int)(fullDays*dailyMaxFee);

        // 3. 24시간을 채우지 못한 나머지 시간 계산
        long remainingMinutes=parkingTime%1440;
        int remainingFee=0;
        if(remainingMinutes<=baseTime){
            remainingFee=0;
        }else{
            int extraTime=(int)(remainingMinutes-baseTime);
            int extraUnit=(int)Math.ceil(extraTime/(double)unitMinutes);
            remainingFee=Math.min(dailyMaxFee,(extraUnit*unitFee+baseFee));
        }

        // 4. 총 주차요금
        int rawFee=fullDaysFee+remainingFee;

        // 5. 사전정산 금액 차감(있는 경우) //금액할인 차감(있는 경우)
        int prepaid=(prepaidFee>0)?prepaidFee:0;
        int discountAmount=(totalDiscountAmount>0)?totalDiscountAmount:0;

        // 6. 최종 요금
        int calculatedFee=rawFee-prepaid-discountAmount;

        return new FeeCalculationResponseDto(rawFee,calculatedFee);
    }

    //요금 계산(사전정산 및 주차시간이 하루가 지난 경우 반영)
    public Map<String,Object> settlementFee(ParkingLog parkingLog,Long parkingFeePolicyId,LocalDateTime calculationStartTime){

        //요금 정책이 없는 경우 오류 처리
        ParkingFeePolicy parkingFeePolicy=parkingFeePolicyRepository.findById(parkingFeePolicyId).orElse(null);
        if(parkingFeePolicy==null)throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);

        //할인권 조회



        //주차 시간 계산
        long parkingTime=calculateParkingTime(calculationStartTime,parkingLog.getTotalDiscountMinutes());

        //요금 계산
        int baseTime=parkingFeePolicy.getGraceMinutes();
        FeeCalculationRequestDto request=FeeCalculationRequestDto.builder()
                .parkingTime(parkingTime)
                .policy(parkingFeePolicy)
                .prepaidFee(parkingLog.getFee())
                .discountAmount(parkingLog.getTotalDiscountAmount())
                .build();

        FeeCalculationResponseDto feeCalculationResponseDto=calculateBaseFee(request);

        // 7. return
        return null;
    }


    //EXIT_REQUESTED
    public VehiclePaymentResponseDto requestPayment(VehiclePaymentRequestDto vehiclePaymentRequestDto) {

        ParkingLog parkingLog = (ParkingLog) parkinglogRepository.findById(vehiclePaymentRequestDto.getParkingLogId()).orElseThrow(() -> {
            throw new BusinessException(ErrorCode.VEHICLE_NOT_ENTERED);
        });
        String carNumber = parkingLog.getCarNumberSnapshot();
        Long parkingFeePolicyId = parkingLog.getParkingFeePolicyId();
        ParkingTypeSnapshot parkingTypeSnapshot = parkingLog.getParkingTypeSnapshot();
        LocalDateTime calculationStartTime=parkingLog.getEnteredAt();

        //타입이 방문인 경우 입차확인&무료요금&방문예약일이 경과했는지 체크
        if (parkingTypeSnapshot.equals(ParkingTypeSnapshot.RESERVATION)) {
            if (isValidReservation(carNumber)){
                return VehiclePaymentResponseDto.builder().isFree(true).fee(0).message("방문 예약 차량입니다.").build();
            }
            //방문예약시간이 초과한 경우 만료 시간 이후부터 과금
            calculationStartTime=parkingLog.getFreeExitUntil();
        }

        // 기본 요금 계산
        Map<String, Object> calculateBaseFee=settlementFee(parkingLog,parkingFeePolicyId,calculationStartTime);
        if(calculateBaseFee==null || (int)calculateBaseFee.get("calculatedFee")<=0)    {
            return VehiclePaymentResponseDto.builder().isFree(true).fee(0).message("결제할 요금이 없습니다.").build();
        }

        //db 업데이트

        // 반환
        return VehiclePaymentResponseDto.builder()
                .build();
    }

}

