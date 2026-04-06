package com.example.demo.soap.saaj.v1.controller;

import com.example.demo.soap.saaj.v1.dto.SoapTestRequest;
import com.example.demo.soap.saaj.v1.service.SaajSoapService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test/saaj")
@RequiredArgsConstructor
public class SaajSoapController {

    private final SaajSoapService service;

    @PostMapping("/call")
    public String call(@RequestBody SoapTestRequest request) {
        return service.call(request.getCallUser());
    }
}