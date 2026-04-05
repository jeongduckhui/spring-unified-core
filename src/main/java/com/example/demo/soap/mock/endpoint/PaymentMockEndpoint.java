package com.example.demo.soap.mock.endpoint;

import com.example.demo.soap.mock.dto.PaymentRequest;
import com.example.demo.soap.payment.dto.SoapPaymentResponse;
import org.springframework.ws.server.endpoint.annotation.*;

@Endpoint
public class PaymentMockEndpoint {

    private static final String NAMESPACE = "http://external.system/payment";

    @PayloadRoot(namespace = NAMESPACE, localPart = "paymentRequest")
    @ResponsePayload
    public SoapPaymentResponse process(@RequestPayload PaymentRequest request) {

        SoapPaymentResponse response = new SoapPaymentResponse();

        response.setResult("SUCCESS");
        response.setMessage("MOCK PAYMENT SUCCESS");
        response.setExternalId("PAY-" + System.currentTimeMillis());

        return response;
    }
}