package com.example.demo.excel.sample.dynamicgrid.service;

import com.example.demo.excel.sample.dynamicgrid.dto.SampleDynamicGridCellSaveDto;
import com.example.demo.excel.sample.dynamicgrid.dto.SampleDynamicGridSaveRequest;
import com.example.demo.excel.sample.dynamicgrid.entity.SampleDynamicGridEntity;
import com.example.demo.excel.sample.dynamicgrid.repository.SampleDynamicGridRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 다이나믹 그리드 JPA 저장 서비스.
 *
 * <p>
 * 조회는 MyBatis를 사용하지만,
 * 저장 방식 비교를 위해 JPA 저장 버전도 제공한다.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class SampleDynamicGridJpaSaveService {

    private static final String ROW_STATUS_DELETE = "D";

    private final SampleDynamicGridService sampleDynamicGridService;
    private final SampleDynamicGridRepository sampleDynamicGridRepository;

    /**
     * JPA 방식으로 저장한다.
     *
     * @param request 저장 요청
     */
    @Transactional
    public void save(SampleDynamicGridSaveRequest request) {

        // Grid rowData를 세로형 cell 목록으로 변환한다.
        List<SampleDynamicGridCellSaveDto> cells = sampleDynamicGridService.flattenRows(request);

        // 저장할 cell이 없으면 종료한다.
        if (cells.isEmpty()) {
            return;
        }

        // row key 기준으로 기존 데이터를 먼저 삭제한다.
        deleteExistingRows(cells);

        // 삭제 상태가 아닌 cell만 Entity로 변환한다.
        List<SampleDynamicGridEntity> entities = cells.stream()
                .filter(cell -> !ROW_STATUS_DELETE.equals(cell.getRowStatus()))
                .map(this::toEntity)
                .toList();

        // insert 대상이 없으면 종료한다.
        if (entities.isEmpty()) {
            return;
        }

        // JPA saveAll로 저장한다.
        sampleDynamicGridRepository.saveAll(entities);
    }

    /**
     * row key 기준으로 기존 데이터를 삭제한다.
     *
     * @param cells 저장 cell 목록
     */
    private void deleteExistingRows(List<SampleDynamicGridCellSaveDto> cells) {

        // 같은 row key를 중복 삭제하지 않기 위한 Set.
        Set<String> deletedRowKeys = new LinkedHashSet<>();

        // cell 목록을 순회한다.
        for (SampleDynamicGridCellSaveDto cell : cells) {

            // row key를 생성한다.
            String rowKey = createRowKey(cell);

            // 이미 삭제한 row key면 건너뛴다.
            if (deletedRowKeys.contains(rowKey)) {
                continue;
            }

            // JPA 파생 delete 메서드로 기존 row 데이터를 삭제한다.
            sampleDynamicGridRepository
                    .deleteByRadioTypeAndMultiTypeAndVersionCodeAndCategoryNameAndAppNameAndGaNgaTypeAndCustomerName(
                            cell.getRadioType(),
                            cell.getMultiType(),
                            cell.getVersionCode(),
                            cell.getCategoryName(),
                            cell.getAppName(),
                            cell.getGaNgaType(),
                            cell.getCustomerName()
                    );

            // 삭제 완료 row key를 기록한다.
            deletedRowKeys.add(rowKey);
        }
    }

    /**
     * 저장용 DTO를 Entity로 변환한다.
     *
     * @param cell 저장용 cell DTO
     * @return Entity
     */
    private SampleDynamicGridEntity toEntity(SampleDynamicGridCellSaveDto cell) {
        return SampleDynamicGridEntity.builder()
                .baseYm(cell.getBaseYm())
                .radioType(cell.getRadioType())
                .multiType(cell.getMultiType())
                .versionCode(cell.getVersionCode())
                .categoryName(cell.getCategoryName())
                .appName(cell.getAppName())
                .gaNgaType(cell.getGaNgaType())
                .customerName(cell.getCustomerName())
                .sort3(cell.getSort3())
                .sort4(cell.getSort4())
                .sort5(cell.getSort5())
                .metricType(cell.getMetricType())
                .yearValue(cell.getYearValue())
                .quarterValue(cell.getQuarterValue())
                .valueDecimal(cell.getValueDecimal())
                .build();
    }

    /**
     * cell에서 row key 문자열을 생성한다.
     *
     * @param cell 저장 cell
     * @return row key
     */
    private String createRowKey(SampleDynamicGridCellSaveDto cell) {
        return String.join("|",
                nullToEmpty(cell.getRadioType()),
                nullToEmpty(cell.getMultiType()),
                nullToEmpty(cell.getVersionCode()),
                nullToEmpty(cell.getCategoryName()),
                nullToEmpty(cell.getAppName()),
                nullToEmpty(cell.getGaNgaType()),
                nullToEmpty(cell.getCustomerName())
        );
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}