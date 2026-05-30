package com.example.demo.excel.sample.dynamicgrid.mapper;

import com.example.demo.excel.sample.dynamicgrid.dto.SampleDynamicGridCellSaveDto;
import com.example.demo.excel.sample.dynamicgrid.dto.SampleDynamicGridRow;
import com.example.demo.excel.sample.dynamicgrid.dto.SampleDynamicGridSearchRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 엑셀 샘플 다이나믹 그리드 MyBatis Mapper.
 *
 * <p>
 * 이 Mapper는 다이나믹 그리드 샘플 화면에서 사용하는 DB 접근을 담당한다.
 * </p>
 *
 * <p>
 * 현재 설계 기준:
 * 조회는 MyBatis로 처리한다.
 * 저장은 MyBatis 버전과 JPA 버전을 둘 다 제공한다.
 * </p>
 */
@Mapper
public interface SampleDynamicGridMapper {

    /**
     * 다이나믹 그리드 데이터를 조회한다.
     *
     * <p>
     * DB에는 동적 컬럼 값이 세로형으로 저장되어 있다.
     * 이 메서드는 세로형 데이터를 조회하고,
     * Service에서 Grid 표시용 가로형 Map으로 변환한다.
     * </p>
     *
     * @param request 조회조건
     * @return 세로형 조회 결과
     */
    List<SampleDynamicGridRow> selectGridRows(SampleDynamicGridSearchRequest request);

    /**
     * row key 기준 기존 데이터를 삭제한다.
     *
     * <p>
     * 다이나믹 컬럼 저장은 delete 후 insert 전략을 사용한다.
     * 특정 row key에 해당하는 기존 metric 데이터를 삭제한 뒤,
     * 현재 Grid row의 동적 컬럼 값을 다시 insert한다.
     * </p>
     *
     * @param dto 저장 대상 row key 정보
     */
    void deleteByRowKey(SampleDynamicGridCellSaveDto dto);

    /**
     * 세로형 셀 데이터를 insert한다.
     *
     * @param dto 저장할 셀 데이터
     */
    void insertCell(SampleDynamicGridCellSaveDto dto);

    /**
     * 세로형 셀 데이터를 batch insert 한다.
     *
     * <p>
     * H2와 Oracle 모두 foreach insert 형태를 사용할 수 있지만,
     * Oracle에서는 INSERT ALL 방식으로 바꾸는 것이 더 안정적인 경우가 있다.
     * 1차 샘플에서는 단건 insert 반복 또는 foreach 기반으로 시작한다.
     * </p>
     *
     * @param cells 저장할 셀 목록
     */
    void insertCells(@Param("cells") List<SampleDynamicGridCellSaveDto> cells);

    /**
     * 샘플 데이터를 초기화한다.
     *
     * <p>
     * 집에서 H2 DB로 샘플을 빠르게 테스트할 때 사용한다.
     * 운영/실무에서는 제거하거나 dev/local profile에서만 사용한다.
     * </p>
     */
    void deleteAllSampleData();

    /**
     * 샘플 데이터를 insert한다.
     *
     * @param cells 샘플 데이터 목록
     */
    void insertSampleCells(@Param("cells") List<SampleDynamicGridCellSaveDto> cells);
}