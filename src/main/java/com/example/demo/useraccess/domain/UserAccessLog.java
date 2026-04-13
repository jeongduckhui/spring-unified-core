package com.example.demo.useraccess.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_access_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserAccessLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 사용자 아이디
    private Long userId;

    // 기능 아이디 (funcId)
    private String funcId;

    // 시스템 타입 코드
    private String systemTypeCode;

    // 접속 시작 일시
    private LocalDateTime accessStartAt;

    // 🔥 추가: 접속 종료 일시
    private LocalDateTime accessEndAt;

    // =========================
    // 🔥 도메인 메서드 (중요)
    // =========================

    /**
     * 접속 종료 처리
     */
    public void end() {
        this.accessEndAt = LocalDateTime.now();
    }

    /**
     * 이미 종료된 세션인지 확인
     */
    public boolean isEnded() {
        return this.accessEndAt != null;
    }
}