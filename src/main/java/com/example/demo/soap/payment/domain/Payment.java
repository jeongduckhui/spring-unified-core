package com.example.demo.soap.payment.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment")
@Getter
@NoArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private BigDecimal amount;

    private String currency;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @Column(name = "external_payment_id")
    private String externalPaymentId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public static Payment create(Long userId, BigDecimal amount) {
        Payment p = new Payment();
        p.userId = userId;
        p.amount = amount;
        p.currency = "KRW";
        p.status = PaymentStatus.REQUESTED;
        p.createdAt = LocalDateTime.now();
        p.updatedAt = LocalDateTime.now();
        return p;
    }

    public void markProcessing(String externalId) {
        this.status = PaymentStatus.PROCESSING;
        this.externalPaymentId = externalId;
        this.updatedAt = LocalDateTime.now();
    }

    public void markCompleted() {
        this.status = PaymentStatus.COMPLETED;
        this.updatedAt = LocalDateTime.now();
    }

    public void markFailed() {
        this.status = PaymentStatus.FAILED;
        this.updatedAt = LocalDateTime.now();
    }
}