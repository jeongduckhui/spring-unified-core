package com.example.demo.soap.payment.dto;

import jakarta.xml.bind.annotation.*;
import lombok.Getter;
import lombok.Setter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
        name = "",
        propOrder = {
                "result",
                "message",
                "externalId"
        }
)
@XmlRootElement(
        name = "paymentResponse",
        namespace = "http://external.system/payment"
)
@Getter
@Setter
public class SoapPaymentResponse {

    @XmlElement(name = "result")
    private String result;

    @XmlElement(name = "message")
    private String message;

    @XmlElement(name = "externalId")
    private String externalId;
}