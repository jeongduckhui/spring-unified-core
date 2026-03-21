package com.example.demo.authgroup.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_auth_group")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserAuthGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "auth_group_id", nullable = false)
    private Long authGroupId;

    @Column(name = "request_type_code")
    private String requestTypeCode;

    @Column(name = "request_yn", length = 1)
    private String requestYn;

    @Column(name = "grant_type_code")
    private String grantTypeCode;

    @Column(name = "grant_yn", length = 1)
    private String grantYn; // 🔥 실제 권한 여부

    @Column(name = "cancel_type_code")
    private String cancelTypeCode;

    private String remarks;

    private LocalDateTime createdAt;
    private String createdBy;

    private LocalDateTime updatedAt;
    private String updatedBy;

    /**
     * 실제 권한 보유 여부
     */
    public boolean isGranted() {
        return "Y".equalsIgnoreCase(grantYn);
    }
}