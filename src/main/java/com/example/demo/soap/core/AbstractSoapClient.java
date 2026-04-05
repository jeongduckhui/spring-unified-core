package com.example.demo.soap.core;

import com.example.demo.soap.util.SoapXmlUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ws.client.core.WebServiceTemplate;

import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public abstract class AbstractSoapClient {

    /**
     * SOAP 공통 호출 메서드
     *
     * 역할:
     * 1. DTO → XML (marshal)
     * 2. SOAP 요청 전송 (WebServiceTemplate)
     * 3. XML → DTO (unmarshal)
     * 4. 요청/응답 XML 확보
     * 5. 성공/실패 시 로그 처리 (Handler 위임)
     *
     * 특징:
     * - 승인, 결제 등 모든 SOAP 호출 공통 엔진
     * - 업무별 로직은 SoapLogHandler로 분리
     */
    protected <T> T callSoap(
            WebServiceTemplate template,
            Object request,
            Long entityId,
            SoapLogHandler logHandler
    ) {

        /**
         * 요청/응답 XML 저장용
         *
         * 이유:
         * 람다 내부에서 값을 변경하기 위해 AtomicReference 사용
         */
        AtomicReference<String> requestXml = new AtomicReference<>("");
        AtomicReference<String> responseXml = new AtomicReference<>("");

        try {
            /**
             * SOAP 호출 실행
             *
             * 내부 흐름:
             * 1. requestCallback → 요청 XML 생성
             * 2. interceptor.handleRequest() → Header 추가
             * 3. HTTP 전송
             * 4. 응답 수신
             * 5. responseExtractor → 응답 처리
             */
            T response = (T) template.sendAndReceive(

                    /**
                     * [1] REQUEST 생성 단계
                     *
                     * - DTO → XML 변환 (marshal)
                     * - 아직 Header 없음 (Interceptor에서 추가됨)
                     */
                    message -> {

                        /**
                         * Java 객체 → XML 변환
                         *
                         * 예:
                         * SoapApproveRequest → <approveRequest> XML
                         */
                        template.getMarshaller().marshal(request, message.getPayloadResult());

                        /**
                         * 현재 시점 XML 저장
                         *
                         * 주의:
                         * - Header 포함 전 상태
                         * - DB 저장용 XML
                         */
                        requestXml.set(SoapXmlUtil.toXml(message));
                    },

                    /**
                     * [2] RESPONSE 처리 단계
                     *
                     * - SOAP 서버 응답 수신 후 실행됨
                     */
                    message -> {

                        /**
                         * 응답 XML 문자열 확보
                         */
                        String xml = SoapXmlUtil.toXml(message);
                        responseXml.set(xml);

                        /**
                         * 응답 XML 로그 출력
                         *
                         * 응답은 이미 최종 상태이므로 여기서 로그 출력 가능
                         */
                        log.info("SOAP RESPONSE XML = {}", xml);

                        /**
                         * XML → Java 객체 변환 (unmarshal)
                         *
                         * 예:
                         * <approveResponse> → SoapApproveResponse
                         */
                        return template.getUnmarshaller().unmarshal(message.getPayloadSource());
                    }
            );

            /**
             * 성공 시 로그 처리
             *
             * - DB 저장 로직은 Handler에 위임
             * - 공통 엔진은 저장 방식 모름
             */
            logHandler.onSuccess(entityId, requestXml.get(), responseXml.get());

            return response;

        } catch (Exception e) {

            /**
             * 공통 에러 로그
             */
            log.error("SOAP ERROR", e);

            /**
             * 실패 로그 처리
             *
             * - root cause 메시지 추출 후 전달
             */
            logHandler.onFail(entityId, requestXml.get(), extractErrorMessage(e));

            /**
             * 중요:
             * 예외를 다시 던져야 Retry / CircuitBreaker 동작함
             */
            throw e;
        }
    }

    /**
     * 예외의 root cause 메시지 추출
     *
     * 목적:
     * - 중첩된 예외에서 실제 원인 추출
     * - DB 저장용 메시지로 사용
     */
    protected String extractErrorMessage(Throwable t) {

        if (t == null) {
            return "UNKNOWN_ERROR";
        }

        Throwable root = t;

        /**
         * 가장 안쪽 원인까지 이동
         */
        while (root.getCause() != null) {
            root = root.getCause();
        }

        return root.getMessage() != null ? root.getMessage() : root.toString();
    }
}