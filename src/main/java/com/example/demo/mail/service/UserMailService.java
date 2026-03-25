package com.example.demo.mail.service;

import com.example.demo.auth.util.SsoUserProvider;
import com.example.demo.mail.dto.UserMailRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserMailService {

    private final MailService mailService;
    private final SsoUserProvider ssoUserProvider;

    public void send(UserMailRequest request) {
        send(request, null);
    }

    public void send(UserMailRequest request, List<MultipartFile> files) {

        if (request.getTo() == null || request.getTo().isEmpty()) {
            throw new IllegalArgumentException("수신자가 없습니다.");
        }

        String from = ssoUserProvider.getCurrentUser().getEmail();

        mailService.sendUserMailWithAttachment(
                from,
                request.getTo(),
                request.getSubject(),
                request.getContent(),
                files
        );
    }
}