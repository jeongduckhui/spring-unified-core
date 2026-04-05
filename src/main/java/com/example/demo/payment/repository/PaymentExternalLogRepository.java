package com.example.demo.payment.repository;

import com.example.demo.payment.domain.PaymentExternalLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentExternalLogRepository extends JpaRepository<PaymentExternalLog, Long> {
}