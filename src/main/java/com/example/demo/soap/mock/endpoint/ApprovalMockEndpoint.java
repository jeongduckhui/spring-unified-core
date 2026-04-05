package com.example.demo.soap.mock.endpoint;

import com.example.demo.soap.approval.dto.SoapApproveResponse;
import com.example.demo.soap.mock.dto.ApprovalRequest;
import org.springframework.ws.server.endpoint.annotation.*;

@Endpoint
public class ApprovalMockEndpoint {

    private static final String NAMESPACE = "http://external.system/approval";

    @PayloadRoot(namespace = NAMESPACE, localPart = "approveRequest")
    @ResponsePayload
    public SoapApproveResponse process(@RequestPayload ApprovalRequest request) {

        SoapApproveResponse response = new SoapApproveResponse();

        response.setResult("SUCCESS");
        response.setMessage("MOCK APPROVAL SUCCESS");
        response.setExternalId("APR-" + System.currentTimeMillis());

        return response;
    }

    /*
    // Timeout 테스트용
    @PayloadRoot(namespace = NAMESPACE, localPart = "approveRequest")
    @ResponsePayload
    public SoapApproveResponse process(@RequestPayload ApprovalRequest request) {

        try {
            Thread.sleep(6000); //  6초 지연 (timeout 유도)
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        SoapApproveResponse response = new SoapApproveResponse();
        response.setResult("SUCCESS");
        response.setMessage("MOCK APPROVAL SUCCESS");
        response.setExternalId("APR-" + System.currentTimeMillis());

        return response;
    }
    */
}