package com.example.demo.mail.dto;

import com.example.demo.mail.domain.MailLog;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MailLogResponse {

    private Long id;
    private String recipient;
    private String subject;
    private String status;
    private int retryCount;
    private String errorMessage;
    private LocalDateTime sentAt;
    private LocalDateTime createdAt;

    public static MailLogResponse from(MailLog entity) {
        return MailLogResponse.builder()
                .id(entity.getId())
                .recipient(entity.getRecipient())
                .subject(entity.getSubject())
                .status(entity.getStatus().name())
                .retryCount(entity.getRetryCount())
                .errorMessage(entity.getErrorMessage())
                .sentAt(entity.getSentAt())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}