package com.example.demo.sample.mybatisbatch;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SaveResponseDto {

    private int insertCount;
    private int updateCount;
    private int deleteCount;
}