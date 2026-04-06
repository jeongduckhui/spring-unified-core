package com.example.demo.soap.mock.endpoint;

import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;

/**
 * Mock SOAP Endpoint (SAAJ 테스트용)
 *
 * - Spring WS가 Envelope + Body를 자동 생성
 * - 여기서는 Body 내부 XML만 반환해야 한다
 */
@Endpoint
public class MockSoapSaajEndpoint {

    private static final String NAMESPACE_URI = "http://external.system/approval";

    /**
     * SOAP 요청 처리
     *
     * @param request SOAP 요청 XML
     * @return SOAP Body 내부에 들어갈 응답 XML
     */
    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "Request-GSCM-006")
    @ResponsePayload
    public Source handle(@RequestPayload Source request) {

        // Envelope, Body 제외하고 순수 Payload만 반환
        String responseXml = """
            <approveResponse xmlns="http://external.system/approval">
                <result>SUCCESS</result>
                <message>OK</message>
                <externalId>APR-123</externalId>
            </approveResponse>
        """;

        return new StreamSource(new StringReader(responseXml));
    }
}