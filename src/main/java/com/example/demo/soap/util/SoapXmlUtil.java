package com.example.demo.soap.util;

import org.springframework.ws.WebServiceMessage;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

/**
 * SOAP 메시지를 XML 문자열로 변환하는 유틸 클래스
 *
 * 역할:
 * - WebServiceMessage 객체를 문자열(XML)로 변환
 * - 요청/응답 로그 출력
 * - DB 저장용 XML 확보
 *
 * 사용 위치:
 * - AbstractSoapClient (요청/응답 XML 저장)
 * - SoapClientInterceptor (최종 요청 XML 로그)
 *
 * 특징:
 * - SOAP Envelope 전체를 문자열로 변환
 * - Header + Body 포함된 XML 생성 가능
 *
 * 예:
 * <soapenv:Envelope>
 *     <soapenv:Header>...</soapenv:Header>
 *     <soapenv:Body>...</soapenv:Body>
 * </soapenv:Envelope>
 */
public class SoapXmlUtil {

    /**
     * 인스턴스 생성 방지
     *
     * static 유틸 클래스이므로 객체 생성 불필요
     */
    private SoapXmlUtil() {
    }

    /**
     * SOAP 메시지를 XML 문자열로 변환
     *
     * @param message WebServiceMessage (SOAP 요청 또는 응답)
     * @return XML 문자열
     *
     * 동작 방식:
     * 1. WebServiceMessage.writeTo() 호출
     * 2. 내부 SOAP 메시지를 OutputStream으로 출력
     * 3. ByteArrayOutputStream에 저장
     * 4. 문자열로 변환하여 반환
     *
     * 중요 포인트:
     * - 이 메서드는 SOAP 전체 구조를 문자열로 변환함
     * - Header 포함 여부는 호출 시점에 따라 다름
     *
     * 예:
     * - AbstractSoapClient → Header 없는 상태 XML
     * - Interceptor → Header 포함 최종 XML
     *
     * 예외 처리:
     * - 변환 실패 시 "XML_CONVERT_ERROR" 반환
     */
    public static String toXml(WebServiceMessage message) {
        try {
            /**
             * XML 데이터를 메모리에 담기 위한 OutputStream
             */
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            /**
             * SOAP 메시지를 OutputStream으로 출력
             *
             * 내부적으로 SOAP Envelope 전체가 write됨
             */
            message.writeTo(out);

            /**
             * byte[] → String 변환 (UTF-8)
             */
            return out.toString(StandardCharsets.UTF_8);

        } catch (Exception e) {

            /**
             * XML 변환 실패 시 fallback 문자열 반환
             *
             * 로그 또는 DB 저장 시 문제 발생 방지
             */
            return "XML_CONVERT_ERROR";
        }
    }
}