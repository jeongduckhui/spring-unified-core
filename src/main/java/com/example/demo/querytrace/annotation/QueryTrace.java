package com.example.demo.querytrace.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * MyBatis 쿼리 추적 대상 Controller 메서드 표시 어노테이션.
 *
 * <p>
 * 이 어노테이션이 붙은 Controller 메서드가 호출되면 AOP가 요청 단위
 * QueryTraceContext를 생성하고, MyBatis Interceptor가 실행 SQL을 수집한다.
 * </p>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface QueryTrace {

    /**
     * 업무 설명.
     *
     * <p>
     * Redis key에는 사용하지 않고, 쿼리보기 그리드 설명용 메타정보로 사용한다.
     * </p>
     */
    String value() default "";
}
