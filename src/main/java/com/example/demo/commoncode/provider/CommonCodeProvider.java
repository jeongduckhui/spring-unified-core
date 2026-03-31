package com.example.demo.commoncode.provider;

import com.example.demo.commoncode.dto.CommonCodeDto;

import java.util.List;

public interface CommonCodeProvider {

    String getGroupCode();

    List<CommonCodeDto> getCodes();
}