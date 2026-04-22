package com.example.demo.domain.statistics.service;

import com.example.demo.domain.parking.log.repository.ParkingLogRepository;
import com.example.demo.domain.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class RevenueProcessor {
    //매출기준은 결제 기준으로 계산합니다.
    private final ParkingLogRepository parkingLogRepository;
    private final PaymentRepository paymentRepository;

    //일반 차량 결제액


    //상가 할인권 판매액


    //정기권 판매액


}
