package com.example.demo.soap.approval.repository;

import com.example.demo.soap.approval.domain.ApprovalHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApprovalHistoryRepository extends JpaRepository<ApprovalHistory, Long> {

    List<ApprovalHistory> findByApprovalId(Long approvalId);
}