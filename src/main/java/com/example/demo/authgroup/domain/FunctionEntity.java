package com.example.demo.authgroup.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "function")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class FunctionEntity {

    @Id
    @Column(name = "function_id")
    private String functionId;

    @Column(name = "system_type_code")
    private String systemTypeCode;

    @Column(name = "program_id")
    private String programId;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Column(name = "level_value")
    private Integer levelValue;

    @Column(name = "use_yn")
    private String useYn;

    @Column(name = "parent_function_id")
    private String parentFunctionId;

    private LocalDateTime createdAt;
    private String createdBy;

    private LocalDateTime updatedAt;
    private String updatedBy;
}