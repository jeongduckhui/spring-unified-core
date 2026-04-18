package com.example.demo.converter.deserializer;

import com.example.demo.converter.annotation.CsvToList;
import com.example.demo.converter.util.CsvUtils;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;

import java.io.IOException;
import java.util.List;

public class CsvToListDeserializer extends JsonDeserializer<List<?>> implements ContextualDeserializer {

    private String delimiter;
    private Class<?> type;

    public CsvToListDeserializer() {
    }

    public CsvToListDeserializer(String delimiter, Class<?> type) {
        this.delimiter = delimiter;
        this.type = type;
    }

    @Override
    public List<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {

        String value = p.getValueAsString();

        if (value == null || value.isBlank()) {
            return List.of();
        }

        if (Integer.class.equals(type)) {
            return CsvUtils.toIntegerList(value, delimiter);
        }

        return CsvUtils.toStringList(value, delimiter);
    }

    /**
     * ⭐ 핵심: 필드별 어노테이션 값 읽어오기
     */
    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) {

        CsvToList ann = property.getAnnotation(CsvToList.class);

        if (ann == null) {
            return this;
        }

        return new CsvToListDeserializer(ann.delimiter(), ann.type());
    }
}