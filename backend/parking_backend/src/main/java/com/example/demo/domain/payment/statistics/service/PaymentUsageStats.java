package com.example.demo.domain.payment.statistics.service;

import com.example.demo.domain.parking.log.ParkingLog;
import com.example.demo.domain.parking.log.repository.ParkingLogRepository;
import com.example.demo.domain.parking.policy.ParkingFeePolicy;
import com.example.demo.domain.parking.policy.repository.ParkingFeePolicyRepository;
import com.example.demo.domain.payment.Payment;
import com.example.demo.domain.payment.enums.PaymentMethod;
import com.example.demo.domain.payment.enums.PaymentStatus;
import com.example.demo.domain.payment.repository.PaymentRepository;
import com.example.demo.domain.payment.service.PaymentService;
import com.example.demo.domain.payment.statistics.dtos.internal.ParkingLogDiscountValueDto;
import com.example.demo.domain.payment.statistics.dtos.response.DailyRevenueDto;
import com.example.demo.domain.payment.statistics.dtos.response.RevenueAnalysisDto;
import com.example.demo.domain.payment.statistics.dtos.response.RevenueResponseDto;
import com.example.demo.domain.payment.statistics.dtos.response.SummaryStatsDto;
import com.example.demo.global.exception.BusinessException;
import com.example.demo.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Transactional
public class PaymentUsageStats {
    private final PaymentRepository paymentRepository;
    private final ParkingLogRepository parkingLogRepository;
    private final ParkingFeePolicyRepository parkingFeePolicyRepository;
    private final PaymentService paymentService;

