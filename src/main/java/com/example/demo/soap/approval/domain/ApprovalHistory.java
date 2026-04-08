package com.example.demo.soap.approval.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "approval_history")
@Getter
@NoArgsConstructor
public class ApprovalHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "approval_id", nullable = false)
    private Long approvalId;

    @Column(nullable = false)
    private String action;

    @Column(nullable = false)
    private String status;

    @Column(length = 1000)
    private String message;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public static ApprovalHistory create(
            Long approvalId,
            String action,
            String status,
            String message
    ) {
        ApprovalHistory history = new ApprovalHistory();
        history.approvalId = approvalId;
        history.action = action;
        history.status = status;
        history.message = message;
        history.createdAt = LocalDateTime.now();
        return history;
    }
}