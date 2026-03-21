package com.example.demo.authgroup.mapper;

import com.example.demo.authgroup.domain.FunctionEntity;
import com.example.demo.authgroup.repository.FunctionRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class FunctionUrlMapper {

    private final FunctionRepository functionRepository;

    /**
     * URL → functionId 매핑 캐시
     */
    private final Map<String, String> urlToFunctionMap = new HashMap<>();

    /**
     * 서버 시작 시 DB에서 전체 로딩
     */
    @PostConstruct
    public void init() {

        List<FunctionEntity> functions = functionRepository.findByUseYn("Y");

        for (FunctionEntity function : functions) {

            /**
             * programId = URL
             */
            urlToFunctionMap.put(
                    function.getProgramId(),
                    function.getFunctionId()
            );
        }
    }

    /**
     * 요청 URL → functionId 조회
     */
    public String getFunctionId(String requestUri) {

        return urlToFunctionMap.get(requestUri);
    }
}