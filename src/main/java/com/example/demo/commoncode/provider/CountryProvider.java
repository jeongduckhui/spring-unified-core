package com.example.demo.commoncode.provider;

import com.example.demo.commoncode.converter.CountryConverter;
import com.example.demo.commoncode.dto.CommonCodeDto;
import com.example.demo.commoncode.repository.CountryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CountryProvider implements CommonCodeProvider {

    private final CountryRepository repository;

    @Override
    public String getGroupCode() {
        return "COUNTRY";
    }

    @Override
    public List<CommonCodeDto> getCodes() {
        return CountryConverter.toDto(
                repository.findByUseYn("Y")
        );
    }
}
