package com.example.demo.common.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

// 페이징 처리 시 사용
@Getter
@Builder
@Schema(description = "페이징 응답")
public class PageResponse<T> {

    @Schema(description = "데이터 목록")
    private List<T> content;

    @Schema(description = "현재 페이지", example = "0")
    private int page;

    @Schema(description = "페이지 크기", example = "10")
    private int size;

    @Schema(description = "전체 데이터 수", example = "100")
    private long totalElements;

    @Schema(description = "전체 페이지 수", example = "10")
    private int totalPages;

    @Schema(description = "마지막 페이지 여부", example = "false")
    private boolean last;
}