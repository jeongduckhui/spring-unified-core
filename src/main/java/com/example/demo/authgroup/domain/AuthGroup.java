package com.example.demo.authgroup.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "auth_group")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AuthGroup {

    @Id
    @Column(name = "auth_group_id")
    private Long authGroupId;

    @Column(name = "company_code", nullable = false, length = 20)
    private String companyCode;

    @Column(name = "system_type_code", nullable = false, length = 20)
    private String systemTypeCode;

    @Column(name = "auth_group_name", nullable = false)
    private String authGroupName;

    @Column(name = "admin_yn", length = 1)
    private String adminYn; // Y/N

    @Column(name = "use_yn", length = 1)
    private String useYn; // Y/N

    private LocalDateTime createdAt;
    private String createdBy;

    private LocalDateTime updatedAt;
    private String updatedBy;
}