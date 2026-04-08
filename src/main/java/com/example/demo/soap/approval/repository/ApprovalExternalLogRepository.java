package com.example.demo.soap.approval.repository;

import com.example.demo.soap.approval.domain.ApprovalExternalLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApprovalExternalLogRepository extends JpaRepository<ApprovalExternalLog, Long> {

    Optional<ApprovalExternalLog> findTopByApprovalIdAndStatusOrderByCreatedAtDesc(
            Long approvalId,
            String status
    );
}