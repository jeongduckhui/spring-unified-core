package com.example.demo.commoncode.converter;

import com.example.demo.commoncode.domain.Country;
import com.example.demo.commoncode.dto.CommonCodeDto;

import java.util.List;

public class CountryConverter {

    public static List<CommonCodeDto> toDto(List<Country> list) {
        return list.stream()
                .map(c -> new CommonCodeDto(
                        c.getCountryCd(),
                        c.getCountryName()
                ))
                .toList();
    }
}