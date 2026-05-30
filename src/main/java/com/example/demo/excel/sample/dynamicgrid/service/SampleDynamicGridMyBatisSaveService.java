package com.example.demo.excel.sample.dynamicgrid.service;

import com.example.demo.excel.sample.dynamicgrid.dto.SampleDynamicGridCellSaveDto;
import com.example.demo.excel.sample.dynamicgrid.dto.SampleDynamicGridSaveRequest;
import com.example.demo.excel.sample.dynamicgrid.mapper.SampleDynamicGridMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 다이나믹 그리드 MyBatis 저장 서비스.
 *
 * <p>
 * 저장 전략:
 * 1. Grid 가로형 row를 세로형 cell 목록으로 변환
 * 2. row key 기준 기존 데이터 삭제
 * 3. I/U row는 insert
 * 4. D row는 delete만 수행
 * </p>
 */
@Service
@RequiredArgsConstructor
public class SampleDynamicGridMyBatisSaveService {

    private static final String ROW_STATUS_DELETE = "D";

    private final SampleDynamicGridService sampleDynamicGridService;
    private final SampleDynamicGridMapper sampleDynamicGridMapper;

    /**
     * MyBatis 방식으로 저장한다.
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

        // 삭제 상태가 아닌 cell만 insert 대상으로 필터링한다.
        List<SampleDynamicGridCellSaveDto> insertCells = cells.stream()
                .filter(cell -> !ROW_STATUS_DELETE.equals(cell.getRowStatus()))
                .toList();

        // insert 대상이 없으면 종료한다.
        if (insertCells.isEmpty()) {
            return;
        }

        // H2 샘플 기준 batch insert를 수행한다.
        sampleDynamicGridMapper.insertCells(insertCells);

        /*
         * Oracle 실무 전환 시에는 아래 둘 중 하나를 추천한다.
         *
         * 1. MyBatis Batch Executor 기반 단건 insert 반복
         * 2. Oracle INSERT ALL 전용 mapper 사용
         *
         * H2의 INSERT INTO ... VALUES (...), (...) 문법은 Oracle에서 그대로 사용할 수 없다.
         */
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

            // row key 기준 기존 데이터를 삭제한다.
            sampleDynamicGridMapper.deleteByRowKey(cell);

            // 삭제 완료 row key를 기록한다.
            deletedRowKeys.add(rowKey);
        }
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