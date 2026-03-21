package com.example.demo.appuser.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "사용자 응답 DTO")
public record AppUserDto(
        @Schema(description = "사용자 ID", example = "1")
        Long id,
        @Schema(description = "사용자 이름", example = "홍길동")
        String name
) {
}