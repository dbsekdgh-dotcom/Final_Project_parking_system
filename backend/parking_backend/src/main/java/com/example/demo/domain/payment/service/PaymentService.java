package com.example.demo.domain.payment.service;

import com.example.demo.domain.payment.dtos.internal.AppliedTicketResult;
import com.example.demo.domain.payment.dtos.internal.PaymentEligibilityResult;
import com.example.demo.domain.payment.dtos.internal.StackableTicketResult;
import com.example.demo.domain.payment.dtos.request.DiscountTicketRequestDto;
import com.example.demo.domain.payment.dtos.request.FeeCalculationRequestDto;
import com.example.demo.domain.payment.dtos.response.FeeCalculationResponseDto;
import com.example.demo.domain.payment.dtos.response.VehiclePaymentResponseDto;
import com.example.demo.domain.payment.ticket.ParkingTicket;
import com.example.demo.domain.payment.ticket.repository.ParkingTicketRepository;
import com.example.demo.domain.parking.policy.ParkingFeePolicy;
import com.example.demo.domain.parking.policy.repository.ParkingFeePolicyRepository;
import com.example.demo.domain.parking.log.ParkingLog;
import com.example.demo.domain.parking.log.enums.ParkingTypeSnapshot;
import com.example.demo.domain.parking.log.enums.PaymentStatus;
import com.example.demo.domain.parking.log.repository.ParkingLogRepository;
import com.example.demo.domain.reservation.enums.Status;
import com.example.demo.domain.reservation.repository.ReservationRepository;
import com.example.demo.domain.system.setting.SettingKey;
import com.example.demo.domain.system.setting.SystemSetting;
import com.example.demo.domain.system.setting.repository.SystemSettingRepository;
import com.example.demo.domain.payment.ticketpolicy.enums.DiscountType;
import com.example.demo.domain.payment.ticketpolicy.enums.UseType;
import com.example.demo.global.exception.BusinessException;
import com.example.demo.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Log4j2
public class PaymentService {
    private final ParkingLogRepository parkinglogRepository;
    private final ReservationRepository reservationRepository;
    private final ParkingFeePolicyRepository parkingFeePolicyRepository;
    private final ParkingTicketRepository parkingTicketRepository;
    private static final int MINUTES_PER_DAY = 1440;
    private final SystemSettingRepository systemSettingRepository;


    //무료 요금 대상자인지 확인
    public PaymentEligibilityResult checkFreeExitEligibility(Long parkingLogId) {
        LocalDateTime now = LocalDateTime.now();
        ParkingLog parkingLog = (ParkingLog) parkinglogRepository.getDetailLogInfo(parkingLogId).orElseThrow(() -> {
            throw new BusinessException(ErrorCode.VEHICLE_NOT_ENTERED);
        });

        PaymentStatus status = parkingLog.getPaymentStatus();
        LocalDateTime freeExitTime = parkingLog.getFreeExitUntil();
        String carNumber=parkingLog.getCarNumberSnapshot();
        VehiclePaymentResponseDto responseDto=null;

        //주차 시간 계산(화면 표시용)
        LocalDateTime exitTime=now;
        long parkingTime=Duration.between(parkingLog.getEnteredAt(),exitTime).toMinutes();

        //1. [NONE 처리] 입주민 /정기권 차량인 경우
        if (PaymentStatus.NONE.equals(status)) {
            responseDto= VehiclePaymentResponseDto.builder().isFree(true).message("등록된 차량입니다.")
                    .rawFee(0).parkingTime(parkingTime).parkingLogId(parkingLogId).vehicleNumber(carNumber).build();
        }

        //2. [PAID/UNPAID 처리] 무료 출차시간 내인 경우
        if (freeExitTime != null && freeExitTime.isAfter(now)) {
            String msg = PaymentStatus.PAID.equals(status) ? "사전정산 완료된 차량입니다." : "무료 출차 대상 차량입니다.";
            responseDto= VehiclePaymentResponseDto.builder()
                    .isFree(true).message(msg).rawFee(0).parkingTime(parkingTime).parkingLogId(parkingLogId).vehicleNumber(carNumber).build();
        }

        //3. 방문예약 차량인 경우
        if (parkingLog.getParkingTypeSnapshot().equals(ParkingTypeSnapshot.RESERVATION)) {
            if (reservationRepository.getCountbyCarNumber(carNumber, Status.ENTERED, true)>0){
                responseDto= VehiclePaymentResponseDto.builder()
                        .isFree(true).message("방문 차량입니다.").rawFee(0).parkingTime(parkingTime).parkingLogId(parkingLogId).vehicleNumber(carNumber).build();
            }
        }
        log.info("차량번호: {}, 타입: {}, 최종 무료판정: {}",
                carNumber, parkingLog.getParkingTypeSnapshot(), responseDto != null && responseDto.isFree());
        return new PaymentEligibilityResult(parkingLog, responseDto);
    }

