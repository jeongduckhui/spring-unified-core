package com.example.demo.converter.util;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.regex.Pattern;

public class CsvUtils {

    public static List<String> toStringList(String source, String delimiter) {
        if (source == null || source.isBlank()) {
            return List.of();
        }

        return Arrays.stream(source.split(Pattern.quote(delimiter)))
                .map(s -> s.replace("'", "").trim())
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    public static List<Integer> toIntegerList(String source, String delimiter) {
        return toStringList(source, delimiter).stream()
                .map(Integer::valueOf)
                .collect(Collectors.toList());
    }
}