package com.example.demo.querytrace.service;

import com.example.demo.querytrace.config.QueryTraceProperties;
import com.example.demo.querytrace.context.QueryTraceContext;
import com.example.demo.querytrace.context.QueryTraceSql;
import com.example.demo.querytrace.dto.QueryTraceMetaResponse;
import com.example.demo.querytrace.dto.QueryTraceSqlResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * QueryTrace Redis 저장/조회 서비스.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QueryTraceRedisService {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final QueryTraceProperties queryTraceProperties;

    /**
     * QueryTrace 정보를 Redis에 저장한다.
     *
     * <p>
     * Redis 자료구조:
     * 1. query:view:{userId}:{screenId}:traces → List(traceId 목록, 최신순)
     * 2. query:trace:{traceId}:meta → Hash(조회 메타정보)
     * 3. query:trace:{traceId}:sqls → List(SQL JSON 목록)
     * </p>
     *
     * <p>
     * 모든 관련 Key는 설정된 지역 기준 당일 만료시각에 동일하게 만료된다.
     * 기본값은 Asia/Seoul 기준 23:59:59이다.
     * </p>
     *
     * @param context QueryTraceContext
     */
    public void save(QueryTraceContext context) {
        if (context == null || !StringUtils.hasText(context.getTraceId())) {
            return;
        }

        Date expireAt = calculateExpireAtEndOfDay();
        Date now = new Date();
        if (!expireAt.after(now)) {
            return;
        }

        String viewKey = viewTracesKey(context.getUserId(), context.getScreenId());
        String metaKey = traceMetaKey(context.getTraceId());
        String sqlsKey = traceSqlsKey(context.getTraceId());

        Map<String, String> metaMap = createMetaMap(context);
        List<String> sqlJsons = createSqlJsons(context);

        /*
         * 운영 정책:
         * 사용자 + 화면(Route Path) 기준으로 최근 N건만 유지한다.
         *
         * 예)
         * - userA + /sample → 최근 N건
         * - userA + /sales  → 최근 N건
         * - userB + /sample → 최근 N건
         *
         * N은 query-trace.max-trace-count-per-screen 설정으로 변경 가능하다.
         */
        int maxTraceCount = queryTraceProperties.getMaxTraceCountPerScreen();

        // 새 trace가 LPUSH되면 기존 목록의 maxTraceCount - 1 이후 항목은 잘려나간다.
        // 잘려나갈 trace의 상세 Key도 함께 삭제해서 Redis에 불필요한 상세 데이터가 남지 않게 한다.
        List<String> trimTargetTraceIds = findTrimTargetTraceIds(viewKey, maxTraceCount);

        stringRedisTemplate.executePipelined(new SessionCallback<Object>() {
            @Override
            @SuppressWarnings({"rawtypes", "unchecked"})
            public Object execute(RedisOperations operations) {
                operations.opsForHash().putAll(metaKey, metaMap);

                if (!sqlJsons.isEmpty()) {
                    operations.opsForList().rightPushAll(sqlsKey, sqlJsons);
                }

                operations.opsForList().leftPush(viewKey, context.getTraceId());
                operations.opsForList().trim(viewKey, 0, maxTraceCount - 1);

                operations.expireAt(viewKey, expireAt);
                operations.expireAt(metaKey, expireAt);

                if (!sqlJsons.isEmpty()) {
                    operations.expireAt(sqlsKey, expireAt);
                }

                deleteTrimmedTraceDetails(operations, trimTargetTraceIds);

                return null;
            }
        });
    }

    /**
     * 현재 사용자/화면 기준 쿼리 추적 목록을 조회한다.
     *
     * @param userId 사용자 ID
     * @param screenId 화면 ID
     * @return 쿼리 추적 목록
     */
    public List<QueryTraceMetaResponse> findMetas(String userId, String screenId) {
        String viewKey = viewTracesKey(userId, screenId);
        List<String> traceIds = stringRedisTemplate.opsForList().range(viewKey, 0, -1);

        if (traceIds == null || traceIds.isEmpty()) {
            return List.of();
        }

        return traceIds.stream()
                .map(traceId -> stringRedisTemplate.opsForHash().entries(traceMetaKey(traceId)))
                .filter(map -> map != null && !map.isEmpty())
                .map(this::toMetaResponse)
                .toList();
    }

    /**
     * 현재 사용자/화면/traceId 기준 SQL 목록을 조회한다.
     *
     * <p>
     * traceId만으로 상세 SQL을 조회하지 않고 meta의 userId/screenId를 검증한다.
     * 이 검증을 통과하지 못하면 빈 목록을 반환한다.
     * </p>
     *
     * @param userId 사용자 ID
     * @param screenId 화면 ID
     * @param traceId traceId
     * @return SQL 목록
     */
    public List<QueryTraceSqlResponse> findSqls(String userId, String screenId, String traceId) {
        if (!StringUtils.hasText(traceId)) {
            return List.of();
        }

        Map<Object, Object> meta = stringRedisTemplate.opsForHash().entries(traceMetaKey(traceId));
        if (meta == null || meta.isEmpty()) {
            return List.of();
        }

        if (!isTraceOwner(meta, userId, screenId)) {
            log.warn(
                    "QueryTrace SQL 조회 차단. 요청 userId={}, screenId={}, traceId={}",
                    userId,
                    screenId,
                    traceId
            );
            return List.of();
        }

        List<String> sqlJsons = stringRedisTemplate.opsForList().range(traceSqlsKey(traceId), 0, -1);

        if (sqlJsons == null || sqlJsons.isEmpty()) {
            return List.of();
        }

        return sqlJsons.stream()
                .map(this::fromJson)
                .filter(Objects::nonNull)
                .toList();
    }

    private Map<String, String> createMetaMap(QueryTraceContext context) {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("traceId", nullToEmpty(context.getTraceId()));
        map.put("userId", nullToEmpty(context.getUserId()));
        map.put("screenId", nullToEmpty(context.getScreenId()));
        map.put("requestUri", nullToEmpty(context.getRequestUri()));
        map.put("controllerName", nullToEmpty(context.getControllerName()));
        map.put("methodName", nullToEmpty(context.getMethodName()));
        map.put("description", nullToEmpty(context.getDescription()));
        map.put("transactionStartTime", toString(context.getTransactionStartTime()));
        map.put("transactionEndTime", toString(context.getTransactionEndTime()));
        map.put("transactionDurationMs", String.valueOf(context.getTransactionDurationMs()));
        map.put("queryCount", String.valueOf(context.getQueryCount()));
        map.put("success", String.valueOf(context.isSuccess()));
        map.put("errorMessage", nullToEmpty(context.getErrorMessage()));
        map.put("createdAt", toString(LocalDateTime.now(queryTraceProperties.getZoneIdValue())));
        return map;
    }

    private List<String> createSqlJsons(QueryTraceContext context) {
        if (context.getSqls() == null || context.getSqls().isEmpty()) {
            return List.of();
        }

        return context.getSqls()
                .stream()
                .map(this::toJson)
                .filter(Objects::nonNull)
                .toList();
    }

    private List<String> findTrimTargetTraceIds(String viewKey, int maxTraceCount) {
        List<String> traceIds = stringRedisTemplate.opsForList()
                .range(viewKey, maxTraceCount - 1, -1);

        if (traceIds == null || traceIds.isEmpty()) {
            return List.of();
        }

        return new ArrayList<>(traceIds);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void deleteTrimmedTraceDetails(RedisOperations operations, List<String> traceIds) {
        if (traceIds == null || traceIds.isEmpty()) {
            return;
        }

        for (String traceId : traceIds) {
            if (!StringUtils.hasText(traceId)) {
                continue;
            }

            operations.delete(traceMetaKey(traceId));
            operations.delete(traceSqlsKey(traceId));
        }
    }

    private boolean isTraceOwner(Map<Object, Object> meta, String userId, String screenId) {
        String savedUserId = asString(meta.get("userId"));
        String savedScreenId = asString(meta.get("screenId"));

        return Objects.equals(savedUserId, nullToEmpty(userId))
                && Objects.equals(savedScreenId, nullToEmpty(screenId));
    }

    private QueryTraceMetaResponse toMetaResponse(Map<Object, Object> map) {
        return QueryTraceMetaResponse.builder()
                .traceId(asString(map.get("traceId")))
                .userId(asString(map.get("userId")))
                .screenId(asString(map.get("screenId")))
                .requestUri(asString(map.get("requestUri")))
                .controllerName(asString(map.get("controllerName")))
                .methodName(asString(map.get("methodName")))
                .description(asString(map.get("description")))
                .transactionStartTime(asString(map.get("transactionStartTime")))
                .transactionEndTime(asString(map.get("transactionEndTime")))
                .transactionDurationMs(toLong(map.get("transactionDurationMs")))
                .queryCount(toInt(map.get("queryCount")))
                .success(Boolean.parseBoolean(asString(map.get("success"))))
                .errorMessage(asString(map.get("errorMessage")))
                .createdAt(asString(map.get("createdAt")))
                .build();
    }

    private String toJson(QueryTraceSql sql) {
        try {
            return objectMapper.writeValueAsString(QueryTraceSqlResponse.builder()
                    .orderNo(sql.getOrderNo())
                    .mapperId(nullToEmpty(sql.getMapperId()))
                    .sqlCommandType(nullToEmpty(sql.getSqlCommandType()))
                    .sql(nullToEmpty(sql.getSql()))
                    .executionStartTime(toString(sql.getExecutionStartTime()))
                    .executionEndTime(toString(sql.getExecutionEndTime()))
                    .executionTimeMs(sql.getExecutionTimeMs())
                    .build());
        } catch (JsonProcessingException e) {
            log.warn("QueryTrace SQL JSON 변환 실패", e);
            return null;
        }
    }

    private QueryTraceSqlResponse fromJson(String json) {
        try {
            return objectMapper.readValue(json, QueryTraceSqlResponse.class);
        } catch (Exception e) {
            log.warn("QueryTrace SQL JSON 읽기 실패", e);
            return null;
        }
    }

    private Date calculateExpireAtEndOfDay() {
        ZoneId zoneId = queryTraceProperties.getZoneIdValue();
        LocalDateTime now = LocalDateTime.now(zoneId);
        LocalTime expireTime = queryTraceProperties.getExpireTime();
        LocalDateTime expireAt = now.toLocalDate().atTime(expireTime);
        return Date.from(expireAt.atZone(zoneId).toInstant());
    }

    private String viewTracesKey(String userId, String screenId) {
        return "query:view:" + normalizeKeyPart(userId) + ":" + normalizeKeyPart(screenId) + ":traces";
    }

    private String traceMetaKey(String traceId) {
        return "query:trace:" + traceId + ":meta";
    }

    private String traceSqlsKey(String traceId) {
        return "query:trace:" + traceId + ":sqls";
    }

    private String normalizeKeyPart(String value) {
        if (!StringUtils.hasText(value)) {
            return "unknown";
        }
        return value.replace(":", "_");
    }

    private String toString(LocalDateTime dateTime) {
        return dateTime == null ? "" : dateTime.toString();
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private String asString(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private long toLong(Object value) {
        try {
            return Long.parseLong(asString(value));
        } catch (Exception e) {
            return 0L;
        }
    }

    private int toInt(Object value) {
        try {
            return Integer.parseInt(asString(value));
        } catch (Exception e) {
            return 0;
        }
    }
}
