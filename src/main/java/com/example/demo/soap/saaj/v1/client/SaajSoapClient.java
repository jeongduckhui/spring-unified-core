package com.example.demo.soap.saaj.v1.client;

import com.example.demo.soap.saaj.v1.config.SaajSoapProperties;
import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.MimeHeaders;
import jakarta.xml.soap.SOAPBody;
import jakarta.xml.soap.SOAPConnection;
import jakarta.xml.soap.SOAPConnectionFactory;
import jakarta.xml.soap.SOAPElement;
import jakarta.xml.soap.SOAPEnvelope;
import jakarta.xml.soap.SOAPMessage;
import jakarta.xml.soap.SOAPPart;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * SAAJ 방식으로 SOAP 요청을 직접 생성하고 호출하는 클라이언트
 *
 * ================================
 * 전체 흐름
 * ================================
 * 1. SOAPMessage 생성
 * 2. SOAP Header / Body 구성
 * 3. SOAP 서버 호출
 * 4. SOAP 응답 수신
 * 5. 응답 데이터 추출
 *
 * ================================
 * XML ↔ Java 대응 핵심 개념
 * ================================
 * SOAP XML 구조:
 *
 * <soapenv:Envelope>
 *     <soapenv:Header/>
 *     <soapenv:Body>
 *         <smar:Request-GSCM-006>
 *             <CALL_USER>testUser</CALL_USER>
 *         </smar:Request-GSCM-006>
 *     </soapenv:Body>
 * </soapenv:Envelope>
 *
 * SAAJ Java 구조:
 *
 * SOAPMessage
 *   └── SOAPPart
 *         └── SOAPEnvelope
 *               ├── SOAPHeader
 *               └── SOAPBody
 *                     └── SOAPElement (Request-GSCM-006)
 *                           └── SOAPElement (CALL_USER)
 */
@Slf4j
@Component("saajSoapClientV1")
@RequiredArgsConstructor
public class SaajSoapClient {

    /**
     * SOAP 호출 관련 설정값
     * 예:
     * - endpoint URL
     * - SOAPAction
     * - namespace prefix
     * - namespace URI
     */
    private final SaajSoapProperties properties;

    /**
     * 외부 SOAP 서버를 호출한다.
     *
     * @param callUser SOAP 요청 Body 안에 들어갈 사용자 값
     * @return 응답 Body에서 추출한 문자열
     */
    public String call(String callUser) {

        try {
            // 1. 요청 SOAPMessage 생성
            SOAPMessage request = createSOAPRequest(callUser);

            // 2. 생성된 SOAP 요청 XML 로그 출력
            log.info("SOAP REQUEST =====");
            request.writeTo(System.out);
            System.out.println();

            // 3. SOAPConnection 생성 (SOAP 서버 연결)
            SOAPConnectionFactory factory = SOAPConnectionFactory.newInstance();
            SOAPConnection connection = factory.createConnection();

            // 4. SOAP 서버 호출
            SOAPMessage response = connection.call(
                    request,
                    properties.getSoapEndpointUrl()
            );

            // 5. 응답 SOAP XML 로그 출력
            log.info("SOAP RESPONSE =====");
            response.writeTo(System.out);
            System.out.println();

            // 6. 연결 종료
            connection.close();

            // 7. 응답 내용 추출 후 반환
            return extractResponse(response);

        } catch (Exception e) {
            // 모든 예외를 RuntimeException으로 감싸서 상위로 전달
            throw new RuntimeException("SOAP 호출 실패", e);
        }
    }

