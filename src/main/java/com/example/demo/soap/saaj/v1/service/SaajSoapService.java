package com.example.demo.soap.saaj.v1.service;

import com.example.demo.soap.saaj.v1.client.SaajSoapClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SaajSoapService {

    private final SaajSoapClient client;

    public String call(String callUser) {
        return client.call(callUser);
    }
}