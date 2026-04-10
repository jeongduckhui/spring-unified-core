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

        // ⭐ 헤더 추출
        String svcId = request.getHeader("X-Svc-Id");
        String txType = request.getHeader("X-Transaction-Type");
        String loggableHeader = request.getHeader("X-Loggable");

        boolean loggable = "true".equalsIgnoreCase(loggableHeader);

        String uri = request.getRequestURI();

        TransactionLog log = TransactionLog.builder()
                .requestId(requestId)
//                .svcId(svcId)
                .transactionType(txType)
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

            // ⭐ 핵심: 로그 저장 여부 판단
            if (shouldLog(txType, loggable, uri)) {
                repository.save(log);
            }

            // 필수
            TransactionLogContext.clear();
        }
    }

    /**
     * 로그 저장 여부 판단
     */
    private boolean shouldLog(String txType, boolean loggable, String uri) {

        // 1️⃣ 프론트에서 제외한 경우
        if (!loggable) {
            return false;
        }

        // 2️⃣ transactionType 없음
        if (txType == null || txType.isBlank()) {
            return false;
        }

        // 3️⃣ 조회 / 저장만 허용
        if (!(txType.equals("SEARCH") || txType.equals("SAVE"))) {
            return false;
        }

        // 4️⃣ 시스템 API 제외 (콤보, 공통코드 등)
        if (uri.startsWith("/api/common-code")
                || uri.startsWith("/api/option")
                || uri.startsWith("/api/code")) {
            return false;
        }

        // 5️⃣ swagger / 정적 리소스 제외
        if (uri.contains("/swagger")
                || uri.contains("/v3/api-docs")
                || uri.contains("/h2-console")
                || uri.contains("/static")) {
            return false;
        }

        return true;
    }
}