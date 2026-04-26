package com.example.demo.domain.payment.statistics.service;

import com.example.demo.domain.payment.enums.PaymentMethod;
import com.example.demo.domain.payment.enums.PaymentStatus;
import com.example.demo.domain.payment.enums.PaymentType;
import com.example.demo.domain.payment.repository.PaymentRepository;
import com.example.demo.domain.payment.statistics.dtos.response.DailyDetailDto;
import com.example.demo.domain.payment.statistics.dtos.response.DailyStatsDto;
import com.example.demo.domain.payment.statistics.dtos.response.RevenueResponseDto;
import com.example.demo.domain.payment.statistics.dtos.response.SummaryStatsDto;
import com.example.demo.domain.system.setting.SettingKey;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class RevenueStats {
    private final PaymentRepository paymentRepository;

    public RevenueResponseDto revenueReponse(LocalDate startDate, LocalDate endDate){
        LocalDateTime start=startDate.atStartOfDay();
        LocalDateTime end=endDate.atTime(LocalTime.MAX);
        String payMethod=PaymentMethod.PAY.name();
        String pointMethod=PaymentMethod.POINT.name();
        String successStatus=PaymentStatus.SUCCESS.name();
        String refundedStatus=PaymentStatus.REFUNDED.name();
        String parkingType= PaymentType.PARKING.name();
        String ticketType=PaymentType.TICKET.name();
        String subscriptionType=PaymentType.SUBSCRIPTION.name();

        //1. 상단 요약 카드
        SummaryStatsDto summary=paymentRepository.summaryStats(start,end,payMethod,pointMethod,successStatus,refundedStatus);
        // - 토스 수수료
        long commission=getCommission(summary.getPayAmount(),summary.getPayRefund());
        // - 포인트 결제액
        long pointAmount=summary.getPointAmount()-summary.getPointRefund();

        //2. 날짜별 상세액
        List<DailyDetailDto> dailyDetails=paymentRepository.dailyStats(start,end,parkingType,ticketType,subscriptionType,payMethod,pointMethod,successStatus,refundedStatus);
        List<DailyStatsDto> dailyStats=dailyDetails.stream().map(d->{
                // - 토스 수수료
                long toss=getCommission(d.getPayAmount(),d.getPayRefund());
                // - 포인트 결제액
                long pointDiscount=d.getPointAmount()-d.getPointRefund();
                return DailyStatsDto.builder()
                        .date(d.getDate())
                        .parkingRevenue(d.getParkingRevenue())
                        .ticketRevenue(d.getTicketRevenue())
                        .subscriptionRevenue(d.getSubscriptionRevenue())
                        .refund(d.getRefund())
                        .commission(toss)
                        .pointDiscount(pointDiscount)
                        .build();
            }).toList();

        return RevenueResponseDto.builder()
                .revenue(summary.getRevenue())
                .refund(summary.getRefund())
                .cost(commission+pointAmount)
                .net(summary.getRevenue()-summary.getRefund()-commission-pointAmount)
                .dailyStats(dailyStats)
                .build();
    }

    private long getCommission(long payAmount,long payRefund){
        double rate=SettingKey.PG_COMMISSION_RATE.getDefaultDoubleValue();
        long netPayAmount=payAmount-payRefund;
        return (long) Math.floor(netPayAmount* rate);
    }

}
