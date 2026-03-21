package com.example.demo.authgroup.repository;

import com.example.demo.authgroup.domain.FunctionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FunctionRepository extends JpaRepository<FunctionEntity, String> {

    List<FunctionEntity> findByUseYn(String useYn);

}