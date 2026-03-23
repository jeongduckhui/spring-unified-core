package com.example.demo.paging.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PagingSampleRequest extends PageRequest {

    /**
     * 조회 조건 1: 제목
     */
    private String title;

    /**
     * 조회 조건 2: 카테고리
     */
    private String category;

    /**
     * 조회 조건 3: 사용 여부
     */
    private String use_yn;
}