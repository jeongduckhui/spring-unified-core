package com.example.demo.approval.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "approval")
@Getter
@NoArgsConstructor
public class Approval {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_user_id", nullable = false)
    private Long requestUserId;

    @Column(nullable = false)
    private String title;

    @Lob
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApprovalStatus status;

    @Column(name = "external_approval_id")
    private String externalApprovalId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 생성 메서드
     */
    public static Approval create(Long requestUserId, String title, String content) {
        Approval approval = new Approval();
        approval.requestUserId = requestUserId;
        approval.title = title;
        approval.content = content;
        approval.status = ApprovalStatus.REQUESTED;
        approval.createdAt = LocalDateTime.now();
        approval.updatedAt = LocalDateTime.now();
        return approval;
    }

    /**
     * 외부 결재 요청 후 상태 변경
     */
    public void markProcessing(String externalId) {
        this.status = ApprovalStatus.PROCESSING;
        this.externalApprovalId = externalId;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 승인
     */
    public void markApproved() {
        this.status = ApprovalStatus.APPROVED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 반려
     */
    public void markRejected() {
        this.status = ApprovalStatus.REJECTED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 실패
     */
    public void markFailed() {
        this.status = ApprovalStatus.FAILED;
        this.updatedAt = LocalDateTime.now();
    }
}