package com.example.demo.commoncode.converter;

import com.example.demo.commoncode.domain.ProcessCode;
import com.example.demo.commoncode.dto.CommonCodeDto;

import java.util.List;

public class ProcessCodeConverter {

    public static List<CommonCodeDto> toDto(List<ProcessCode> list) {
        return list.stream()
                .map(p -> new CommonCodeDto(
                        p.getProcessCd(),
                        p.getProcessName()
                ))
                .toList();
    }
}