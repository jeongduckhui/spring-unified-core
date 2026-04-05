package com.example.demo.soap.payment.dto;

import jakarta.xml.bind.annotation.*;
import lombok.Getter;
import lombok.Setter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
        name = "",
        propOrder = {
                "paymentId",
                "userId",
                "amount",
                "currency"
        }
)
@XmlRootElement(
        name = "paymentRequest",
        namespace = "http://external.system/payment"
)
@Getter
@Setter
public class SoapPaymentRequest {

    @XmlElement(name = "paymentId", required = true)
    private String paymentId;

    @XmlElement(name = "userId", required = true)
    private String userId;

    @XmlElement(name = "amount", required = true)
    private String amount;

    @XmlElement(name = "currency")
    private String currency;
}