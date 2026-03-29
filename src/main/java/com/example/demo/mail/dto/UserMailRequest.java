package com.example.demo.mail.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserMailRequest {

    private List<String> to;
    private String subject;
    private String content;
    private Long userId;
    private String ip;
    private String userAgent;
    private String deviceId;
}