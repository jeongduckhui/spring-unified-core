package com.example.demo.commoncode.controller;

import com.example.demo.commoncode.dto.CommonCodeCacheStatsDto;
import com.example.demo.commoncode.service.CommonCodeCacheMonitorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/common-code/cache")
@RequiredArgsConstructor
public class CommonCodeCacheMonitorController {

    private final CommonCodeCacheMonitorService monitorService;

    @GetMapping("/stats")
    public CommonCodeCacheStatsDto getStats() {
        return monitorService.getStats();
    }
}