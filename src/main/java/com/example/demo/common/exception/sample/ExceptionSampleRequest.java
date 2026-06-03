package com.example.demo.common.exception.sample;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExceptionSampleRequest {

    @NotBlank(message = "{validation.userId.required}")
    private String userId;

    @NotBlank(message = "{validation.userName.required}")
    private String userName;

    @Size(max = 10, message = "{validation.description.max-length}")
    private String description;
}