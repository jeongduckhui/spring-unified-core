package com.example.demo.commoncode.domain;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "PROCESS_CODE")
@Getter
public class ProcessCode {

    @Id
    @Column(name = "PROCESS_CD")
    private String processCd;

    @Column(name = "PROCESS_NAME")
    private String processName;

    @Column(name = "PROCESS_TYPE")
    private String processType;

    @Column(name = "USE_YN")
    private String useYn;

    @Column(name = "SORT_ORDER")
    private Integer sortOrder;
}