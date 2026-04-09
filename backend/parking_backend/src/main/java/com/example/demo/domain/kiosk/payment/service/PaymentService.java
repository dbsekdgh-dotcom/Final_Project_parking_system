package com.example.demo.domain.kiosk.payment.service;

import com.example.demo.domain.kiosk.payment.dtos.internal.PaymentEligibilityResult;
import com.example.demo.domain.kiosk.payment.dtos.request.DiscountTicketRequestDto;
import com.example.demo.domain.kiosk.payment.dtos.request.FeeCalculationRequestDto;
import com.example.demo.domain.kiosk.payment.dtos.response.FeeCalculationResponseDto;
import com.example.demo.domain.kiosk.payment.dtos.response.VehiclePaymentResponseDto;
import com.example.demo.domain.shared.parkingTicket.repository.ParkingTicketRepository;
import com.example.demo.domain.shared.parkingfeepolicy.ParkingFeePolicy;
import com.example.demo.domain.shared.parkingfeepolicy.repository.ParkingFeePolicyRepository;
import com.example.demo.domain.shared.parkinglog.ParkingLog;
import com.example.demo.domain.shared.parkinglog.enums.ParkingTypeSnapshot;
import com.example.demo.domain.shared.parkinglog.enums.PaymentStatus;
import com.example.demo.domain.shared.parkinglog.repository.ParkingLogRepository;
import com.example.demo.domain.shared.reservation.enums.Status;
import com.example.demo.domain.shared.reservation.repository.ReservationRepository;
import com.example.demo.domain.shared.ticketPolicy.enums.DiscountType;
import com.example.demo.global.exception.BusinessException;
import com.example.demo.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {
    private final ParkingLogRepository parkinglogRepository;
    private final ReservationRepository reservationRepository;
    private final ParkingFeePolicyRepository parkingFeePolicyRepository;
    private final ParkingTicketRepository parkingTicketRepository;


    //무료 요금 대상자인지 확인
    public PaymentEligibilityResult checkFreeExitEligibility(Long parkingLogId) {
        ParkingLog parkingLog = (ParkingLog) parkinglogRepository.findById(parkingLogId).orElseThrow(() -> {
            throw new BusinessException(ErrorCode.VEHICLE_NOT_ENTERED);
        });

        PaymentStatus status = parkingLog.getPaymentStatus();
        LocalDateTime freeExitTime = parkingLog.getFreeExitUntil();
        String carNumber=parkingLog.getCarNumberSnapshot();

        //주차 시간 계산(화면 표시용)
        LocalDateTime exitTime=LocalDateTime.now();
        long parkingTime=Duration.between(parkingLog.getEnteredAt(),exitTime).toMinutes();

        //1. [NONE 처리] 입주민 /정기권 차량인 경우
        if (PaymentStatus.NONE.equals(status)) {
            return new PaymentEligibilityResult(parkingLog, VehiclePaymentResponseDto.builder().isFree(true).message("등록된 차량입니다.")
                    .rawFee(0).parkingTime(parkingTime).parkingLogId(parkingLogId).vehicleNumber(carNumber).build());
        }

        //2. [PAID/UNPAID 처리] 무료 출차시간 내인 경우
        if (freeExitTime != null && freeExitTime.isAfter(LocalDateTime.now())) {
            String msg = PaymentStatus.PAID.equals(status) ? "사전정산 완료된 차량입니다." : "무료 출차 대상 차량입니다.";
            return new PaymentEligibilityResult(parkingLog, VehiclePaymentResponseDto.builder()
                    .isFree(true).message(msg).rawFee(0).parkingTime(parkingTime).parkingLogId(parkingLogId).vehicleNumber(carNumber).build());
        }

        //3. 방문예약 차량인 경우
        if (parkingLog.getParkingTypeSnapshot().equals(ParkingTypeSnapshot.RESERVATION)) {
            if (reservationRepository.getCountbyCarNumber(carNumber, Status.ENTERED, true)>0){
                return new PaymentEligibilityResult(parkingLog, VehiclePaymentResponseDto.builder()
                        .isFree(true).message("방문 차량입니다.").rawFee(0).parkingTime(parkingTime).parkingLogId(parkingLogId).vehicleNumber(carNumber).build());
            }
        }
        return new PaymentEligibilityResult(parkingLog, null);
    }

    //요금 계산
    public FeeCalculationResponseDto calculateBaseFee(FeeCalculationRequestDto request){
        LocalDateTime now=LocalDateTime.now();
        // 1. 무료 주차 시간을 넘지 않은 경우 0
        long parkingTime=request.getParkingTime();
        int entryGraceTime=request.getPolicy().getGraceMinutes();
        int dailyMaxFee=request.getPolicy().getDailyMaxFee();
        int unitMinutes=request.getPolicy().getUnitMinutes();
        int unitFee=request.getPolicy().getUnitFee();
        int baseFee=request.getPolicy().getBaseFee();
        int prepaidFee=request.getPrepaidFee();

        // 0. 주차시간에서 무료 출자 시간 차감
        long billableTime=Math.max(0,parkingTime-entryGraceTime);

        // 1. 주차시간이 무료 주차 가능시간보다 적으면 요금 0
        if(billableTime==0) {
            return FeeCalculationResponseDto.builder().rawFee(0).calculatedFee(0).paymentRequestedAt(now).parkingTime(parkingTime).build();
        }

        // 2 시간 할인권 차감
        List<DiscountTicketRequestDto> discountTicketRequestDtos =request.getDiscountTicketRequestDtos();
        int totalDiscountMinutes=calculateStackable(discountTicketRequestDtos,DiscountType.TIME);
        long discountedParkingTime=Math.max(0,billableTime-totalDiscountMinutes);

        // 3. 24시간 단위 요금 계산
        long fullDays=discountedParkingTime/1440;
        int fullDaysFee=(int)(fullDays*dailyMaxFee);

        // 4. 24시간을 채우지 못한 나머지 시간 계산
        long remainingMinutes=discountedParkingTime%1440;
        int remainingFee=0;
        if(remainingMinutes>0){
            int extraUnit=(int)Math.ceil(remainingMinutes/(double)unitMinutes);
            remainingFee=Math.min(dailyMaxFee,(extraUnit*unitFee+baseFee));
        }

        // 5. 총 주차요금
        int rawFee=fullDaysFee+remainingFee;

        // 6. 할인권 (discount_type==free 인 경우)
        boolean hasFreeTicket= discountTicketRequestDtos.stream().anyMatch(l->l.getTicketPolicy().getDiscountType().equals(DiscountType.FREE));
        if(hasFreeTicket) return FeeCalculationResponseDto.builder().rawFee(rawFee).calculatedFee(0).paymentRequestedAt(now).build();

        // 7. 사전정산 금액
        int prepaid=(prepaidFee>0)?prepaidFee:0;

        // 8. 할인권 차감
        int discountRate=calculateStackable(discountTicketRequestDtos,DiscountType.RATE);// - 퍼센트 할인
        int discountAmount=calculateStackable(discountTicketRequestDtos,DiscountType.AMOUNT);// - 금액할인
        int totalDiscountAmount=Math.max(0,(rawFee*discountRate/100)+discountAmount);

        // 9. 최종 요금 (사전정산 후 사후 정산 시 할인금액이 아무리 커도 결제 금액은 0원)
        long calculatedFee=Math.max(0,rawFee-totalDiscountAmount);
        long amountToPay=Math.max(0,calculatedFee-prepaid);

        return FeeCalculationResponseDto.builder()
                .rawFee(rawFee)
                .calculatedFee(calculatedFee)
                .totalDiscountMinutes(totalDiscountMinutes)
                .totalDiscountAmount(totalDiscountAmount)
                .amountToPay(amountToPay)
                .paymentRequestedAt(now) //결제요청 시간
                .build();
    }

    public int calculateStackable(List<DiscountTicketRequestDto> discountTicketRequestDtos, DiscountType discountType){
        // 1. 중복 가능한  티켓 합계
        int stackableSum= discountTicketRequestDtos.stream()
                .filter(l->l.getTicketPolicy().getDiscountType().equals(discountType))
                .filter(l->l.getTicketPolicy().isStackable()==true)
                .mapToInt(l->l.getTicketPolicy().getDiscountValue()).sum();
        //2.중복 불가능한 할인 티켓 중 가장 큰 값
        int nonStackableMax= discountTicketRequestDtos.stream()
                .filter(l->l.getTicketPolicy().getDiscountType().equals(discountType))
                .filter(l->l.getTicketPolicy().isStackable()==false)
                .mapToInt(l->l.getTicketPolicy().getDiscountValue())
                .max()
                .orElse(0);
        return Math.max(stackableSum,nonStackableMax);
    }

    public FeeCalculationResponseDto settlementFee(ParkingLog parkingLog,Long parkingFeePolicyId,Long parkingTime){

        //요금 정책이 없는 경우 오류 처리
        ParkingFeePolicy parkingFeePolicy=parkingFeePolicyRepository.findById(parkingFeePolicyId).orElse(null);
        if(parkingFeePolicy==null)throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);

        //할인권 조회(status=='ACTIVE' & 유효기간이 지나지 않은 것)
        List<DiscountTicketRequestDto> list=parkingTicketRepository.getValidTickets(parkingLog.getParkingLogId(), com.example.demo.domain.shared.ticketPolicy.enums.Status.ACTIVE);
        List<DiscountTicketRequestDto> discountTicketRequestDtos =list.stream().filter(l->{
            Long totalMinutes=l.getTicketPolicy().getValidDays()*1440L +l.getTicketPolicy().getValidMinutes();
            LocalDateTime expiryDate=l.getTicketPolicy().getCreatedAt().plusMinutes(totalMinutes);
            return expiryDate.isAfter(LocalDateTime.now());
        }).toList();


        //요금 계산
        FeeCalculationRequestDto request=FeeCalculationRequestDto.builder()
                .parkingTime(parkingTime)
                .policy(parkingFeePolicy)
                .prepaidFee(parkingLog.getFee())
                .discountTicketRequestDtos(discountTicketRequestDtos)
                .build();

        return calculateBaseFee(request);
    }

    //EXIT_REQUESTED
    @Transactional
    public VehiclePaymentResponseDto requestPayment(ParkingLog parkingLog) {
        LocalDateTime now=LocalDateTime.now();
        String carNumber = parkingLog.getCarNumberSnapshot();
        Long parkingFeePolicyId = parkingLog.getParkingFeePolicyId();
        ParkingTypeSnapshot parkingTypeSnapshot = parkingLog.getParkingTypeSnapshot();
        LocalDateTime calculationStartTime=parkingLog.getEnteredAt();
        long parkingLogId=parkingLog.getParkingLogId();
        //주차 시간 계산(화면 표시용)
        LocalDateTime exitTime=LocalDateTime.now();
        long parkingTime=Duration.between(parkingLog.getEnteredAt(),exitTime).toMinutes();

        //타입이 방문인 경우 입차확인&무료요금&방문예약일이 경과했는지 체크
        if (parkingTypeSnapshot.equals(ParkingTypeSnapshot.RESERVATION)) {
            if (reservationRepository.getCountbyCarNumber(carNumber, Status.ENTERED, true)<=0){
                //방문예약시간이 초과한 경우 만료 시간 이후부터 과금
                calculationStartTime=parkingLog.getFreeExitUntil();
            }

        }
        //주차 시간 계산(요금 계산용)
        long totalDurationForCalculation=Duration.between(calculationStartTime,exitTime).toMinutes();//방문예약시간을 초과한 경우를 고려하여 parkingLog.getEnteredAt()대신 사용

        // 기본 요금 계산
        FeeCalculationResponseDto feeCalculationResponseDto=settlementFee(parkingLog,parkingFeePolicyId,totalDurationForCalculation);
        if(feeCalculationResponseDto==null || feeCalculationResponseDto.getCalculatedFee()<=0)    {
            return VehiclePaymentResponseDto.builder().isFree(true).rawFee(0).parkingLogId(parkingLogId).parkingTime(parkingTime).build();
        }
        //db 업데이트FeeCalculationResponseDto
        parkingLog.requestPayment(feeCalculationResponseDto);
        parkinglogRepository.save(parkingLog);

        // 반환
        return VehiclePaymentResponseDto.builder()
                .isFree(false)
                .vehicleNumber(carNumber)
                .parkingTime(parkingTime)
                .rawFee(feeCalculationResponseDto.getRawFee())
                .calculatedFee(feeCalculationResponseDto.getCalculatedFee())
                .amountToPay(feeCalculationResponseDto.getAmountToPay())
                .parkingLogId(parkingLog.getParkingLogId())
                .message("결제가 필요합니다.")
                .build();
    }

}

