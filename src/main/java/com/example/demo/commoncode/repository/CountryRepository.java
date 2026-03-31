package com.example.demo.commoncode.repository;

import com.example.demo.commoncode.domain.Country;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CountryRepository extends JpaRepository<Country, String> {

    /**
     * 사용 여부 기준 조회
     */
    List<Country> findByUseYn(String useYn);
}