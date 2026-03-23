package com.example.demo.paging.mapper;

import com.example.demo.paging.dto.PagingSampleRequest;
import com.example.demo.paging.dto.PagingSampleRow;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PagingSampleMapper {

    List<PagingSampleRow> selectPagingSamplePage(PagingSampleRequest request);

    List<PagingSampleRow> selectPagingSampleAll(PagingSampleRequest request);
}