    /**
     * SOAP 요청 메시지를 생성한다.
     *
     * @param callUser SOAP Body에 들어갈 값
     * @return 완성된 SOAPMessage
     */
    private SOAPMessage createSOAPRequest(String callUser) throws Exception {

        // ================================
        // XML: <soapenv:Envelope>
        // ================================
        // SOAPMessage 생성을 위한 Factory 생성
        MessageFactory messageFactory = MessageFactory.newInstance();

        // 기본 SOAPMessage 생성
        // 이 시점에 기본 Envelope / Header / Body 골격이 생성된다.
        SOAPMessage soapMessage = messageFactory.createMessage();


        // ================================
        // HTTP Header 설정
        // ================================
        // MIME Header 객체 조회
        MimeHeaders headers = soapMessage.getMimeHeaders();

        // XML에는 없지만 HTTP Header로 들어감
        // 예: SOAPAction: SMARTGMS-GSCM-0022
        headers.addHeader("SOAPAction", properties.getSoapAction());


        // ================================
        // Body 내부 XML 생성
        // ================================
        // SOAP Envelope / Body 내부 구조 생성
        createSoapEnvelope(soapMessage, callUser);

        // 변경 내용 반영
        soapMessage.saveChanges();

        return soapMessage;
    }

    /**
     * SOAP Envelope / Body 내부에 실제 요청 Payload를 만든다.
     *
     * SOAP Body 구성
     *
     * ================================
     * XML ↔ Java 대응
     * ================================
     *
     * XML:
     *
     * <soapenv:Body>
     *     <smar:Request-GSCM-006>
     *         <CALL_USER>testUser</CALL_USER>
     *     </smar:Request-GSCM-006>
     * </soapenv:Body>
     *
     * Java:
     *
     * SOAPBody body = envelope.getBody();
     * SOAPElement request = body.addChildElement(...)
     * request.addChildElement("CALL_USER").addTextNode(...)
     *
     * 최종적으로 이런 구조가 생성된다:
     *
     * <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
     *                   xmlns:smar="http://external.system/approval">
     *     <soapenv:Header/>
     *     <soapenv:Body>
     *         <smar:Request-GSCM-006>
     *             <CALL_USER>testUser</CALL_USER>
     *         </smar:Request-GSCM-006>
     *     </soapenv:Body>
     * </soapenv:Envelope>
     *
     * @param soapMessage 생성 중인 SOAPMessage
     * @param callUser 요청 값
     */
    private void createSoapEnvelope(SOAPMessage soapMessage, String callUser) throws Exception {

        // ================================
        // XML: SOAP 전체 구조 접근
        // ================================
        // SOAPMessage의 큰 구조를 담당하는 SOAPPart 조회
        SOAPPart soapPart = soapMessage.getSOAPPart();


        // ================================
        // XML: <soapenv:Envelope>
        // ================================
        SOAPEnvelope envelope = soapPart.getEnvelope();


        // ================================
        // XML: xmlns:smar="http://external.system/approval"
        // ================================
        envelope.addNamespaceDeclaration(
                properties.getMyNamespace(),
                properties.getMyNamespaceURI()
        );


        // ================================
        // XML: <soapenv:Body>
        // ================================
        SOAPBody body = envelope.getBody();


        // ================================
        // XML: <smar:Request-GSCM-006>
        // ================================
        SOAPElement request = body.addChildElement(
                "Request-GSCM-006",
                properties.getMyNamespace()
        );


        /// ================================
        // XML: <CALL_USER>testUser</CALL_USER>
        // ================================
        request.addChildElement("CALL_USER")
                .addTextNode(callUser);
    }

    /**
     * SOAP 응답에서 필요한 값을 추출한다.
     *
     * 현재는 단순히 Body의 텍스트만 반환한다.
     * 실무에서는 다음 방식이 더 적절하다.
     * - 특정 태그를 찾아 값 추출
     * - DOM / JAXB / XPath 기반 파싱
     *
     * @param response SOAP 응답 메시지
     * @return SOAP Body의 텍스트 내용
     */
    private String extractResponse(SOAPMessage response) throws Exception {

        // ================================
        // XML: <soapenv:Body>
        // ================================
        SOAPBody body = response.getSOAPBody();

        // ================================
        // 단순 텍스트 추출
        // ================================
        // 예: SUCCESSOKAPR-123 처럼 이어붙여질 수 있음
        return body.getTextContent();
    }
}