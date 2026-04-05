package com.example.demo.soap.payment.client;

import com.example.demo.payment.domain.PaymentExternalLog;
import com.example.demo.payment.repository.PaymentExternalLogRepository;
import com.example.demo.soap.core.AbstractSoapClient;
import com.example.demo.soap.core.SoapLogHandler;
import com.example.demo.soap.payment.dto.SoapPaymentRequest;
import com.example.demo.soap.payment.dto.SoapPaymentResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.ws.client.core.WebServiceTemplate;

@Slf4j
@Component
public class PaymentSoapClient extends AbstractSoapClient {

    private final WebServiceTemplate template;
    private final PaymentExternalLogRepository logRepository;

    public PaymentSoapClient(
            @Qualifier("paymentTemplate") WebServiceTemplate template,
            PaymentExternalLogRepository logRepository
    ) {
        this.template = template;
        this.logRepository = logRepository;
    }

    /**
     * 결제 SOAP 호출
     */
    @CircuitBreaker(name = "paymentSoapCircuitBreaker", fallbackMethod = "fallback")
    @Retry(name = "paymentSoapRetry")
    public SoapPaymentResponse send(Long paymentId, SoapPaymentRequest request) {

        return callSoap(
                template,
                request,
                paymentId,
                new SoapLogHandler() {

                    @Override
                    public void onSuccess(Long id, String req, String res) {
                        logRepository.save(
                                PaymentExternalLog.success(id, req, res)
                        );
                    }

                    @Override
                    public void onFail(Long id, String req, String err) {
                        logRepository.save(
                                PaymentExternalLog.fail(id, req, err)
                        );
                    }
                }
        );
    }

    /**
     * 결제는 장애를 숨기면 안 되는 업무라고 가정
     * fallback 에서도 예외를 최종 전달
     */
    public SoapPaymentResponse fallback(Long paymentId, SoapPaymentRequest request, Throwable t) {

        log.error("PAYMENT SOAP FALLBACK", t);

        String errorMessage = extractErrorMessage(t);

        logRepository.save(
                PaymentExternalLog.fail(
                        paymentId,
                        "FALLBACK_TRIGGERED",
                        errorMessage
                )
        );

        throw new RuntimeException("결제 외부 시스템 장애", t);
    }
}