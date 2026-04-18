package com.example.demo.converter.annotation;

import com.example.demo.converter.deserializer.CsvToListDeserializer;
import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@JacksonAnnotationsInside
@JsonDeserialize(using = CsvToListDeserializer.class)
public @interface CsvToList {

    String delimiter() default ",";

    Class<?> type() default String.class;
}