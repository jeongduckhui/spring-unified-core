package com.example.demo.common.response;

import com.example.demo.common.exception.ExceptionCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "공통 에러 응답")
public class ErrorResponse {

    @Schema(
            description = "에러 코드",
            example = "E400"
            // 허용 가능 값 세팅
//            allowableValues = {"E400", "E401", "E403", "U404"}
    )
    private String code;

    @Schema(description = "에러 메시지", example = "사용자를 찾을 수 없습니다.")
    private String message;

    public static ErrorResponse of_message적용전(ExceptionCode e) {
        return ErrorResponse.builder()
                .code(e.getCode())
//                .message(e.getMessage())
                .build();
    }

    public static ErrorResponse of(String code, String message) {
        return new ErrorResponse(code, message);
    }
}