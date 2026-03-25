package com.example.demo.mail.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MailRetryScheduler {

    private final MailRetryBatchService mailRetryBatchService;

    @Scheduled(fixedDelay = 300000)
    public void retryFailedMails() {
        log.info("메일 재처리 배치 시작");
        mailRetryBatchService.retryFailedMails();
        log.info("메일 재처리 배치 종료");
    }
}