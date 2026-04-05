package com.example.demo.soap.util;

import com.example.demo.soap.config.SoapProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.ws.soap.SoapHeader;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.SoapMessage;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import javax.xml.transform.dom.DOMResult;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class SoapHeaderUtil {

    private final SoapProperties soapProperties;

    /**
     * SOAP Header 생성
     *
     * 최종 생성되는 XML 구조:
     *
     * <soapenv:Header>
     *     <auth:AuthHeader xmlns:auth="http://example.com/auth">
     *         <auth:systemId>...</auth:systemId>
     *         <auth:authToken>...</auth:authToken>
     *         <auth:timestamp>...</auth:timestamp>
     *     </auth:AuthHeader>
     * </soapenv:Header>
     */
    public void addHeader(SoapMessage soapMessage) {

        try {
            /**
             * SOAP Header 영역 가져오기
             *
             * <soapenv:Header> ... </soapenv:Header>
             */
            SoapHeader header = soapMessage.getSoapHeader();

            /**
             * namespace, prefix 설정
             *
             * namespace:
             *   XML에서 해당 요소가 어떤 스키마/시스템에 속하는지 정의
             *   예: xmlns:auth="http://example.com/auth"
             *
             * prefix:
             *   namespace를 축약해서 표현하는 별칭
             *   예: auth:systemId
             */
            String namespace = soapProperties.getHeader().getNamespace();
            String prefix = soapProperties.getHeader().getPrefix();

            /**
             * QName 생성
             *
             * QName = XML 태그의 정식 이름
             *
             * 구성:
             *   namespace + localName + prefix
             *
             * 결과 XML:
             *   <auth:AuthHeader xmlns:auth="http://example.com/auth">
             */
            QName rootName = new QName(namespace, "AuthHeader", prefix);

            /**
             * Header에 Root Element 추가
             *
             * 생성되는 구조:
             * <soapenv:Header>
             *     <auth:AuthHeader xmlns:auth="http://example.com/auth">
             *     </auth:AuthHeader>
             * </soapenv:Header>
             */
            SoapHeaderElement rootElement = header.addHeaderElement(rootName);

            /**
             * DOMResult 획득
             *
             * rootElement 내부를 DOM 방식으로 조작하기 위한 객체
             * (XML을 트리 구조로 다루기 위한 컨테이너)
             */
            DOMResult domResult = (DOMResult) rootElement.getResult();

            /**
             * 현재 Element(Node) 가져오기
             *
             * 주의:
             * Document 전체가 아니라
             * <auth:AuthHeader> 요소 하나를 가리킴
             */
            Node rootNode = domResult.getNode();

            /**
             * Node → Element 캐스팅
             *
             * 이후 appendChild 등을 통해 자식 태그 추가 가능
             */
            Element root = (Element) rootNode;

            /**
             * 자식 요소 추가
             *
             * 최종 생성:
             * <auth:systemId>...</auth:systemId>
             */
            appendChild(
                    root,
                    namespace,
                    prefix,
                    "systemId",
                    soapProperties.getHeader().getSystemId()
            );

            /**
             * <auth:authToken>...</auth:authToken>
             */
            appendChild(
                    root,
                    namespace,
                    prefix,
                    "authToken",
                    soapProperties.getHeader().getAuthToken()
            );

            /**
             * <auth:timestamp>...</auth:timestamp>
             *
             * ISO_LOCAL_DATE_TIME 형식 사용
             */
            appendChild(
                    root,
                    namespace,
                    prefix,
                    "timestamp",
                    LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );

        } catch (Exception e) {
            throw new RuntimeException("SOAP Header 생성 실패", e);
        }
    }

    /**
     * 자식 Element 생성 및 추가
     *
     * parent 아래에 다음 구조를 추가:
     *
     * <prefix:name>value</prefix:name>
     *
     * 예:
     * <auth:systemId>SYSTEM_001</auth:systemId>
     */
    private void appendChild(
            Element parent,
            String namespace,
            String prefix,
            String name,
            String value
    ) {

        /**
         * namespace를 포함한 Element 생성
         *
         * createElementNS 사용 이유:
         * namespace가 포함된 XML을 생성하기 위해 필요
         *
         * 결과:
         * <auth:systemId>
         */
        Element child = parent.getOwnerDocument()
                .createElementNS(namespace, prefix + ":" + name);

        /**
         * 값 설정
         *
         * 결과:
         * <auth:systemId>SYSTEM_001</auth:systemId>
         */
        child.setTextContent(value);

        /**
         * 부모에 추가
         */
        parent.appendChild(child);
    }
}