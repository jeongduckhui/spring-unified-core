package com.example.demo.soap.payment.repository;

import com.example.demo.soap.payment.domain.PaymentExternalLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentExternalLogRepository extends JpaRepository<PaymentExternalLog, Long> {
}