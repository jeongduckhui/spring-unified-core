package com.example.demo.mail.controller;

import com.example.demo.mail.dto.*;
import com.example.demo.mail.service.MailLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/mail/logs")
public class MailLogController {

    private final MailLogService mailLogService;

    @GetMapping
    public List<MailLogResponse> search(MailLogSearchRequest request) {
        return mailLogService.search(request);
    }
}