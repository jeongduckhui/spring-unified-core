package com.example.demo.soap.saaj.v2.dto.sales;

import lombok.Data;

@Data
public class SalesRequest {
    private long salesAmount;
    private long salesPrice;
    private String seller;
}