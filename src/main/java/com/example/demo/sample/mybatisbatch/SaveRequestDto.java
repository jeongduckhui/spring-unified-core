package com.example.demo.sample.mybatisbatch;

import lombok.Data;

import java.util.List;

@Data
public class SaveRequestDto {

    private List<SampleDto> added;
    private List<SampleDto> updated;
    private List<Long> deleted;
}