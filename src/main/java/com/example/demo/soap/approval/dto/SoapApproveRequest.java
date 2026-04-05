package com.example.demo.soap.approval.dto;

import jakarta.xml.bind.annotation.*;
import lombok.Getter;
import lombok.Setter;

/**
 * ===============================
 * 1. XSD 정의 (스키마 원본)
 * ===============================
 *
 * <xsd:element name="approveRequest">
 *     <xsd:complexType>
 *         <xsd:sequence>
 *             <xsd:element name="docId" type="xsd:string"/>
 *             <xsd:element name="requesterId" type="xsd:string"/>
 *             <xsd:element name="title" type="xsd:string"/>
 *             <xsd:element name="content" type="xsd:string"/>
 *         </xsd:sequence>
 *     </xsd:complexType>
 * </xsd:element>
 *
 *
 * ===============================
 * 2. 실제 SOAP 요청 XML
 * ===============================
 *
 * <approveRequest xmlns="http://external.system/approval">
 *     <docId>1</docId>
 *     <requesterId>user1</requesterId>
 *     <title>제목</title>
 *     <content>내용</content>
 * </approveRequest>
 *
 *
 * ===============================
 * 3. Java ↔ XML 매핑 설명
 * ===============================
 *
 * @XmlRootElement
 *   → <approveRequest> 루트 태그 매핑
 *   → namespace 포함
 *
 * @XmlType(propOrder)
 *   → XML 태그 순서 정의 (sequence와 동일해야 함)
 *
 * @XmlElement(name="...")
 *   → 각 XML 태그와 필드 매핑
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
        name = "",
        propOrder = {
                "docId",
                "requesterId",
                "title",
                "content"
        }
)
@XmlRootElement(
        name = "approveRequest",
        namespace = "http://external.system/approval"
)
@Getter
@Setter
public class SoapApproveRequest {

    /**
     * <docId>1</docId>
     */
    @XmlElement(name = "docId", required = true)
    private String docId;

    /**
     * <requesterId>user1</requesterId>
     */
    @XmlElement(name = "requesterId", required = true)
    private String requesterId;

    /**
     * <title>제목</title>
     */
    @XmlElement(name = "title")
    private String title;

    /**
     * <content>내용</content>
     */
    @XmlElement(name = "content")
    private String content;
}