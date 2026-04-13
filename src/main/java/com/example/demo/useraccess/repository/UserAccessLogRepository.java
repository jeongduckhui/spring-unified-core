package com.example.demo.useraccess.repository;

import com.example.demo.useraccess.domain.UserAccessLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserAccessLogRepository extends JpaRepository<UserAccessLog, Long> {

    Optional<UserAccessLog> findTopByUserIdAndFuncIdOrderByAccessStartAtDesc(
            Long userId, String funcId
    );

    // 🔥 추가 (핵심)
    Optional<UserAccessLog> findTopByUserIdAndFuncIdAndAccessEndAtIsNullOrderByAccessStartAtDesc(
            Long userId, String funcId
    );

    Optional<UserAccessLog> findTopByUserIdAndAccessEndAtIsNullOrderByAccessStartAtDesc(Long userId);

    Optional<UserAccessLog> findTopByAccessEndAtIsNullOrderByAccessStartAtDesc();
}