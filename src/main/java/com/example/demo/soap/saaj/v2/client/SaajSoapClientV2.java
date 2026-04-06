package com.example.demo.soap.saaj.v2.client;

import jakarta.xml.soap.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component("saajSoapClientV2")
public class SaajSoapClientV2 {

    public SOAPMessage call(SOAPMessage request, String url) {

        try {
            log.info("SOAP REQUEST =====");
            request.writeTo(System.out);
            System.out.println();

            SOAPConnectionFactory factory = SOAPConnectionFactory.newInstance();
            SOAPConnection connection = factory.createConnection();

            SOAPMessage response = connection.call(request, url);

            log.info("SOAP RESPONSE =====");
            response.writeTo(System.out);
            System.out.println();

            connection.close();

            return response;

        } catch (Exception e) {
            throw new RuntimeException("SOAP 호출 실패", e);
        }
    }
}