package com.example.demo.commoncode.provider;

import com.example.demo.commoncode.converter.CategoryConverter;
import com.example.demo.commoncode.dto.CommonCodeDto;
import com.example.demo.commoncode.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CategoryProvider implements CommonCodeProvider {

    private final CategoryRepository repository;

    @Override
    public String getGroupCode() {
        return "CATEGORY";
    }

    @Override
    public List<CommonCodeDto> getCodes() {
        return CategoryConverter.toDto(
                repository.findByUseYnOrderByCategoryId("Y")
        );
    }
}