package com.example.demo.paging.service;

import com.example.demo.paging.dto.PagingSampleRequest;
import com.example.demo.paging.dto.PagingSampleResponse;
import com.example.demo.paging.dto.PagingSampleRow;
import com.example.demo.paging.mapper.PagingSampleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PagingSampleService {

    private final PagingSampleMapper pagingSampleMapper;

    public PagingSampleResponse<PagingSampleRow> getPagingSampleList(PagingSampleRequest request) {

        List<PagingSampleRow> list;

        if (request.isAll_view()) {
            list = pagingSampleMapper.selectPagingSampleAll(request);
        } else {
            list = pagingSampleMapper.selectPagingSamplePage(request);
        }

        long tot_count = list.isEmpty() ? 0 : list.get(0).getTot_count();

        int tot_page = 0;
        if (request.getRow_per_page() > 0) {
            tot_page = (int) Math.ceil((double) tot_count / request.getRow_per_page());
        }

        if (request.isAll_view()) {
            tot_page = 1;
        }

        // PagingSampleResponse<Map<String, Object>>
        return PagingSampleResponse.<PagingSampleRow>builder()
                .list(list)
                .page_no(request.getPage_no())
                .row_per_page(request.getRow_per_page())
                .tot_page(tot_page)
                .tot_count(tot_count)
                .build();
    }
}