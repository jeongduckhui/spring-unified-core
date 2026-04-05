package com.example.demo.soap.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApprovalFallbackMessage {

    private Long approvalId;
    private String requestXml;
    private String errorMessage;
}