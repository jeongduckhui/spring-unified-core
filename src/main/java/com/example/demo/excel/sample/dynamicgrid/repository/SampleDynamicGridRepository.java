package com.example.demo.excel.sample.dynamicgrid.repository;

import com.example.demo.excel.sample.dynamicgrid.entity.SampleDynamicGridEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 엑셀 샘플 다이나믹 그리드 JPA Repository.
 *
 * <p>
 * 조회는 MyBatis를 기준으로 사용하고,
 * 이 Repository는 JPA 저장 버전에서 사용한다.
 * </p>
 */
public interface SampleDynamicGridRepository extends JpaRepository<SampleDynamicGridEntity, Long> {

    /**
     * 저장 전 기존 데이터를 삭제하기 위해 row key 기준 데이터를 조회한다.
     *
     * <p>
     * 동적 컬럼 저장은 delete 후 insert 전략을 사용하므로,
     * 이 조건으로 기존 데이터를 찾아 삭제한다.
     * </p>
     *
     * @param radioType 라디오 선택값
     * @param multiType 다중 선택 조건값
     * @param versionCode 단건 선택 조건값
     * @param categoryName 구분
     * @param appName APP
     * @param gaNgaType GA/NGA
     * @param customerName CUSTOMER
     * @return 기존 저장 데이터 목록
     */
    List<SampleDynamicGridEntity> findByRadioTypeAndMultiTypeAndVersionCodeAndCategoryNameAndAppNameAndGaNgaTypeAndCustomerName(
            String radioType,
            String multiType,
            String versionCode,
            String categoryName,
            String appName,
            String gaNgaType,
            String customerName
    );

    /**
     * row key 기준 기존 데이터를 삭제한다.
     *
     * @param radioType 라디오 선택값
     * @param multiType 다중 선택 조건값
     * @param versionCode 단건 선택 조건값
     * @param categoryName 구분
     * @param appName APP
     * @param gaNgaType GA/NGA
     * @param customerName CUSTOMER
     */
    void deleteByRadioTypeAndMultiTypeAndVersionCodeAndCategoryNameAndAppNameAndGaNgaTypeAndCustomerName(
            String radioType,
            String multiType,
            String versionCode,
            String categoryName,
            String appName,
            String gaNgaType,
            String customerName
    );
}