    //요금 계산
    public FeeCalculationResponseDto calculateBaseFee(FeeCalculationRequestDto request){
        long parkingTime=request.getParkingTime();
        int entryGraceTime=request.getPolicy().getGraceMinutes();
        int dailyMaxFee=request.getPolicy().getDailyMaxFee();
        int unitMinutes=request.getPolicy().getUnitMinutes();
        int unitFee=request.getPolicy().getUnitFee();
        int baseFee=request.getPolicy().getBaseFee();
        int prepaidFee=request.getPrepaidFee();
        LocalDateTime now = LocalDateTime.now();
        List<AppliedTicketResult> ticketList=new ArrayList<>();

        // 1. 주차시간에서 무료 출자 시간 차감
        long billableTime=Math.max(0,parkingTime-entryGraceTime);

        // 2. 주차시간이 무료 주차 가능시간보다 적으면 요금 0
        if(billableTime==0) {
            return FeeCalculationResponseDto.builder().rawFee(0).calculatedFee(0).paymentRequestedAt(now).parkingTime(parkingTime).build();
        }

        // 3. 사전정산 금액
        int prepaid=(prepaidFee>0)?prepaidFee:0;

        // 4. rawFee
        String overTimeFee=systemSettingRepository.findBySettingKey(SettingKey.OVERTIME_MIN_FEE.name()).map(SystemSetting::getSettingValue).orElse(SettingKey.OVERTIME_MIN_FEE.getDefaultValue());
        int rawFee=calculatedrawFee(billableTime,dailyMaxFee,unitMinutes,unitFee,baseFee);
        if(request.getFreeExitUntil()!=null && request.getFreeExitUntil().isBefore(LocalDateTime.now())){
            int minFee=Integer.parseInt(overTimeFee);
            if((rawFee-prepaid)<minFee){
                rawFee+=minFee;
            }
        }

        // 5. 시간 할인권 차감
        List<ParkingTicket> discountTicketRequestDtos =request.getDiscountTicketRequestDtos();
        StackableTicketResult timeDiscountResult=calculateStackable(discountTicketRequestDtos,DiscountType.TIME, UseType.STORE);
        int totalDiscountMinutes=(int)Math.min(billableTime,timeDiscountResult.getTotalAmount());
        long discountedParkingTime=Math.max(0,billableTime-totalDiscountMinutes);

        long remainingBillableTime = billableTime; // 차감할 주차 시간 (분)
        if(timeDiscountResult.getTotalAmount()>0){
            for (AppliedTicketResult detail : timeDiscountResult.getDetails()){
                if(remainingBillableTime<=0)break; //더이상 깎을 시간이 없으면 종료
                int actualTimeEffect=(int)Math.min(remainingBillableTime,detail.getAppliedValue());
                ticketList.add(new AppliedTicketResult(
                        detail.getTicketId(),
                        detail.getPolicyId(),
                        detail.getStoreId(),
                        actualTimeEffect
                ));
                remainingBillableTime-=actualTimeEffect;
            }
        }

        // 6. 할인 후 주차요금
        int timeDiscountedRawFee=(totalDiscountMinutes==0)?rawFee:calculatedrawFee(discountedParkingTime,dailyMaxFee,unitMinutes,unitFee,baseFee);
        if(request.getFreeExitUntil()!=null && request.getFreeExitUntil().isBefore(LocalDateTime.now())){
            int minFee=Integer.parseInt(overTimeFee);
            if((timeDiscountedRawFee-prepaid)<minFee){
                timeDiscountedRawFee+=minFee;
            }
        }

        // 7 -- 할인가능한 금액
        int currentBalance=timeDiscountedRawFee-prepaid;

        // 8. 할인권 (discount_type==free 인 경우)
        Optional<ParkingTicket> freeTicketOpt= discountTicketRequestDtos.stream().filter(l->l.getTicketPolicy().getDiscountType().equals(DiscountType.FREE)).findFirst();
        if(freeTicketOpt.isPresent()) {
            ticketList.add(new AppliedTicketResult(
                    freeTicketOpt.get().getParkingTicketId(),
                    freeTicketOpt.get().getTicketPolicy().getTicketPolicyId(),
                    freeTicketOpt.get().getStore().getStoreId(),
                    currentBalance
            ));
            //무료 할인권 포함
            return FeeCalculationResponseDto.builder()
                    .rawFee(timeDiscountedRawFee)
                    .calculatedFee(prepaid)
                    .totalDiscountMinutes(totalDiscountMinutes)
                    .totalDiscountAmount(currentBalance)
                    .amountToPay(0)
                    .paymentRequestedAt(now)
                    .parkingTime(parkingTime)
                    .vehicleNumber(request.getVehicleNumber())
                    .stackableTicketResult(new StackableTicketResult(currentBalance,ticketList))
                    .build();
        }

        // 9. 할인권 차감
        StackableTicketResult rateDiscountResult=calculateStackable(discountTicketRequestDtos,DiscountType.RATE, UseType.STORE);// - 퍼센트 할인
        StackableTicketResult amountDiscountResult=calculateStackable(discountTicketRequestDtos,DiscountType.AMOUNT,UseType.STORE);// - 금액할인

        // - rate 할인의 경우 퍼센트가 아닌 실제 감면 금액으로 변환
        currentBalance=applyDiscount(currentBalance,rateDiscountResult,timeDiscountedRawFee,ticketList,DiscountType.RATE);

        // - amaount 할인
        currentBalance=applyDiscount(currentBalance,amountDiscountResult,timeDiscountedRawFee,ticketList,DiscountType.AMOUNT);

        // 10. 관리자 할인 적용
        StackableTicketResult adminDiscountResult=calculateStackable(discountTicketRequestDtos,DiscountType.AMOUNT, UseType.ADMIN);// - 금액할인
        currentBalance=applyDiscount(currentBalance,adminDiscountResult,timeDiscountedRawFee,ticketList,DiscountType.AMOUNT);

        // 11. 최종 요금 (사전정산 후 사후 정산 시 할인금액이 아무리 커도 결제 금액은 0원)
        long calculatedFee=Math.max(0,currentBalance+prepaid);
        long amountToPay=Math.max(0,currentBalance);


        return FeeCalculationResponseDto.builder()
                .rawFee(timeDiscountedRawFee)
                .calculatedFee(calculatedFee)
                .totalDiscountMinutes(totalDiscountMinutes)
                .totalDiscountAmount((int)(timeDiscountedRawFee-calculatedFee))
                .amountToPay(amountToPay)
                .paymentRequestedAt(now) //결제요청 시간
                .parkingTime(parkingTime)
                .vehicleNumber(request.getVehicleNumber())
                .stackableTicketResult(new StackableTicketResult((int)(timeDiscountedRawFee-amountToPay-prepaid),ticketList))
                .build();
    }