    public RevenueAnalysisDto findPaymentMethodStats(LocalDate startDate, LocalDate endDate){
        LocalDateTime start=startDate.atStartOfDay();
        LocalDateTime end=endDate.atTime(LocalTime.MAX);

        //할인권 사용액 정산
        List<ParkingLog> parkingLogs=parkingLogRepository.findParkingLogWithPolicy(start,end);

        // - log 정책 가져오기
        List<Long> feeIds=parkingLogs.stream().map(ParkingLog::getParkingFeePolicyId).toList();
        List<ParkingFeePolicy> policies=parkingFeePolicyRepository.findAllById(feeIds);

        // - payment 내역 가져오기
        List<Long> logIds=parkingLogs.stream().map(ParkingLog::getParkingLogId).toList();
        List<PaymentStatus> paymentStatus=List.of(PaymentStatus.SUCCESS,PaymentStatus.REFUNDED);
        List<Payment> payments=paymentRepository.findPaymentsByParkinglogId(logIds,paymentStatus);

        // - 로그별로 할인금액, 할인 시간 추출
        List<ParkingLogDiscountValueDto> details= parkingLogs.stream().map(l->{
                long parkingLogId=l.getParkingLogId();
                //카드 결제금액 계산
                long card=getAmount(payments,parkingLogId,PaymentMethod.PAY,PaymentStatus.SUCCESS);
                long cardRefund=getAmount(payments,parkingLogId,PaymentMethod.PAY,PaymentStatus.REFUNDED);
                long cardAmount=card-cardRefund;

                //포인트 결제 금액 계산
                long point=getAmount(payments,parkingLogId,PaymentMethod.POINT,PaymentStatus.SUCCESS);
                long pointRefund=getAmount(payments,parkingLogId,PaymentMethod.POINT,PaymentStatus.REFUNDED);
                long pointAmount=point-pointRefund;

                // 할인권 할인 가치 계산
                // -할인 전 주차요금
                ParkingFeePolicy policy=policies.stream()
                        .filter(p->p.getId().equals(l.getParkingFeePolicyId())).findFirst().orElseThrow(()->new BusinessException(ErrorCode.INVALID_REQUEST));
                long parkingTime=calculateBillableTime(l.getEnteredAt(),l.getExitedAt(),policy);
                int rawFee=calculatedFee(parkingTime,policy);
                // -할인 후 주차요금
                long discountedTime= Math.max(0,parkingTime-l.getTotalDiscountMinutes());
                int discountedFee=calculatedFee(discountedTime,policy);
                int discountedValue=Math.max(0,rawFee-discountedFee)+l.getTotalDiscountAmount();;

                return ParkingLogDiscountValueDto.builder()
                        .date(l.getExitedAt().toLocalDate())
                        .parkingLogId(l.getParkingLogId())
                        .ticketUsedAmount(discountedValue)
                        .payAmount(cardAmount)
                        .pointAmount(pointAmount)
                        .totalAmount(discountedValue+cardAmount+pointAmount)
                        .build();
            }).toList();
        
        // 날짜별 그룹화
        Map<LocalDate, List<ParkingLogDiscountValueDto>> groupByDate=details.stream().collect(Collectors.groupingBy(ParkingLogDiscountValueDto::getDate));
        // 그룹별 합계
        List<DailyRevenueDto> dailyRevenueDtos=new ArrayList<>();
        for (LocalDate date: groupByDate.keySet()){
            List<ParkingLogDiscountValueDto> list=groupByDate.get(date);
            long totalCard=0;
            long totalPoint=0;
            long totalTicket=0;
            long total=0;
            for(ParkingLogDiscountValueDto dto:list){
                totalCard+=dto.getPayAmount();
                totalPoint+=dto.getPointAmount();
                totalTicket+=dto.getTicketUsedAmount();
                total+=dto.getTotalAmount();
            }
            DailyRevenueDto dto=DailyRevenueDto.builder()
                    .date(date)
                    .pointAmount(totalPoint)
                    .payAmount(totalCard)
                    .ticketUsedAmount(totalTicket)
                    .totalAmount(total)
                    .build();
            dailyRevenueDtos.add(dto);
        }
        long amount=dailyRevenueDtos.stream().mapToLong(DailyRevenueDto::getTotalAmount).sum(); //총매출액
        long card=dailyRevenueDtos.stream().mapToLong(DailyRevenueDto::getPayAmount).sum(); //총카드 매출액
        long point=dailyRevenueDtos.stream().mapToLong(DailyRevenueDto::getPointAmount).sum(); //총 포인트 매출액
        long ticket=dailyRevenueDtos.stream().mapToLong(DailyRevenueDto::getTicketUsedAmount).sum(); //매출액 중 할인권 사용액
        dailyRevenueDtos.sort(Comparator.comparing(DailyRevenueDto::getDate).reversed()); //정렬
        return RevenueAnalysisDto.builder()
                .amount(amount)
                .card(card)
                .point(point)
                .ticket(ticket)
                .exitCount(parkingLogs.size())
                .dailyRevenue(dailyRevenueDtos)
                .build();
    }
    
    //카드& 포인트 결제 금액
    private long getAmount(List<Payment> payments, long parkingLogId,PaymentMethod paymentMethod,PaymentStatus paymentStatus){
        return payments.stream()
                .filter(p->p.getParkingLog().getParkingLogId().equals(parkingLogId))
                .filter(p->paymentMethod.equals(p.getPaymentMethod()))
                .filter(p->paymentStatus.equals(p.getPaymentStatus()))
                .mapToLong(Payment::getAmount)
                .sum();
    }

    //주차 요금 부과 시간 계산
    private long calculateBillableTime(LocalDateTime enteredAt, LocalDateTime exitedAt, ParkingFeePolicy policy ){
        long totalTime= Duration.between(enteredAt,exitedAt).toMinutes();
        return totalTime-policy.getGraceMinutes();
    }

    //주차 요금 계산
    private int calculatedFee(long parkingTime, ParkingFeePolicy policy){
        int dailyMaxFee=policy.getDailyMaxFee();
        int unitMinutes=policy.getUnitMinutes();
        int unitFee=policy.getUnitFee();
        int baseFee=policy.getBaseFee();
        return paymentService.calculatedrawFee(parkingTime,dailyMaxFee,unitMinutes,unitFee,baseFee);
    }

}
