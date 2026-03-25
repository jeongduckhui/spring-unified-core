package com.example.demo.mail.dto;

import com.example.demo.mail.domain.MailType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SystemMailRequest {

    private List<String> to;   // 다수 수신자
    private String content;   // 링크 or 메시지
    private MailType mailType;
}