package com.example.demo.mail.controller;

import com.example.demo.mail.dto.SystemMailRequest;
import com.example.demo.mail.service.SystemMailService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/mail/system")
public class SystemMailController {

    private final SystemMailService systemMailService;

    @PostMapping
    public void send(@RequestBody SystemMailRequest request) {
        systemMailService.send(request);
    }
}