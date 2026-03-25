package com.example.demo.mail.service;

import com.example.demo.mail.domain.MailLog;
import com.example.demo.mail.dto.SystemMailRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SystemMailFacadeService {

    private final MailLogService mailLogService;
    private final AsyncMailService asyncMailService;

    public void send(SystemMailRequest request) {

        if (request.getTo() == null || request.getTo().isEmpty()) {
            throw new IllegalArgumentException("수신자가 없습니다.");
        }

        for (String recipient : request.getTo()) {

            // 로그 생성 (수신자별)
            MailLog mailLog = mailLogService.create(
                    recipient,
                    resolveSubject(request),
                    request.getContent(),
                    request.getMailType()
            );

            // 핵심: 단일 수신자 request 생성
            SystemMailRequest singleRequest = new SystemMailRequest();
            singleRequest.setTo(List.of(recipient));
            singleRequest.setContent(request.getContent());
            singleRequest.setMailType(request.getMailType());

            // 비동기 발송
            asyncMailService.send(mailLog.getId(), singleRequest);
        }
    }

    private String resolveSubject(SystemMailRequest request) {
        return switch (request.getMailType()) {
            case AUTH -> "이메일 인증 안내";
            case PASSWORD_RESET -> "비밀번호 재설정";
            case NOTIFICATION -> "알림";
        };
    }
}