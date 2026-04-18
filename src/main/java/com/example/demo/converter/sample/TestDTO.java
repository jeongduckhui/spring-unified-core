package com.example.demo.converter.sample;

import com.example.demo.converter.annotation.CsvToList;
import lombok.Data;

import java.util.List;

@Data
public class TestDTO {

    private String userId;

    // 기본 (String, ",")
    @CsvToList
    private List<String> codes;

    // 구분자 변경
    @CsvToList(delimiter = "|")
    private List<String> codes2;

    // Integer 변환
    @CsvToList(type = Integer.class)
    private List<Integer> ids;

    // 둘 다 적용
    @CsvToList(delimiter = "|", type = Integer.class)
    private List<Integer> ids2;
}
