package com.example.demo.domain.shared.payment.repository;

import com.example.demo.domain.shared.parkinglog.ParkingLog;
import com.example.demo.domain.shared.payment.Payment;
import com.example.demo.domain.shared.payment.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByExternalPaymentId (String externalPaymentId);

    //특정 주차 로그에 대해 READY / FAILED 상태인 결제내역만 조회
    List<Payment> findAllByParkingLogAndPaymentStatusIn(ParkingLog parkingLog, Collection<PaymentStatus> paymentStatus);
    //반대로 SUCCESS, CANCELLED, REFUNDED가 아닌것만 조회
    List<Payment> findAllByParkingLogAndPaymentStatusNotIn(ParkingLog parkingLog, Collection<PaymentStatus> statuses);
}
