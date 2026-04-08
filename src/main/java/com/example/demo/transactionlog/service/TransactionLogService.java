package com.example.demo.transactionlog.service;

import com.example.demo.transactionlog.entity.TransactionLog;
import com.example.demo.transactionlog.repository.TransactionLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TransactionLogService {

    private final TransactionLogRepository repository;

    /**
     * 요청 시작 시 로그 생성
     */
    public void create(String requestId) {

        TransactionLog log = TransactionLog.builder()
                .requestId(requestId)
                .serverRequestAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();

        repository.save(log);
    }

    /**
     * 정상 응답
     */
    public void success(String requestId, String messageCode, String messageName) {

        TransactionLog log = repository.findById(requestId)
                .orElseThrow();

        log.setServerResponseAt(LocalDateTime.now());
        log.setMessageCode(messageCode);
        log.setMessageName(messageName);

        repository.save(log);
    }

    /**
     * 에러 응답
     */
    public void fail(String requestId, String errorMessage) {

        TransactionLog log = repository.findById(requestId)
                .orElseThrow();

        log.setServerResponseAt(LocalDateTime.now());
        log.setErrorMessage(errorMessage);

        repository.save(log);
    }
}