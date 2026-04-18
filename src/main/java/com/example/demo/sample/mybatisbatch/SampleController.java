package com.example.demo.sample.mybatisbatch;

import com.example.demo.common.response.ApiResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sample")
public class SampleController {

    private final SampleService sampleService;

    @PostMapping("/save")
    public ApiResult<SaveResponseDto> save(@RequestBody SaveRequestDto request) {

        SaveResponseDto response = sampleService.save(request);

        return ApiResult.success(response);
    }
}
