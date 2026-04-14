package com.example.demo.sample.swagger.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Schema(description = "사용자 조회 응답")
public class UserResponse {

    @Schema(description = "사용자 ID", example = "A001")
    private String userId;

    @Schema(description = "사용자 이름", example = "홍길동")
    private String userName;

    @Schema(description = "부서명", example = "개발팀")
    private String deptName;

    @Schema(description = "생성일시", example = "2026-04-14T18:07:17")
    private LocalDateTime createdAt;
}