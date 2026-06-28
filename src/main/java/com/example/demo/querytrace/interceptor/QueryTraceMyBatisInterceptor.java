package com.example.demo.querytrace.interceptor;

import com.example.demo.querytrace.context.QueryTraceContext;
import com.example.demo.querytrace.context.QueryTraceContextHolder;
import com.example.demo.querytrace.context.QueryTraceSql;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.ParameterMode;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Properties;

/**
 * MyBatis SQL 추적 Interceptor.
 */
@Component
@Intercepts({
        @Signature(type = Executor.class, method = "query", args = {
                MappedStatement.class,
                Object.class,
                RowBounds.class,
                ResultHandler.class
        }),
        @Signature(type = Executor.class, method = "update", args = {
                MappedStatement.class,
                Object.class
        })
})
public class QueryTraceMyBatisInterceptor implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {

        if (!QueryTraceContextHolder.isActive()) {
            return invocation.proceed();
        }

        MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
        Object parameter = invocation.getArgs()[1];
        BoundSql boundSql = mappedStatement.getBoundSql(parameter);

        String finalSql = createFinalSql(mappedStatement.getConfiguration(), boundSql, parameter);
        String mapperId = mappedStatement.getId();
        String sqlCommandType = mappedStatement.getSqlCommandType().name();

        LocalDateTime startTime = LocalDateTime.now();
        try {
            return invocation.proceed();
        } finally {
            LocalDateTime endTime = LocalDateTime.now();
            QueryTraceContext context = QueryTraceContextHolder.get();

            if (context != null) {
                context.addSql(QueryTraceSql.builder()
                        .orderNo(context.nextSqlOrderNo())
                        .mapperId(mapperId)
                        .sqlCommandType(sqlCommandType)
                        .sql(maskSql(finalSql))
                        .executionStartTime(startTime)
                        .executionEndTime(endTime)
                        .executionTimeMs(Duration.between(startTime, endTime).toMillis())
                        .build());
            }
        }
    }

    private String createFinalSql(Configuration configuration, BoundSql boundSql, Object parameterObject) {
        String sql = boundSql.getSql().replaceAll("\\s+", " ").trim();
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();

        if (parameterMappings == null || parameterMappings.isEmpty()) {
            return sql;
        }

        TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
        MetaObject metaObject = parameterObject == null ? null : configuration.newMetaObject(parameterObject);

        for (ParameterMapping parameterMapping : parameterMappings) {
            if (parameterMapping.getMode() == ParameterMode.OUT) {
                continue;
            }

            Object value = resolveParameterValue(
                    boundSql,
                    parameterObject,
                    typeHandlerRegistry,
                    metaObject,
                    parameterMapping.getProperty()
            );

            sql = sql.replaceFirst("\\?", quoteParameterValue(value));
        }

        return sql;
    }

    private Object resolveParameterValue(
            BoundSql boundSql,
            Object parameterObject,
            TypeHandlerRegistry typeHandlerRegistry,
            MetaObject metaObject,
            String propertyName
    ) {
        if (boundSql.hasAdditionalParameter(propertyName)) {
            return boundSql.getAdditionalParameter(propertyName);
        }

        if (parameterObject == null) {
            return null;
        }

        if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
            return parameterObject;
        }

        return metaObject == null ? null : metaObject.getValue(propertyName);
    }

    private String quoteParameterValue(Object value) {
        if (value == null) {
            return "NULL";
        }

        if (value instanceof Number || value instanceof Boolean) {
            return value.toString();
        }

        return "'" + value.toString().replace("'", "''") + "'";
    }

    private String maskSql(String sql) {
        if (sql == null) {
            return "";
        }

        // 1차 샘플 마스킹. 실무에서는 필드명 정책을 더 늘리면 된다.
        return sql
                .replaceAll("(?i)(password|passwd|pwd)\\s*=\\s*'[^']*'", "$1 = '****'")
                .replaceAll("(?i)(token)\\s*=\\s*'[^']*'", "$1 = '****'");
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        // NOP
    }
}
