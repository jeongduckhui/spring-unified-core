package com.example.demo.commoncode.provider;

import com.example.demo.commoncode.converter.ProcessCodeConverter;
import com.example.demo.commoncode.dto.CommonCodeDto;
import com.example.demo.commoncode.repository.ProcessCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ProcessCodeProvider implements CommonCodeProvider {

    private final ProcessCodeRepository repository;

    @Override
    public String getGroupCode() {
        return "PROCESS";
    }

    @Override
    public List<CommonCodeDto> getCodes() {
        return ProcessCodeConverter.toDto(
                repository.findByUseYnOrderBySortOrder("Y")
        );
    }
}