package com.example.demo.commoncode.controller;

import com.example.demo.commoncode.service.CommonCodeCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/common-code/cache")
@RequiredArgsConstructor
public class CommonCodeCacheController {

    private final CommonCodeCacheService cacheService;

    @DeleteMapping("/{groupCode}")
    public void evict(@PathVariable String groupCode) {
        cacheService.evict(groupCode);
    }

    @DeleteMapping("/all")
    public void evictAll() {
        cacheService.evictAll();
    }
}