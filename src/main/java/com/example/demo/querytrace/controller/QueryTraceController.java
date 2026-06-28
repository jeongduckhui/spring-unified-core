package com.example.demo.querytrace.controller;

import com.example.demo.common.response.ApiResult;
import com.example.demo.querytrace.dto.QueryTraceMetaResponse;
import com.example.demo.querytrace.dto.QueryTraceSqlResponse;
import com.example.demo.querytrace.service.QueryTraceRedisService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 쿼리보기 API Controller.
 */
@RestController
@RequestMapping("/api/query-traces")
@RequiredArgsConstructor
public class QueryTraceController {

    private static final String HEADER_SCREEN_ID = "X-Screen-Id";

    private final QueryTraceRedisService queryTraceRedisService;

    /**
     * 현재 사용자/화면 기준 쿼리 추적 목록 조회.
     *
     * @param request HttpServletRequest
     * @return 쿼리 추적 목록
     */
    @GetMapping
    public ApiResult<List<QueryTraceMetaResponse>> findTraces(HttpServletRequest request) {
        String userId = resolveUserId();
        String screenId = resolveScreenId(request);

        return ApiResult.success(queryTraceRedisService.findMetas(userId, screenId));
    }

    /**
     * 현재 사용자/화면/traceId 기준 SQL 상세 목록 조회.
     *
     * <p>
     * traceId만으로 상세 SQL을 조회하면 다른 사용자의 traceId를 알게 되었을 때
     * SQL을 볼 수 있는 문제가 생길 수 있으므로, userId와 screenId를 함께 검증한다.
     * </p>
     *
     * @param traceId traceId
     * @param request HttpServletRequest
     * @return SQL 목록
     */
    @GetMapping("/{traceId}/sqls")
    public ApiResult<List<QueryTraceSqlResponse>> findSqls(
            @PathVariable String traceId,
            HttpServletRequest request
    ) {
        String userId = resolveUserId();
        String screenId = resolveScreenId(request);

        return ApiResult.success(queryTraceRedisService.findSqls(userId, screenId, traceId));
    }

    private String resolveScreenId(HttpServletRequest request) {
        if (request == null) {
            return "UNKNOWN_SCREEN";
        }

        String screenId = request.getHeader(HEADER_SCREEN_ID);
        return StringUtils.hasText(screenId) ? screenId : request.getRequestURI();
    }

    private String resolveUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !StringUtils.hasText(authentication.getName())) {
            return "anonymous";
        }

        return authentication.getName();
    }
}
