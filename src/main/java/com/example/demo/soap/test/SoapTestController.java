package com.example.demo.soap.test;

import com.example.demo.soap.approval.client.ApprovalSoapClient;
import com.example.demo.soap.approval.dto.SoapApproveRequest;
import com.example.demo.soap.approval.dto.SoapApproveResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SoapTestController {

    private final ApprovalSoapClient approvalSoapClient;

    @GetMapping("/test/soap")
    public SoapApproveResponse test() {

        SoapApproveRequest request = new SoapApproveRequest();
        request.setDocId("123");
        request.setRequesterId("user1");
        request.setTitle("테스트 결재");
        request.setContent("내용");

        return approvalSoapClient.send(1L, request);
    }
}