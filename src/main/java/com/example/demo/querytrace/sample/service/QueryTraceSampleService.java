package com.example.demo.querytrace.sample.service;

import com.example.demo.querytrace.sample.dto.QueryTraceSampleRow;
import com.example.demo.querytrace.sample.dto.QueryTraceSampleSearchRequest;
import com.example.demo.querytrace.sample.mapper.QueryTraceSampleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 쿼리추적 샘플 조회 Service.
 */
@Service
@RequiredArgsConstructor
public class QueryTraceSampleService {

    private final QueryTraceSampleMapper queryTraceSampleMapper;

    /**
     * SAMPLE_DYNAMIC_GRID 데이터를 조회한다.
     *
     * @param request 조회조건
     * @return 조회 결과
     */
    public List<QueryTraceSampleRow> search(QueryTraceSampleSearchRequest request) {
        return queryTraceSampleMapper.selectRows(request);
    }
}
