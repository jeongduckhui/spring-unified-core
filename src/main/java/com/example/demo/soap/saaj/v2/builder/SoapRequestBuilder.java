package com.example.demo.soap.saaj.v2.builder;

import jakarta.xml.soap.SOAPMessage;

public interface SoapRequestBuilder {

    SOAPMessage build() throws Exception;
}