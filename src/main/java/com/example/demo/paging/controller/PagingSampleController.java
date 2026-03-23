package com.example.demo.paging.controller;

import com.example.demo.paging.dto.PagingSampleRequest;
import com.example.demo.paging.dto.PagingSampleResponse;
import com.example.demo.paging.dto.PagingSampleRow;
import com.example.demo.paging.service.PagingSampleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PagingSampleController {

    private final PagingSampleService pagingSampleService;

    @GetMapping("/paging-samples")
    public PagingSampleResponse<PagingSampleRow> getPagingSamples(PagingSampleRequest request) {
        return pagingSampleService.getPagingSampleList(request);
    }
}