package com.example.demo.soap.approval.dto;

import jakarta.xml.bind.annotation.*;
import lombok.Getter;
import lombok.Setter;

/**
 * ===============================
 * 1. XSD 정의 (응답 스키마 원본)
 * ===============================
 *
 * <xsd:element name="approveResponse">
 *     <xsd:complexType>
 *         <xsd:sequence>
 *             <xsd:element name="result" type="xsd:string"/>
 *             <xsd:element name="message" type="xsd:string"/>
 *             <xsd:element name="externalId" type="xsd:string"/>
 *         </xsd:sequence>
 *     </xsd:complexType>
 * </xsd:element>
 *
 *
 * ===============================
 * 2. 실제 SOAP 응답 XML
 * ===============================
 *
 * <approveResponse xmlns="http://external.system/approval">
 *     <result>SUCCESS</result>
 *     <message>OK</message>
 *     <externalId>APR-12345</externalId>
 * </approveResponse>
 *
 *
 * ===============================
 * 3. Java ↔ XML 매핑 설명
 * ===============================
 *
 * @XmlRootElement
 *   → <approveResponse> 루트 태그 매핑
 *   → namespace 반드시 동일해야 함
 *
 * @XmlType(propOrder)
 *   → XML element 순서 정의 (XSD sequence와 동일해야 함)
 *
 * @XmlElement(name="...")
 *   → XML 태그와 Java 필드 매핑
 *
 *
 * ===============================
 * 4. 동작 흐름 (callSoap 기준)
 * ===============================
 *
 * SOAP 서버 응답(XML)
 *   → unmarshaller
 *   → SoapApproveResponse 객체로 변환
 *
 * 예:
 * <approveResponse> XML → SoapApproveResponse 객체
 *
 *
 * ===============================
 * 5. 중요 포인트
 * ===============================
 *
 * - 요청(request)과 응답(response)은 WSDL 계약으로 쌍을 이룸
 * - namespace, element 이름, 순서가 틀리면 unmarshal 실패
 * - SOAP는 HTTP 200이어도 result 값으로 성공/실패를 판단하는 경우 많음
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
        name = "",
        propOrder = {
                "result",
                "message",
                "externalId"
        }
)
@XmlRootElement(
        name = "approveResponse",
        namespace = "http://external.system/approval"
)
@Getter
@Setter
public class SoapApproveResponse {

    /**
     * <result>SUCCESS</result>
     *
     * 결과 코드
     * 예:
     * - SUCCESS
     * - FAIL
     */
    @XmlElement(name = "result")
    private String result;

    /**
     * <message>OK</message>
     *
     * 결과 메시지
     */
    @XmlElement(name = "message")
    private String message;

    /**
     * <externalId>APR-12345</externalId>
     *
     * 외부 시스템에서 발급한 식별자
     */
    @XmlElement(name = "externalId")
    private String externalId;
}