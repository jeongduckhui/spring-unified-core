package com.example.demo.transactionlog.filter;

import com.example.demo.transactionlog.context.TransactionLogContext;
import com.example.demo.transactionlog.entity.TransactionLog;
import com.example.demo.transactionlog.repository.TransactionLogRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TransactionLogFilter extends OncePerRequestFilter {

    private final TransactionLogRepository repository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String requestId = UUID.randomUUID().toString();

        TransactionLog log = TransactionLog.builder()
                .requestId(requestId)
                .serverRequestAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();

        // ThreadLocal 저장
        TransactionLogContext.init(log);

        try {
            filterChain.doFilter(request, response);

            // 성공 기본값
            if (log.getMessageCode() == null) {
                log.setMessageCode("SUCCESS");
                log.setMessageName("정상 처리");
            }

        } catch (Exception e) {

            log.setErrorMessage(e.getMessage());

            throw e;

        } finally {

            log.setServerResponseAt(LocalDateTime.now());

            // DB 1번 저장
            repository.save(log);

            // 필수
            TransactionLogContext.clear();
        }
    }
}