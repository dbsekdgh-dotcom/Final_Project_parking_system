package com.example.demo.domain.payment.repository;

import com.example.demo.domain.parking.log.ParkingLog;
import com.example.demo.domain.payment.Payment;
import com.example.demo.domain.payment.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByExternalPaymentId (String externalPaymentId);

    //특정 주차 로그에 대해 READY / FAILED 상태인 결제내역만 조회
    List<Payment> findAllByParkingLogAndPaymentStatusIn(ParkingLog parkingLog, Collection<PaymentStatus> paymentStatus);
    //반대로 SUCCESS, CANCELLED, REFUNDED가 아닌것만 조회
    List<Payment> findAllByParkingLogAndPaymentStatusNotIn(ParkingLog parkingLog, Collection<PaymentStatus> statuses);
    //ParkingLog 엔티티과 결제 상태를 조건으로 리스트 조회
    List<Payment> findAllByParkingLogAndPaymentStatus(ParkingLog parkingLog,PaymentStatus paymentStatus);



}
