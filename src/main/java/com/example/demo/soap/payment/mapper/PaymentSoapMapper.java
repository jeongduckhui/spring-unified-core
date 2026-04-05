package com.example.demo.soap.payment.mapper;

import com.example.demo.payment.domain.Payment;
import com.example.demo.soap.payment.dto.SoapPaymentRequest;
import org.springframework.stereotype.Component;

@Component
public class PaymentSoapMapper {

    public SoapPaymentRequest toRequest(Payment payment) {
        SoapPaymentRequest req = new SoapPaymentRequest();
        req.setPaymentId(String.valueOf(payment.getId()));
        req.setUserId(String.valueOf(payment.getUserId()));
        req.setAmount(payment.getAmount().toString());
        req.setCurrency(payment.getCurrency());
        return req;
    }
}