    public int applyDiscount(int currentBalance, StackableTicketResult discountResult, int timeDiscountedRawFee, List<AppliedTicketResult> resultList, DiscountType type){
        if(discountResult.getTotalAmount()>0){
            for (AppliedTicketResult detail : discountResult.getDetails()){
                if(currentBalance<=0)break; //더이상 깎을 시간이 없으면 종료

                int effect=(type==DiscountType.RATE)?
                        (int)(detail.getAppliedValue()*timeDiscountedRawFee/100):detail.getAppliedValue();

                int actualEffect=Math.min(currentBalance,effect);
                resultList.add(new AppliedTicketResult(
                        detail.getTicketId(),
                        detail.getPolicyId(),
                        detail.getStoreId(),
                        actualEffect
                ));
                currentBalance-=actualEffect;
            }
        }
        return currentBalance;
    }

    public int calculatedrawFee(long discountedParkingTime,int dailyMaxFee,int unitMinutes,int unitFee,int baseFee){
        //1. 24시간 단위 요금 계산
        long fullDays=discountedParkingTime/MINUTES_PER_DAY;
        int fullDaysFee=(int)(fullDays*dailyMaxFee);

        //2. 24시간을 채우지 못한 나머지 시간 계산
        long remainingMinutes=discountedParkingTime%MINUTES_PER_DAY;
        int remainingFee=0;
        if(remainingMinutes>0){
            int extraUnit=(int)Math.ceil(remainingMinutes/(double)unitMinutes);
            remainingFee=Math.min(dailyMaxFee,(extraUnit*unitFee+baseFee));
        }

        return  remainingFee+fullDaysFee;
    }


