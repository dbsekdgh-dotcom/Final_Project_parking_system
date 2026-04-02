package com.example.demo.domain.shared.payment.repository;

import com.example.demo.domain.shared.payment.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
