package com.example.demo.commoncode.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

@Entity
@Table(name = "COUNTRY")
@Getter
public class Country {

    @Id
    @Column(name = "COUNTRY_CD")
    private String countryCd;

    @Column(name = "COUNTRY_NAME")
    private String countryName;

    @Column(name = "USE_YN")
    private String useYn;
}