package com.example.demo.paging.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PagingSampleRow {

    private Long sampleId;
    private String title;
    private String category;
    private String useYn;
    private String createdAt;

    /**
     * 전체 건수
     */
    private long tot_count;
}