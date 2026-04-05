package com.example.demo.soap.mock.dto;

import jakarta.xml.bind.annotation.*;
import lombok.Getter;
import lombok.Setter;

@XmlRootElement(name = "approveRequest", namespace = "http://external.system/approval")
@XmlAccessorType(XmlAccessType.FIELD)
@Getter
@Setter
public class ApprovalRequest {

    private String docId;
    private String requesterId;
    private String title;
    private String content;
}