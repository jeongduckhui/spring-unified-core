package com.example.demo.commoncode.controller;

import com.example.demo.commoncode.dto.CommonCodeDto;
import com.example.demo.commoncode.service.CommonCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/common-code")
@RequiredArgsConstructor
public class CommonCodeController {

    private final CommonCodeService service;

    @PostMapping("/batch")
    public Map<String, List<CommonCodeDto>> getCodes(
            @RequestBody List<String> groupCodes
    ) {
        return service.getCodes(groupCodes);
    }
}
