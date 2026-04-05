package com.example.demo.soap.approval.client;

import com.example.demo.approval.domain.ApprovalExternalLog;
import com.example.demo.approval.repository.ApprovalExternalLogRepository;
import com.example.demo.soap.approval.dto.SoapApproveRequest;
import com.example.demo.soap.approval.dto.SoapApproveResponse;
import com.example.demo.soap.core.AbstractSoapClient;
import com.example.demo.soap.core.SoapLogHandler;
import com.example.demo.soap.kafka.ApprovalFallbackMessage;
import com.example.demo.soap.kafka.ApprovalKafkaProducer;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.ws.client.core.WebServiceTemplate;

import java.util.Optional;

@Slf4j
@Component
public class ApprovalSoapClient extends AbstractSoapClient {

    /**
     * SOAP 호출 실행 객체
     * - endpoint
     * - marshaller/unmarshaller
     * - interceptor 포함
     */
    private final WebServiceTemplate template;

    /**
     * 외부 연동 로그 저장 Repository
     */
    private final ApprovalExternalLogRepository logRepository;

    /**
     * 장애 시 Kafka로 메시지 전송
     */
    private final ApprovalKafkaProducer kafkaProducer;

    public ApprovalSoapClient(
            @Qualifier("approvalTemplate") WebServiceTemplate template,
            ApprovalExternalLogRepository logRepository,
            ApprovalKafkaProducer kafkaProducer
    ) {
        this.template = template;
        this.logRepository = logRepository;
        this.kafkaProducer = kafkaProducer;
    }

    /**
     * 승인 SOAP 호출 메서드
     *
     * 특징:
     * - Idempotency 보장
     * - Retry + CircuitBreaker 적용
     * - 공통 SOAP 엔진(AbstractSoapClient) 사용
     */
    @CircuitBreaker(name = "approvalSoapCircuitBreaker", fallbackMethod = "fallback")
    @Retry(name = "approvalSoapRetry")
    public SoapApproveResponse send(Long approvalId, SoapApproveRequest request) {

        /**
         * 1. Idempotency 체크
         *
         * 이미 성공한 요청이 있는 경우:
         * - SOAP 재호출 방지
         * - 기존 결과 반환
         */
        Optional<ApprovalExternalLog> existing =
                logRepository.findTopByApprovalIdAndStatusOrderByCreatedAtDesc(
                        approvalId,
                        "SUCCESS"
                );

        if (existing.isPresent()) {

            log.info("IDEMPOTENT HIT - SOAP CALL SKIP approvalId={}", approvalId);

            /**
             * 기존 결과 재구성
             */
            SoapApproveResponse response = new SoapApproveResponse();
            response.setResult("SUCCESS");
            response.setMessage("이미 처리된 요청");
            response.setExternalId(existing.get().getExternalId());

            return response;
        }

        /**
         * 2. 요청 시작 로그 저장
         *
         * 상태: PENDING
         */
        logRepository.save(
                ApprovalExternalLog.pending(approvalId, "[REQUEST_CAPTURED]")
        );

        /**
         * 3. SOAP 호출
         *
         * - AbstractSoapClient.callSoap() 실행
         * - 성공/실패 처리 로직은 Handler로 전달
         */
        return callSoap(
                template,
                request,
                approvalId,

                /**
                 * 성공/실패 처리 전략 정의
                 */
                new SoapLogHandler() {

                    /**
                     * 성공 시 실행
                     */
                    @Override
                    public void onSuccess(Long id, String req, String res) {

                        /**
                         * SUCCESS 로그 저장
                         * - 요청 XML
                         * - 응답 XML
                         * - externalId 추출
                         */
                        logRepository.save(
                                ApprovalExternalLog.success(
                                        id,
                                        req,
                                        res,
                                        extractExternalId(res)
                                )
                        );
                    }

                    /**
                     * 실패 시 실행
                     */
                    @Override
                    public void onFail(Long id, String req, String err) {

                        /**
                         * FAIL 로그 저장
                         * - 요청 XML
                         * - 에러 메시지
                         */
                        logRepository.save(
                                ApprovalExternalLog.fail(id, req, err, 1)
                        );
                    }
                }
        );
    }

    /**
     * CircuitBreaker fallback 메서드
     *
     * 실행 조건:
     * - Retry 실패
     * - CircuitBreaker OPEN 상태
     */
    public SoapApproveResponse fallback(Long approvalId, SoapApproveRequest request, Throwable t) {

        log.error("FALLBACK 진입");
        log.error("APPROVAL SOAP FALLBACK", t);

        /**
         * root cause 에러 메시지 추출
         */
        String errorMessage = extractErrorMessage(t);

        try {
            /**
             * 1. DB에 장애 로그 저장
             */
            logRepository.save(
                    ApprovalExternalLog.fail(
                            approvalId,
                            "FALLBACK_TRIGGERED",
                            errorMessage,
                            99
                    )
            );
        } catch (Exception e) {
            log.error("DB 저장 실패", e);
        }

        try {
            /**
             * 2. Kafka로 장애 이벤트 전송
             *
             * 목적:
             * - 비동기 재처리
             * - 장애 복구 대응
             */
            kafkaProducer.sendFallback(
                    new ApprovalFallbackMessage(
                            approvalId,
                            "[REQUEST_CAPTURED]",
                            errorMessage
                    )
            );
        } catch (Exception e) {
            log.error("Kafka 전송 실패", e);
        }

        /**
         * 3. 사용자 응답 반환
         *
         * 특징:
         * - 장애를 숨기고 FAIL 응답 반환
         */
        SoapApproveResponse response = new SoapApproveResponse();
        response.setResult("FAIL");
        response.setMessage("외부 승인 시스템 장애");
        response.setExternalId(null);

        return response;
    }

    /**
     * 응답 XML에서 externalId 추출
     *
     * 예:
     * <externalId>APR-12345</externalId>
     *
     * 단순 문자열 파싱 방식 사용
     */
    private String extractExternalId(String responseXml) {
        try {
            if (responseXml == null) return null;

            int start = responseXml.indexOf("<externalId>");
            int end = responseXml.indexOf("</externalId>");

            if (start == -1 || end == -1) return null;

            return responseXml.substring(start + 12, end);

        } catch (Exception e) {
            return null;
        }
    }
}