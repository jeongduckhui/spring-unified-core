package com.example.demo.authgroup.repository;

import com.example.demo.authgroup.domain.UserAuthGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserAuthGroupRepository extends JpaRepository<UserAuthGroup, Long> {

    List<UserAuthGroup> findByUserIdAndGrantYn(Long userId, String grantYn);
}