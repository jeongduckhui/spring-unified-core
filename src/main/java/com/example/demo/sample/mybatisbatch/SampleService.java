package com.example.demo.sample.mybatisbatch;

import com.example.demo.batch.BatchExecutor;
import com.example.demo.common.exception.BusinessException;
import com.example.demo.common.exception.ExceptionCode;
import lombok.RequiredArgsConstructor;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SampleService {

    private final SqlSessionTemplate batchSqlSessionTemplate;
    private final BatchExecutor batchExecutor;

    @Transactional
    public SaveResponseDto save(SaveRequestDto request) {

        SampleMapper mapper = batchSqlSessionTemplate.getMapper(SampleMapper.class);

        int insertCnt = 0;
        int updateCnt = 0;
        int deleteCnt = 0;

        // 입력 검증만 여기서 처리
        if ((request.getAdded() == null || request.getAdded().isEmpty()) &&
                (request.getUpdated() == null || request.getUpdated().isEmpty()) &&
                (request.getDeleted() == null || request.getDeleted().isEmpty())) {

            throw new BusinessException(ExceptionCode.INVALID_INPUT);
        }

        if (request.getAdded() != null && !request.getAdded().isEmpty()) {
//            insertCnt = batchExecutor.execute(request.getAdded(), dto -> mapper.insertOne(dto));
            insertCnt = batchExecutor.execute(request.getAdded(), mapper::insertOne);
        }

        if (request.getUpdated() != null && !request.getUpdated().isEmpty()) {
//            updateCnt = batchExecutor.execute(request.getUpdated(), dto -> mapper.updateOne(dto));
            updateCnt = batchExecutor.execute(request.getUpdated(), mapper::updateOne);
        }

        if (request.getDeleted() != null && !request.getDeleted().isEmpty()) {
//            deleteCnt = batchExecutor.execute(request.getDeleted(), id -> mapper.deleteOne(id));
            deleteCnt = batchExecutor.execute(request.getDeleted(), mapper::deleteOne);
        }

        return SaveResponseDto.builder()
                .insertCount(insertCnt)
                .updateCount(updateCnt)
                .deleteCount(deleteCnt)
                .build();
    }
}