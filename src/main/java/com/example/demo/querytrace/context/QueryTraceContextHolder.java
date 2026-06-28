package com.example.demo.querytrace.context;

/**
 * QueryTraceContext ThreadLocal holder.
 */
public final class QueryTraceContextHolder {

    private static final ThreadLocal<QueryTraceContext> HOLDER = new ThreadLocal<>();

    private QueryTraceContextHolder() {
    }

    /**
     * 현재 요청의 QueryTraceContext 저장.
     *
     * @param context QueryTraceContext
     */
    public static void set(QueryTraceContext context) {
        HOLDER.set(context);
    }

    /**
     * 현재 요청의 QueryTraceContext 반환.
     *
     * @return QueryTraceContext
     */
    public static QueryTraceContext get() {
        return HOLDER.get();
    }

    /**
     * 현재 요청이 쿼리 추적 대상인지 여부.
     *
     * @return 추적 대상 여부
     */
    public static boolean isActive() {
        return HOLDER.get() != null;
    }

    /**
     * ThreadLocal 제거.
     */
    public static void clear() {
        HOLDER.remove();
    }
}
