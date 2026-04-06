package com.example.demo.soap.saaj.v2.controller;

import com.example.demo.soap.saaj.v2.service.SaajSoapServiceV2;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * SOAP 테스트 컨트롤러 (v2)
 */
@RestController
@RequiredArgsConstructor
public class SaajSoapControllerV2 {

    private final SaajSoapServiceV2 soapService;

    /**
     * Approve SOAP 테스트 API
     *
     * 호출:
     * GET http://localhost:8081/v2/soap/approve
     */
    @GetMapping("/test/soap/approve")
    public String approve() {
        return soapService.callApprove();
    }
}