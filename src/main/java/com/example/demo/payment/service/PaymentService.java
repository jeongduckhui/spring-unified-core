package com.example.demo.payment.service;

import com.example.demo.payment.domain.Payment;
import com.example.demo.payment.repository.PaymentRepository;
import com.example.demo.soap.payment.client.PaymentSoapClient;
import com.example.demo.soap.payment.mapper.PaymentSoapMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentSoapClient soapClient;
    private final PaymentSoapMapper mapper;

    @Transactional
    public void requestPayment(Long paymentId) {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow();

        var request = mapper.toRequest(payment);

        try {

            var response = soapClient.send(paymentId, request);

            if ("SUCCESS".equals(response.getResult())) {
                payment.markProcessing(response.getExternalId());
            } else {
                payment.markFailed();
            }

        } catch (Exception e) {
            payment.markFailed();
        }
    }
}