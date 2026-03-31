package com.example.demo.commoncode.converter;

import com.example.demo.commoncode.domain.Category;
import com.example.demo.commoncode.dto.CommonCodeDto;

import java.util.List;

public class CategoryConverter {

    public static List<CommonCodeDto> toDto(List<Category> list) {
        return list.stream()
                .map(c -> new CommonCodeDto(
                        String.valueOf(c.getCategoryId()),
                        c.getCategoryName()
                ))
                .toList();
    }
}