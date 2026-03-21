package com.example.demo.authgroup.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "function_auth_group")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class FunctionAuthGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "function_id", nullable = false)
    private String functionId;

    @Column(name = "auth_group_id", nullable = false)
    private Long authGroupId;

    @Column(name = "use_yn", length = 1)
    private String useYn;

    private String remarks;

    private LocalDateTime createdAt;
    private String createdBy;

    private LocalDateTime updatedAt;
    private String updatedBy;

    public boolean isActive() {
        return "Y".equalsIgnoreCase(useYn);
    }
}