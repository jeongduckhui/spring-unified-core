package com.example.demo.common.response;

import com.example.demo.transactionlog.context.TransactionLogContext; // ✅ 추가
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "공통 API 응답")
public class ApiResult<T> {

    @Schema(description = "성공 여부", example = "true")
    private boolean success;

    @Schema(description = "응답 데이터")
    private T data;

    @Schema(description = "에러 정보")
    private ErrorResponse error;

    @Schema(description = "응답 메시지")
    private String message;

    /**
     * 성공 응답 (메시지 포함)
     */
    public static <T> ApiResult<T> success(T data, String code, String message) {

        // 핵심: 성공 메시지를 트랜잭션 로그에 세팅
        TransactionLogContext.setMessage(code, message);

        return ApiResult.<T>builder()
                .success(true)
                .data(data)
                .error(null)
                .message(message)
                .build();
    }

    /**
     * 성공 응답 (기본)
     */
    public static <T> ApiResult<T> success(T data) {

        // fallback (혹시 Controller에서 메시지 안 넣었을 경우)
        TransactionLogContext.setMessage("SUCCESS", "정상 처리");

        return ApiResult.<T>builder()
                .success(true)
                .data(data)
                .error(null)
                .build();
    }

    /**
     * 실패 응답
     */
    public static ApiResult<?> fail(ErrorResponse error) {
        return ApiResult.builder()
                .success(false)
                .data(null)
                .error(error)
                .build();
    }
}