package com.example.demo.mail.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "mail_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MailLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recipient", nullable = false, length = 255)
    private String recipient;

    @Column(name = "subject", nullable = false, length = 255)
    private String subject;

    @Lob
    @Column(name = "content", nullable = false)
    private String content;

    // 🔥 추가 (핵심)
    @Enumerated(EnumType.STRING)
    @Column(name = "mail_type", length = 50)
    private MailType mailType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private MailSendStatus status;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @Column(name = "max_retry_count", nullable = false)
    private int maxRetryCount;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // 생성 메서드 수정
    public static MailLog create(
            String recipient,
            String subject,
            String content,
            MailType mailType,
            int maxRetryCount
    ) {
        LocalDateTime now = LocalDateTime.now();

        return MailLog.builder()
                .recipient(recipient)
                .subject(subject)
                .content(content)
                .mailType(mailType)
                .status(MailSendStatus.PENDING)
                .retryCount(0)
                .maxRetryCount(maxRetryCount)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public void markSuccess() {
        this.status = MailSendStatus.SUCCESS;
        this.sentAt = LocalDateTime.now();
        this.errorMessage = null;
        this.updatedAt = LocalDateTime.now();
    }

    public void markFail(String errorMessage) {
        this.status = MailSendStatus.FAIL;
        this.retryCount++;
        this.errorMessage = trimMessage(errorMessage);
        this.updatedAt = LocalDateTime.now();
    }

    public boolean canRetry() {
        return this.retryCount < this.maxRetryCount;
    }

    public void markPending() {
        this.status = MailSendStatus.PENDING;
        this.updatedAt = LocalDateTime.now();
    }

    private String trimMessage(String message) {
        if (message == null) {
            return null;
        }
        return message.length() > 1000
                ? message.substring(0, 1000)
                : message;
    }
}