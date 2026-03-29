package com.example.demo.mail.controller;

import com.example.demo.mail.dto.UserMailRequest;
import com.example.demo.mail.service.UserMailFacadeService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/mail/user")
public class UserMailController {

    private final UserMailFacadeService facade;
    private final ObjectMapper objectMapper;

    /**
     * 첨부파일 없는 개인 메일 발송
     */
    @PostMapping(consumes = "application/json")
    public void send(@RequestBody UserMailRequest request) {
        facade.send(request, null);
    }

    /**
     * 첨부파일 있는 개인 메일 발송
     */
    @PostMapping(consumes = "multipart/form-data")
    public void send(
            @RequestParam String to,
            @RequestParam String subject,
            @RequestParam String content,
            @RequestPart(required = false) List<MultipartFile> files,
            Authentication authentication,
            HttpServletRequest httpServletRequest
    ) throws Exception {

        List<String> toList = objectMapper.readValue(
                to,
                new TypeReference<List<String>>() {}
        );

        Long userId = Long.valueOf(authentication.getName());

        UserMailRequest request = new UserMailRequest();
        request.setTo(toList);
        request.setSubject(subject);
        request.setContent(content);

        request.setUserId(userId);
        request.setIp(httpServletRequest.getRemoteAddr());
        request.setUserAgent(httpServletRequest.getHeader("User-Agent"));
        request.setDeviceId(httpServletRequest.getHeader("X-Device-Id"));

        facade.send(request, files);
    }
}