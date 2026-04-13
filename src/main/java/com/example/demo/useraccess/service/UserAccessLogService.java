package com.example.demo.useraccess.service;

import com.example.demo.useraccess.domain.UserAccessLog;
import com.example.demo.useraccess.repository.UserAccessLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserAccessLogService {

    private final UserAccessLogRepository repository;

    public void save(Long userId, String funcId, String systemTypeCode) {

        UserAccessLog log = UserAccessLog.builder()
                .userId(userId)
                .funcId(funcId)
                .systemTypeCode(systemTypeCode)
                .accessStartAt(LocalDateTime.now())
                .build();

        repository.save(log);
    }

    // 🔥 정확 종료 (탭 닫기)
    @Transactional
    public void endByFuncId(Long userId, String funcId) {

        UserAccessLog log = repository
                .findTopByUserIdAndFuncIdAndAccessEndAtIsNullOrderByAccessStartAtDesc(userId, funcId)
                .orElse(null);

        if (log == null) return;

        log.end();
    }

    // 🔥 fallback (브라우저 종료)
    @Transactional
    public void endLastActive(Long userId) {

        UserAccessLog log;

        if (userId != null) {
            log = repository
                    .findTopByUserIdAndAccessEndAtIsNullOrderByAccessStartAtDesc(userId)
                    .orElse(null);
        } else {
            log = repository
                    .findTopByAccessEndAtIsNullOrderByAccessStartAtDesc()
                    .orElse(null);
        }

        if (log == null) return;

        log.end();
    }
}