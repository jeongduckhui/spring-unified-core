package com.example.demo.batch;

import lombok.RequiredArgsConstructor;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
public class BatchExecutor {

    private final SqlSessionTemplate batchSqlSessionTemplate;

    public <T> int execute(List<T> list, Consumer<T> consumer) {

        int count = 0;
        int batchSize = 100;

        for (int i = 0; i < list.size(); i++) {

            consumer.accept(list.get(i));
            count++;

            if ((i + 1) % batchSize == 0) {
                batchSqlSessionTemplate.flushStatements();
                batchSqlSessionTemplate.clearCache();
            }
        }

        batchSqlSessionTemplate.flushStatements();
        batchSqlSessionTemplate.clearCache();

        return count;
    }
}