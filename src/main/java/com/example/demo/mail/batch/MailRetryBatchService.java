package com.example.demo.mail.batch;

import com.example.demo.mail.domain.MailLog;
import com.example.demo.mail.domain.MailSendStatus;
import com.example.demo.mail.dto.SystemMailRequest;
import com.example.demo.mail.repository.MailLogRepository;
import com.example.demo.mail.service.AsyncMailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailRetryBatchService {

    private final MailLogRepository mailLogRepository;
    private final AsyncMailService asyncMailService;

    public void retryFailedMails() {
        List<MailLog> targets = mailLogRepository.findRetryTargets(MailSendStatus.FAIL);

        if (targets.isEmpty()) {
            log.info("재처리 대상 메일 없음");
            return;
        }

        for (MailLog mailLog : targets) {
            if (!mailLog.canRetry()) {
                continue;
            }

            // SystemMailRequest로 변환
            SystemMailRequest request = new SystemMailRequest();
            request.setTo(List.of(mailLog.getRecipient()));
            request.setContent(mailLog.getContent());
            request.setMailType(mailLog.getMailType());

            asyncMailService.send(mailLog.getId(), request);
        }

        log.info("재처리 대상 메일 수={}", targets.size());
    }
}