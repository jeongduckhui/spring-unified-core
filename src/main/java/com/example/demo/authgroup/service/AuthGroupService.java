package com.example.demo.authgroup.service;

import java.util.Set;

public interface AuthGroupService {

    /**
     * 사용자 권한그룹 조회
     */
    Set<Long> getUserAuthGroupIds(Long userId);
}