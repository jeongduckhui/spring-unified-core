package com.example.demo.mail.service;

import com.example.demo.mail.dto.UserMailRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserMailFacadeService {

    private final UserMailService userMailService;

    public void send(UserMailRequest request) {
        send(request, null);
    }

    public void send(UserMailRequest request, List<MultipartFile> files) {

        if (request.getTo() == null || request.getTo().isEmpty()) {
            throw new IllegalArgumentException("수신자가 없습니다.");
        }

        userMailService.send(request, files);
    }
}