package com.example.demo.commoncode.repository;

import com.example.demo.commoncode.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * 사용 여부 기준 조회 + 정렬
     */
    List<Category> findByUseYnOrderByCategoryId(String useYn);
}