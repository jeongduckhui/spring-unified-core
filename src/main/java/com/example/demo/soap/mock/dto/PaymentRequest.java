package com.example.demo.soap.mock.dto;

import jakarta.xml.bind.annotation.*;
import lombok.Getter;
import lombok.Setter;

@XmlRootElement(name = "paymentRequest", namespace = "http://external.system/payment")
@XmlAccessorType(XmlAccessType.FIELD)
@Getter
@Setter
public class PaymentRequest {

    private String paymentId;
    private String userId;
    private String amount;
    private String currency;
}