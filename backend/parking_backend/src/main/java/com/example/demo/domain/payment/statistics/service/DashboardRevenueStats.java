package com.example.demo.domain.payment.statistics.service;

import com.example.demo.domain.parking.policy.enums.ParkingType;
import com.example.demo.domain.payment.Payment;
import com.example.demo.domain.payment.enums.PaymentMethod;
import com.example.demo.domain.payment.enums.PaymentStatus;
import com.example.demo.domain.payment.enums.PaymentType;
import com.example.demo.domain.payment.repository.PaymentRepository;
import com.example.demo.domain.payment.statistics.dtos.internal.DailyRevenueByTypeDto;
import com.example.demo.domain.payment.statistics.dtos.response.DailyDetailDto;
import com.example.demo.domain.payment.statistics.dtos.response.DashboardMonthlyRevenueDto;
import com.example.demo.domain.payment.statistics.dtos.response.DashboardRevenueResponseDto;
import com.example.demo.domain.payment.statistics.dtos.response.SummaryStatsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class DashboardRevenueStats {
    private final PaymentRepository paymentRepository;

    public DashboardRevenueResponseDto getDashboardTotalRevenue(String type){
        LocalDate today=LocalDate.now();
        //이번달
        LocalDateTime firstDay=today.with(TemporalAdjusters.firstDayOfMonth()).atStartOfDay();
        LocalDateTime lastDay=today.with(TemporalAdjusters.lastDayOfMonth()).atTime(LocalTime.MAX);
        SummaryStatsDto summary=paymentRepository.summaryStats(firstDay,lastDay,PaymentMethod.PAY.name(),PaymentMethod.POINT.name(), PaymentStatus.SUCCESS.name(),PaymentStatus.REFUNDED.name());
        long totalAmount=summary.getRevenue()-summary.getRefund();

        //이전달
        SummaryStatsDto lastSummary=paymentRepository.summaryStats(firstDay.minusMonths(1),lastDay.minusMonths(1),PaymentMethod.PAY.name(),PaymentMethod.POINT.name(), PaymentStatus.SUCCESS.name(),PaymentStatus.REFUNDED.name());
        long lastMonthTotalAmount=lastSummary.getRevenue()-lastSummary.getRefund();

        //증감율
        double changePercent=0.00;
        if(lastMonthTotalAmount!=0){
            double change=(totalAmount-lastMonthTotalAmount)/(double)lastMonthTotalAmount*100;
            changePercent=Math.round(change*100)/100.0;
        }else{
            changePercent=100.0;
        }

        return DashboardRevenueResponseDto.builder()
                .totalAmount(totalAmount)
                .changePercent(changePercent)
                .monthly(getMonthlyRevenue(type)) //월별 집계
                .build();
    }

    public List<DashboardMonthlyRevenueDto> getMonthlyRevenue(String type){
        LocalDate today=LocalDate.now();
        LocalDateTime firstDay=today.with(TemporalAdjusters.firstDayOfYear()).atStartOfDay();
        LocalDateTime lastDay=today.atTime(LocalTime.MAX);
        String successStatus=PaymentStatus.SUCCESS.name();
        String refundedStatus=PaymentStatus.REFUNDED.name();

        List<DailyRevenueByTypeDto> dailyPayments=null;
        if(type.equals("TOTAL")){
            dailyPayments=paymentRepository.dailyStatsTotal(firstDay,lastDay,successStatus,refundedStatus);
        }else{
            dailyPayments=
                    paymentRepository.dailyStatsByType(firstDay,today.atTime(LocalTime.MAX), type, PaymentStatus.SUCCESS.name(), PaymentStatus.REFUNDED.name());
        }

        //월별 집계
        Map<String,List<DailyRevenueByTypeDto>> map=dailyPayments.stream().collect(Collectors.groupingBy(p->p.getDate().format(DateTimeFormatter.ofPattern("M"))));
        List<DashboardMonthlyRevenueDto> result=new ArrayList<>();

        for(String month: map.keySet()){
            List<DailyRevenueByTypeDto> list=map.get(month);
            long amount=0;
            for(DailyRevenueByTypeDto dto:list){
                amount+=(dto.getRevenue()-dto.getRefund());
            }
             DashboardMonthlyRevenueDto monthlyRevenue=DashboardMonthlyRevenueDto.builder()
                    .month(month+"월")
                    .amount(amount)
                    .build();
            result.add(monthlyRevenue);
        }
        result.sort(Comparator.comparing(DashboardMonthlyRevenueDto::getMonth).reversed());
        return result;
    }
}
