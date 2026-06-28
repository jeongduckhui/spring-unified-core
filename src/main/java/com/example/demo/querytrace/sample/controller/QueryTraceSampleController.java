package com.example.demo.querytrace.sample.controller;

import com.example.demo.common.response.ApiResult;
import com.example.demo.querytrace.annotation.QueryTrace;
import com.example.demo.querytrace.sample.dto.QueryTraceSampleRow;
import com.example.demo.querytrace.sample.dto.QueryTraceSampleSearchRequest;
import com.example.demo.querytrace.sample.service.QueryTraceSampleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 쿼리추적 테스트 화면용 샘플 Controller.
 */
@RestController
@RequestMapping("/api/query-trace/sample")
@RequiredArgsConstructor
public class QueryTraceSampleController {

    private final QueryTraceSampleService queryTraceSampleService;

    /**
     * SAMPLE_DYNAMIC_GRID 조회.
     *
     * @param request 조회조건
     * @return 조회 결과
     */
    @QueryTrace("SAMPLE_DYNAMIC_GRID 조회")
    @PostMapping("/search")
    public ApiResult<List<QueryTraceSampleRow>> search(
            @RequestBody QueryTraceSampleSearchRequest request
    ) {
        return ApiResult.success(queryTraceSampleService.search(request));
    }
}
