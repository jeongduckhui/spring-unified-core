package com.example.demo.mail.service;

import com.example.demo.mail.domain.MailType;
import com.example.demo.mail.dto.SystemMailRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SystemMailService {

    private final MailService mailService;

    // 테스트용 수신자 (항상 이걸로 발송)
    private static final List<String> TEST_RECIPIENTS = List.of(
            "jeongduckhui@gmail.com",
            "duckis-j@hanmail.net",
            "duckis_j79@naver.com"
    );

    public void send(SystemMailRequest request) {

        // 핵심: 무조건 하드코딩 수신자 사용
        List<String> recipients = TEST_RECIPIENTS;

        switch (request.getMailType()) {

            case AUTH -> sendAuth(recipients, request.getContent());
            case PASSWORD_RESET -> sendPasswordReset(recipients, request.getContent());
            case NOTIFICATION -> sendNotification(recipients, request.getContent());

            default -> throw new IllegalArgumentException("지원하지 않는 메일 타입");
        }
    }

    private void sendAuth(List<String> to, String url) {
        mailService.sendSystemMail(
                to,
                "이메일 인증 안내",
                url,
                MailType.AUTH
        );
    }

    private void sendPasswordReset(List<String> to, String url) {
        mailService.sendSystemMail(
                to,
                "비밀번호 재설정",
                url,
                MailType.PASSWORD_RESET
        );
    }

    private void sendNotification(List<String> to, String content) {
        mailService.sendSystemMail(
                to,
                "알림",
                content,
                MailType.NOTIFICATION
        );
    }
}