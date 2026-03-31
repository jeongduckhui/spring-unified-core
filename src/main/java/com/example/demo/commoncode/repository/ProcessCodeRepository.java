package com.example.demo.commoncode.repository;

import com.example.demo.commoncode.domain.ProcessCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProcessCodeRepository extends JpaRepository<ProcessCode, String> {

    List<ProcessCode> findByUseYnOrderBySortOrder(String useYn);
}