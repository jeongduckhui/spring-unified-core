package com.example.demo.querytrace.aop;

import com.example.demo.querytrace.annotation.QueryTrace;
import com.example.demo.querytrace.context.QueryTraceContext;
import com.example.demo.querytrace.context.QueryTraceContextHolder;
import com.example.demo.querytrace.service.QueryTraceRedisService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * QueryTrace 어노테이션 기반 AOP.
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class QueryTraceAspect {

    private static final String HEADER_SCREEN_ID = "X-Screen-Id";

    private final QueryTraceRedisService queryTraceRedisService;

    /**
     * QueryTrace 어노테이션이 붙은 Controller 메서드의 SQL 추적.
     *
     * @param joinPoint joinPoint
     * @param queryTrace QueryTrace 어노테이션
     * @return Controller 메서드 반환값
     * @throws Throwable 예외
     */
    @Around("@annotation(queryTrace)")
    public Object trace(ProceedingJoinPoint joinPoint, QueryTrace queryTrace) throws Throwable {

        LocalDateTime startTime = LocalDateTime.now();
        HttpServletRequest request = currentRequest();

        QueryTraceContext context = QueryTraceContext.builder()
                .traceId(UUID.randomUUID().toString())
                .userId(resolveUserId())
                .screenId(resolveScreenId(request))
                .requestUri(request == null ? "" : request.getRequestURI())
                .controllerName(joinPoint.getSignature().getDeclaringTypeName())
                .methodName(joinPoint.getSignature().getName())
                .description(queryTrace.value())
                .transactionStartTime(startTime)
                .success(false)
                .build();

        QueryTraceContextHolder.set(context);

        try {
            Object result = joinPoint.proceed();
            context.setSuccess(true);
            return result;
        } catch (Throwable throwable) {
            context.setSuccess(false);
            context.setErrorMessage(shortErrorMessage(throwable));
            throw throwable;
        } finally {
            LocalDateTime endTime = LocalDateTime.now();
            context.setTransactionEndTime(endTime);
            context.setTransactionDurationMs(Duration.between(startTime, endTime).toMillis());

            try {
                queryTraceRedisService.save(context);
            } catch (Exception redisException) {
                log.warn("QueryTrace Redis 저장 실패. traceId={}", context.getTraceId(), redisException);
            } finally {
                QueryTraceContextHolder.clear();
            }
        }
    }

    private HttpServletRequest currentRequest() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes == null ? null : attributes.getRequest();
    }

    private String resolveScreenId(HttpServletRequest request) {
        if (request == null) {
            return "UNKNOWN_SCREEN";
        }

        String screenId = request.getHeader(HEADER_SCREEN_ID);
        if (StringUtils.hasText(screenId)) {
            return screenId;
        }

        return request.getRequestURI();
    }

    private String resolveUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !StringUtils.hasText(authentication.getName())) {
            return "anonymous";
        }

        return authentication.getName();
    }

    private String shortErrorMessage(Throwable throwable) {
        if (throwable == null) {
            return "";
        }

        String message = throwable.getMessage();
        if (!StringUtils.hasText(message)) {
            return throwable.getClass().getSimpleName();
        }

        return message.length() > 500 ? message.substring(0, 500) : message;
    }
}
