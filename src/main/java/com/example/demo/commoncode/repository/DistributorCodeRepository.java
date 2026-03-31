package com.example.demo.commoncode.repository;

import com.example.demo.commoncode.domain.DistributorCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DistributorCodeRepository extends JpaRepository<DistributorCode, String> {

    List<DistributorCode> findByUseYnOrderBySortOrder(String useYn);
}