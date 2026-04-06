package com.example.demo.soap.saaj.v2.service;

import com.example.demo.soap.saaj.v2.builder.approve.ApproveRequestBuilder;
import com.example.demo.soap.saaj.v2.client.SaajSoapClientV2;
import com.example.demo.soap.saaj.v2.dto.approve.ApproveRequest;
import jakarta.xml.soap.SOAPMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * SOAP 호출 서비스 (v2)
 *
 * 역할:
 * - DTO → Builder → SOAPMessage 생성
 * - Client 호출
 * - 응답 반환
 */
@Service
@RequiredArgsConstructor
public class SaajSoapServiceV2 {

    private final SaajSoapClientV2 soapClient;

    /**
     * Approve SOAP 호출
     */
    public String callApprove() {

        try {
            // ================================
            // 1. DTO 생성 (테스트용)
            // ================================
            ApproveRequest req = new ApproveRequest();
            req.setCallUser("user1");
            req.setAmount("1000");

            // ================================
            // 2. Builder → SOAPMessage 생성
            // ================================
            SOAPMessage request = new ApproveRequestBuilder(req).build();

            // ================================
            // 3. SOAP 호출
            // ================================
            SOAPMessage response = soapClient.call(
                    request,
                    "http://localhost:8081/ws"
            );

            // ================================
            // 4. 간단 응답 처리
            // ================================
            return response.getSOAPBody().getTextContent();

        } catch (Exception e) {
            throw new RuntimeException("Approve SOAP 호출 실패", e);
        }
    }
}