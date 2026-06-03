package com.example.demo.common.exception.sample;

import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/common/exception/sample")
public class ExceptionSampleController {

    @PostMapping("/validation")
    public SampleApiResult<Void> validation(
            @Valid @RequestBody ExceptionSampleRequest request
    ) {
        return SampleApiResult.success();
    }

    @PostMapping("/multi-validation")
    public SampleApiResult<Void> multiValidation(
            @Valid @RequestBody ExceptionSampleRequest request
    ) {
        return SampleApiResult.success();
    }

    @GetMapping("/business")
    public SampleApiResult<Void> businessException() {
        throw new SampleBusinessException(SampleExceptionCode.SAMPLE_BUSINESS_ERROR);
    }

    @GetMapping("/system")
    public SampleApiResult<Void> systemException() {
        String value = null;
        value.length();

        return SampleApiResult.success();
    }

    @GetMapping("/duplicate-key")
    public SampleApiResult<Void> duplicateKeyException() {
        throw new DataIntegrityViolationException("sample duplicate key exception");
    }
}