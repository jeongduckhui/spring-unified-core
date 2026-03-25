package com.example.demo.mail.service;

import com.example.demo.mail.dto.SystemMailRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncMailService {

    private final MailService mailService;
    private final MailLogService mailLogService;

    @Async("mailExecutor")
    @Retryable(
            retryFor = Exception.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000)
    )
    public void send(Long mailLogId, SystemMailRequest request) {
        mailService.sendSystemMail(
                request.getTo(),
                resolveSubject(request),
                request.getContent(),
                request.getMailType()
        );

        mailLogService.markSuccess(mailLogId);
        log.info("메일 발송 성공 mailLogId={}, to={}", mailLogId, request.getTo());
    }

    @Recover
    public void recover(Exception e, Long mailLogId, SystemMailRequest request) {
        log.error("메일 최종 실패 mailLogId={}, to={}", mailLogId, request.getTo(), e);
        mailLogService.markFail(mailLogId, e.getMessage());
    }

    private String resolveSubject(SystemMailRequest request) {
        return switch (request.getMailType()) {
            case AUTH -> "이메일 인증 안내";
            case PASSWORD_RESET -> "비밀번호 재설정";
            case NOTIFICATION -> "알림";
        };
    }
}