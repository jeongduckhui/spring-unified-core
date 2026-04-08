package com.example.demo.soap.payment.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment_external_log")
@Getter
@NoArgsConstructor
public class PaymentExternalLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long paymentId;

    @Lob
    private String requestXml;

    @Lob
    private String responseXml;

    private String successYn;
    private String errorMessage;

    private LocalDateTime createdAt;

    public static PaymentExternalLog success(Long paymentId, String requestXml, String responseXml) {
        PaymentExternalLog log = new PaymentExternalLog();
        log.paymentId = paymentId;
        log.requestXml = requestXml;
        log.responseXml = responseXml;
        log.successYn = "Y";
        log.createdAt = LocalDateTime.now();
        return log;
    }

    public static PaymentExternalLog fail(Long paymentId, String requestXml, String errorMessage) {
        PaymentExternalLog log = new PaymentExternalLog();
        log.paymentId = paymentId;
        log.requestXml = requestXml;
        log.successYn = "N";
        log.errorMessage = errorMessage;
        log.createdAt = LocalDateTime.now();
        return log;
    }
}