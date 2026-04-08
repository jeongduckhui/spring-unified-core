package com.example.demo.transactionlog.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "transaction_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionLog {

    /**
     * 트랜잭션 아이디
     */
    @Id
    @Column(name = "request_id", length = 100)
    private String requestId;

    /**
     * 세션 아이디
     */
    @Column(name = "session_id", length = 100)
    private String sessionId;

    /**
     * 화면 요청일시
     */
    @Column(name = "screen_request_at")
    private LocalDateTime screenRequestAt;

    /**
     * 서버 요청일시
     */
    @Column(name = "server_request_at", nullable = false)
    private LocalDateTime serverRequestAt;

    /**
     * 서버 응답일시
     */
    @Column(name = "server_response_at")
    private LocalDateTime serverResponseAt;

    /**
     * 프로그램 아이디
     */
    @Column(name = "program_id", length = 100)
    private String programId;

    /**
     * 프로그램명
     */
    @Column(name = "program_name", length = 200)
    private String programName;

    /**
     * 기능 아이디
     */
    @Column(name = "function_id", length = 100)
    private String functionId;

    /**
     * 서비스 아이디
     */
    @Column(name = "service_id", length = 100)
    private String serviceId;

    /**
     * 트랜잭션 타입
     */
    @Column(name = "transaction_type", length = 50)
    private String transactionType;

    /**
     * 에러 메시지
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * 메시지 코드
     */
    @Column(name = "message_code", length = 100)
    private String messageCode;

    /**
     * 메시지명
     */
    @Column(name = "message_name", length = 200)
    private String messageName;

    /**
     * 생성자
     */
    @Column(name = "created_by", length = 50)
    private String createdBy;

    /**
     * 생성일시
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}