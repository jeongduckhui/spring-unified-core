package com.example.demo.sample.swagger.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "사용자 조회 조건")
public class UserSearchRequest {

    @Schema(description = "사용자 ID", example = "A001")
    private String userId;

    @Schema(description = "사용자 이름", example = "홍길동")
    private String userName;

    @Schema(description = "부서명", example = "개발팀")
    private String deptName;

    @Schema(description = "조회 시작일 (yyyy-MM-dd)", example = "2026-04-01")
    private String fromDate;
}
