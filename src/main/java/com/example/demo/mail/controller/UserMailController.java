package com.example.demo.mail.controller;

import com.example.demo.mail.dto.UserMailRequest;
import com.example.demo.mail.service.UserMailFacadeService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
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
            @RequestPart(required = false) List<MultipartFile> files
    ) throws Exception {

        List<String> toList = objectMapper.readValue(
                to,
                new TypeReference<List<String>>() {}
        );

        UserMailRequest request = new UserMailRequest();
        request.setTo(toList);
        request.setSubject(subject);
        request.setContent(content);

        facade.send(request, files);
    }
}