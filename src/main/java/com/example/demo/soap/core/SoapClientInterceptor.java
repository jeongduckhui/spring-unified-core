package com.example.demo.soap.core;

import com.example.demo.soap.util.SoapHeaderUtil;
import com.example.demo.soap.util.SoapXmlUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.ws.client.WebServiceClientException;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapMessage;

@Slf4j
@Component
@RequiredArgsConstructor
public class SoapClientInterceptor implements ClientInterceptor {

    private final SoapHeaderUtil soapHeaderUtil;

    @Override
    public boolean handleRequest(MessageContext messageContext) {

        try {
            /**
             * 요청 XML 생성 완료 상태
             *
             * 이 시점:
             * - marshal 완료
             * - 아직 서버 전송 전
             */
            SoapMessage request = (SoapMessage) messageContext.getRequest();

            /**
             * [1] SOAP Header 추가
             *
             * 추가되는 구조:
             * <soapenv:Header>
             *   <auth:AuthHeader>
             *     ...
             *   </auth:AuthHeader>
             * </soapenv:Header>
             */
            soapHeaderUtil.addHeader(request);

            /**
             * [2] 최종 XML 로그 출력
             *
             * 특징:
             * - Header 포함된 최종 전송 XML
             * - 실제 서버로 나가는 데이터
             */
            String finalXml = SoapXmlUtil.toXml(request);
            log.info("SOAP FINAL REQUEST XML = {}", finalXml);

        } catch (Exception e) {
            log.error("SOAP REQUEST INTERCEPT ERROR", e);
        }

        /**
         * true → 계속 진행
         * false → 요청 중단
         */
        return true;
    }

    @Override
    public boolean handleResponse(MessageContext messageContext) {

        /**
         * 응답 처리 안 하는 이유:
         * - AbstractSoapClient에서 이미 처리
         * - 책임 분리
         */
        return true;
    }

    @Override
    public boolean handleFault(MessageContext messageContext) {

        try {
            /**
             * SOAP Fault 발생 시
             *
             * 예:
             * <soap:Fault>...</soap:Fault>
             */
            SoapMessage response = (SoapMessage) messageContext.getResponse();

            String xml = SoapXmlUtil.toXml(response);

            /**
             * Fault XML 로그 출력
             */
            log.error("SOAP FAULT XML = {}", xml);

        } catch (Exception e) {
            log.error("SOAP FAULT INTERCEPT ERROR", e);
        }

        return true;
    }

    @Override
    public void afterCompletion(MessageContext messageContext, Exception ex) throws WebServiceClientException {

        /**
         * 요청-응답 전체 완료 후 실행
         *
         * 현재 사용하지 않음
         */
    }
}