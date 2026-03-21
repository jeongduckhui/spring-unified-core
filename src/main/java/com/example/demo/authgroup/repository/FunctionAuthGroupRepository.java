package com.example.demo.authgroup.repository;

import com.example.demo.authgroup.domain.FunctionAuthGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FunctionAuthGroupRepository extends JpaRepository<FunctionAuthGroup, Long> {

    List<FunctionAuthGroup> findByFunctionIdAndUseYn(String functionId, String useYn);
}