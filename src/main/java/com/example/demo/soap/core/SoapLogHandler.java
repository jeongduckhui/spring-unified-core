package com.example.demo.soap.core;

public interface SoapLogHandler {

    void onSuccess(Long id, String requestXml, String responseXml);

    void onFail(Long id, String requestXml, String errorMessage);
}