package com.example.demo.authgroup.repository;

import com.example.demo.authgroup.domain.AuthGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthGroupRepository extends JpaRepository<AuthGroup, Long> {
}