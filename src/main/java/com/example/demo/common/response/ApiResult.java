package com.example.demo.common.response;

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

    public static <T> ApiResult<T> success(T data) {
        return ApiResult.<T>builder()
                .success(true)
                .data(data)
                .error(null)
                .build();
    }

    public static ApiResult<?> fail(ErrorResponse error) {
        return ApiResult.builder()
                .success(false)
                .data(null)
                .error(error)
                .build();
    }
}