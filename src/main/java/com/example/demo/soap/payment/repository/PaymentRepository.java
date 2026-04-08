package com.example.demo.soap.payment.repository;

import com.example.demo.soap.payment.domain.Payment;
import com.example.demo.soap.payment.domain.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByUserId(Long userId);

    List<Payment> findByStatus(PaymentStatus status);

    Payment findByExternalPaymentId(String externalPaymentId);
}