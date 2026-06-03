package com.example.demo.common.exception.sample;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SampleExceptionCode {

    INVALID_REQUEST("CMN400", "error.common.invalid-request"),
    SYSTEM_ERROR("SYS500", "error.common.system-error"),
    DUPLICATE_KEY("CMN409", "error.common.duplicate-key"),

    SAMPLE_BUSINESS_ERROR("SMP400", "error.sample.business");

    private final String code;
    private final String messageKey;
}
