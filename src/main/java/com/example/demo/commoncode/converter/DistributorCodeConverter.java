package com.example.demo.commoncode.converter;

import com.example.demo.commoncode.domain.DistributorCode;
import com.example.demo.commoncode.dto.CommonCodeDto;

import java.util.List;

public class DistributorCodeConverter {

    public static List<CommonCodeDto> toDto(List<DistributorCode> list) {
        return list.stream()
                .map(d -> new CommonCodeDto(
                        d.getDistributorCd(),
                        d.getDistributorName()
                ))
                .toList();
    }
}