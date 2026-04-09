package com.example.demo.domain.shared.payment.repository;

import com.example.demo.domain.shared.payment.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByExternalPaymentId (String externalPaymentId);
}
