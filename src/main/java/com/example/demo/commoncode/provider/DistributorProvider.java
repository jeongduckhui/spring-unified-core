package com.example.demo.commoncode.provider;

import com.example.demo.commoncode.converter.DistributorCodeConverter;
import com.example.demo.commoncode.dto.CommonCodeDto;
import com.example.demo.commoncode.repository.DistributorCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DistributorProvider implements CommonCodeProvider {

    private final DistributorCodeRepository repository;

    @Override
    public String getGroupCode() {
        return "DISTRIBUTOR";
    }

    @Override
    public List<CommonCodeDto> getCodes() {
        return DistributorCodeConverter.toDto(
                repository.findByUseYnOrderBySortOrder("Y")
        );
    }
}
