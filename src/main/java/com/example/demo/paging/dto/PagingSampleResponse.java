package com.example.demo.paging.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PagingSampleResponse<T> {

    private List<T> list;

    /**
     * 현재 페이지
     */
    private int page_no;

    /**
     * 페이지당 건수
     */
    private int row_per_page;

    /**
     * 전체 페이지 수
     */
    private int tot_page;

    /**
     * 전체 건수
     */
    private long tot_count;
}