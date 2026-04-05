package com.example.demo.approval.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "approval_external_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ApprovalExternalLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long approvalId;

    @Column(columnDefinition = "TEXT")
    private String requestXml;

    @Column(columnDefinition = "TEXT")
    private String responseXml;

    private String status;        // PENDING / SUCCESS / FAIL
    private Integer retryCount;   // 재시도 횟수
    private String lastError;     // 마지막 에러
    private String externalId;    // 외부 시스템 ID

    private LocalDateTime createdAt;

    // ===============================
    // static factory
    // ===============================

    public static ApprovalExternalLog pending(Long approvalId, String requestXml) {
        return ApprovalExternalLog.builder()
                .approvalId(approvalId)
                .requestXml(requestXml)
                .status("PENDING")
                .retryCount(0)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static ApprovalExternalLog success(
            Long approvalId,
            String requestXml,
            String responseXml,
            String externalId
    ) {
        return ApprovalExternalLog.builder()
                .approvalId(approvalId)
                .requestXml(requestXml)
                .responseXml(responseXml)
                .status("SUCCESS")
                .retryCount(0)
                .externalId(externalId)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static ApprovalExternalLog fail(
            Long approvalId,
            String requestXml,
            String errorMessage,
            int retryCount
    ) {
        return ApprovalExternalLog.builder()
                .approvalId(approvalId)
                .requestXml(requestXml)
                .status("FAIL")
                .lastError(errorMessage)
                .retryCount(retryCount)
                .createdAt(LocalDateTime.now())
                .build();
    }
}