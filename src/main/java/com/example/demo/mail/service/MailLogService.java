package com.example.demo.mail.service;

import com.example.demo.mail.config.MailProperties;
import com.example.demo.mail.domain.MailLog;
import com.example.demo.mail.domain.MailSendStatus;
import com.example.demo.mail.domain.MailType;
import com.example.demo.mail.dto.MailLogResponse;
import com.example.demo.mail.dto.MailLogSearchRequest;
import com.example.demo.mail.repository.MailLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MailLogService {

    private final MailLogRepository mailLogRepository;
    private final MailProperties mailProperties;

    @Transactional
    public MailLog create(String recipient, String subject, String content, MailType mailType) {
        MailLog mailLog = MailLog.create(
                recipient,
                subject,
                content,
                mailType,
                mailProperties.getMaxRetryCount()
        );
        return mailLogRepository.save(mailLog);
    }

    @Transactional
    public void markSuccess(Long mailLogId) {
        MailLog mailLog = mailLogRepository.findById(mailLogId)
                .orElseThrow(() -> new IllegalArgumentException("메일 로그가 없습니다. id=" + mailLogId));

        mailLog.markSuccess();
    }

    @Transactional
    public void markFail(Long mailLogId, String errorMessage) {
        MailLog mailLog = mailLogRepository.findById(mailLogId)
                .orElseThrow(() -> new IllegalArgumentException("메일 로그가 없습니다. id=" + mailLogId));

        mailLog.markFail(errorMessage);
    }

    public List<MailLogResponse> search(MailLogSearchRequest request) {

        MailSendStatus status = null;

        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            status = MailSendStatus.valueOf(request.getStatus());
        }

        return mailLogRepository.search(
                        request.getRecipient(),
                        request.getSubject(),
                        status
                ).stream()
                .map(MailLogResponse::from)
                .toList();
    }
}