package com.example.demo.converter.converter;

import com.example.demo.converter.util.CsvUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CsvToListConverter implements Converter<String, List<String>> {

    @Override
    public List<String> convert(String source) {
        return CsvUtils.toStringList(source, ",");
    }
}