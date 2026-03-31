package com.example.demo.commoncode.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CommonCodeCacheStatsDto {

    private long hitCount;
    private long missCount;
    private long successCount;
    private long evictionCount;
    private double hitRate;
    private long estimatedSize;
}