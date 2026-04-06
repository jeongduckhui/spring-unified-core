package com.example.demo.soap.saaj.v2.builder.approve;

import com.example.demo.soap.saaj.v2.builder.SoapRequestBuilder;
import com.example.demo.soap.saaj.v2.dto.approve.ApproveRequest;
import jakarta.xml.soap.*;

public class ApproveRequestBuilder implements SoapRequestBuilder {

    private final ApproveRequest request;

    public ApproveRequestBuilder(ApproveRequest request) {
        this.request = request;
    }

    @Override
    public SOAPMessage build() throws Exception {

        MessageFactory factory = MessageFactory.newInstance();
        SOAPMessage message = factory.createMessage();

        SOAPPart part = message.getSOAPPart();
        SOAPEnvelope envelope = part.getEnvelope();

        envelope.addNamespaceDeclaration("smar", "http://external.system/approval");

        SOAPBody body = envelope.getBody();

        SOAPElement root = body.addChildElement("Request-GSCM-006", "smar");

        // 단일 필드
        if (request.getCallUser() != null) {
            root.addChildElement("CALL_USER")
                    .addTextNode(request.getCallUser());
        }

        if (request.getAmount() != null) {
            root.addChildElement("AMOUNT")
                    .addTextNode(request.getAmount());
        }

        // 반복 구조
        if (request.getItems() != null) {
            for (ApproveRequest.Item item : request.getItems()) {

                SOAPElement itemEl = root.addChildElement("ITEM");

                itemEl.addChildElement("NAME")
                        .addTextNode(item.getName());

                itemEl.addChildElement("PRICE")
                        .addTextNode(item.getPrice());
            }
        }

        message.saveChanges();
        return message;
    }
}