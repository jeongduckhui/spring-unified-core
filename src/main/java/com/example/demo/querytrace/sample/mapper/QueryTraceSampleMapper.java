package com.example.demo.querytrace.sample.mapper;

import com.example.demo.querytrace.sample.dto.QueryTraceSampleRow;
import com.example.demo.querytrace.sample.dto.QueryTraceSampleSearchRequest;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 쿼리추적 샘플 MyBatis Mapper.
 */
@Mapper
public interface QueryTraceSampleMapper {

    /**
     * SAMPLE_DYNAMIC_GRID 데이터를 조회한다.
     *
     * @param request 조회조건
     * @return 조회 결과
     */
    List<QueryTraceSampleRow> selectRows(QueryTraceSampleSearchRequest request);
}
