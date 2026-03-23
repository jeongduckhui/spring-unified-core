package com.example.demo.paging.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PageRequest {

    /**
     * 페이지당 건수
     */
    private int row_per_page = 10;

    /**
     * 현재 페이지 번호
     */
    private int page_no = 1;

    /**
     * 전체 보기 여부
     */
    private boolean all_view = false;
}