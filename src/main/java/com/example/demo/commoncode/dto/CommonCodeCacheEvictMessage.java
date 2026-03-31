package com.example.demo.commoncode.dto;

import com.example.demo.commoncode.redis.CommonCodeCacheEventType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CommonCodeCacheEvictMessage {

    private CommonCodeCacheEventType type;      // EVICT, EVICT_ALL
    private String groupCode; // CATEGORY, COUNTRY ...
}