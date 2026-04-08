package com.example.demo.soap.approval.repository;

import com.example.demo.soap.approval.domain.Approval;
import com.example.demo.soap.approval.domain.ApprovalStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApprovalRepository extends JpaRepository<Approval, Long> {

    List<Approval> findByRequestUserId(Long requestUserId);

    List<Approval> findByStatus(ApprovalStatus status);

    Approval findByExternalApprovalId(String externalApprovalId);
}