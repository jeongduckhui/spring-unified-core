package com.example.demo.payment.repository;

import com.example.demo.payment.domain.Payment;
import com.example.demo.payment.domain.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByUserId(Long userId);

    List<Payment> findByStatus(PaymentStatus status);

    Payment findByExternalPaymentId(String externalPaymentId);
}