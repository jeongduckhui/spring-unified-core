package com.example.demo.mail.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MailLogSearchRequest {

    private String recipient;   // 수신자
    private String subject;     // 제목
    private String status;      // SUCCESS / FAIL
}