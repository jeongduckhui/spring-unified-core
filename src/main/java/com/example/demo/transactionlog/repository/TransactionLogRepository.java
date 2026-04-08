package com.example.demo.transactionlog.repository;

import com.example.demo.transactionlog.entity.TransactionLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionLogRepository extends JpaRepository<TransactionLog, String> {
}