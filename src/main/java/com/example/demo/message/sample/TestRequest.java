package com.example.demo.message.sample;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TestRequest {

    @NotBlank(message = "REQUIRED_FIELD")
    private String name;

    @Size(max = 5, message = "MAX_LENGTH_EXCEEDED")
    private String code;
}