    public StackableTicketResult calculateStackable(List<ParkingTicket> discountTicketRequestDtos, DiscountType discountType, UseType useType){
        List<AppliedTicketResult> resultList=new ArrayList<>();
        int max=0;

        // 1. 중복 가능한 티켓
       List<ParkingTicket> stackable= discountTicketRequestDtos.stream()
                .filter(l->l.getTicketPolicy().getDiscountType().equals(discountType))
                .filter(l->l.getTicketPolicy().isStackable()==true)
               .filter(l->useType.equals(l.getTicketPolicy().getUseType())).toList();
       int stackableSum=(!stackable.isEmpty())?
           stackable.stream().mapToInt(l->l.getTicketPolicy().getDiscountValue()).sum():0;

        //2.중복 불가능한 할인 티켓 중 가장 큰 값
        ParkingTicket nonStackableMax= discountTicketRequestDtos.stream()
                .filter(l->l.getTicketPolicy().getDiscountType().equals(discountType))
                .filter(l->l.getTicketPolicy().isStackable()==false)
                .filter(l->useType.equals(l.getTicketPolicy().getUseType()))
                .max(Comparator.comparingInt(l->l.getTicketPolicy().getDiscountValue())).orElse(null);
        int nonStackableMaxValue=(nonStackableMax!=null)?nonStackableMax.getTicketPolicy().getDiscountValue():0;

        if(stackableSum>=nonStackableMaxValue && stackableSum>0){
            stackable.forEach(l->resultList.add(new AppliedTicketResult(l.getParkingTicketId(),l.getTicketPolicy().getTicketPolicyId(),l.getStore().getStoreId(),l.getTicketPolicy().getDiscountValue())));
            return new StackableTicketResult(stackableSum,resultList);
        }else if(stackableSum<nonStackableMaxValue && nonStackableMaxValue>0) {
            resultList.add(new AppliedTicketResult(nonStackableMax.getParkingTicketId(),nonStackableMax.getTicketPolicy().getTicketPolicyId(),nonStackableMax.getStore().getStoreId(),nonStackableMax.getTicketPolicy().getDiscountValue()));
            return new StackableTicketResult(nonStackableMaxValue,resultList);
        }
        return new StackableTicketResult(0,resultList);
    }

    public FeeCalculationResponseDto settlementFee(ParkingLog parkingLog,Long parkingFeePolicyId,Long parkingTime){
        //요금 정책이 없는 경우 오류 처리
        ParkingFeePolicy parkingFeePolicy=parkingFeePolicyRepository.findById(parkingFeePolicyId).orElse(null);
        if(parkingFeePolicy==null)throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);

        //할인권 조회(status=='ACTIVE')
        List<ParkingTicket> discountTicketRequestDtos=parkingTicketRepository.getValidTickets(parkingLog.getParkingLogId(), com.example.demo.domain.payment.ticketpolicy.enums.Status.ACTIVE);

        //요금 계산
        FeeCalculationRequestDto request=FeeCalculationRequestDto.builder()
                .parkingTime(parkingTime)
                .policy(parkingFeePolicy)
                .prepaidFee(parkingLog.getFee())
                .discountTicketRequestDtos(discountTicketRequestDtos)
                .vehicleNumber(parkingLog.getCarNumberSnapshot())
                .freeExitUntil(parkingLog.getFreeExitUntil())
                .build();

        return calculateBaseFee(request);
    }

    //EXIT_REQUESTED
    @Transactional
    public VehiclePaymentResponseDto requestPayment(ParkingLog parkingLog) {
        String carNumber = parkingLog.getCarNumberSnapshot();
        Long parkingFeePolicyId = parkingLog.getParkingFeePolicyId();
        ParkingTypeSnapshot parkingTypeSnapshot = parkingLog.getParkingTypeSnapshot();
        LocalDateTime calculationStartTime=parkingLog.getEnteredAt();
        long parkingLogId=parkingLog.getParkingLogId();
        //주차 시간 계산(화면 표시용)
        LocalDateTime exitTime=LocalDateTime.now();;
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
            return VehiclePaymentResponseDto.builder().isFree(true).rawFee(0).parkingLogId(parkingLogId).vehicleNumber(carNumber).parkingTime(parkingTime).build();
        }

        // 반환
        return VehiclePaymentResponseDto.builder()
                .isFree(false)
                .vehicleNumber(carNumber)
                .parkingTime(parkingTime)
                .rawFee(feeCalculationResponseDto.getRawFee())
                .calculatedFee(feeCalculationResponseDto.getCalculatedFee())
                .amountToPay(feeCalculationResponseDto.getAmountToPay())
                .parkingLogId(parkingLog.getParkingLogId())
                .stackableTicketResult(feeCalculationResponseDto.getStackableTicketResult())
                .message("결제가 필요합니다.")
                .totalDiscountMinutes(feeCalculationResponseDto.getTotalDiscountMinutes())
                .totalDiscountAmount(feeCalculationResponseDto.getTotalDiscountAmount())
                .build();
    }
}

