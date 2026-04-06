package com.example.demo.soap.saaj.v2.builder.sales;

import com.example.demo.soap.saaj.v2.builder.SoapRequestBuilder;
import com.example.demo.soap.saaj.v2.dto.sales.SalesRequest;
import jakarta.xml.soap.*;

public class SalesRequestBuilder implements SoapRequestBuilder {

    private final SalesRequest request;

    public SalesRequestBuilder(SalesRequest request) {
        this.request = request;
    }

    @Override
    public SOAPMessage build() throws Exception {

        MessageFactory factory = MessageFactory.newInstance();
        SOAPMessage message = factory.createMessage();

        SOAPPart part = message.getSOAPPart();
        SOAPEnvelope envelope = part.getEnvelope();

        envelope.addNamespaceDeclaration("sale", "http://external.system/sales");

        SOAPBody body = envelope.getBody();

        SOAPElement root = body.addChildElement("SalesRequest", "sale");

        root.addChildElement("SALES_AMOUNT")
                .addTextNode(String.valueOf(request.getSalesAmount()));

        root.addChildElement("SALES_PRICE")
                .addTextNode(String.valueOf(request.getSalesPrice()));

        root.addChildElement("SELLER")
                .addTextNode(request.getSeller());

        message.saveChanges();
        return message;
    }
}