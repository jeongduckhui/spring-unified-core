package com.example.demo.commoncode.domain;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "DISTRIBUTOR_CODE")
@Getter
public class DistributorCode {

    @Id
    @Column(name = "DISTRIBUTOR_CD")
    private String distributorCd;

    @Column(name = "DISTRIBUTOR_NAME")
    private String distributorName;

    @Column(name = "BUSINESS_TYPE")
    private String businessType;

    @Column(name = "REGION")
    private String region;

    @Column(name = "USE_YN")
    private String useYn;

    @Column(name = "SORT_ORDER")
    private Integer sortOrder;
}