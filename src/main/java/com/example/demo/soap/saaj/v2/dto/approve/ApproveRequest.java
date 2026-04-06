package com.example.demo.soap.saaj.v2.dto.approve;

import lombok.Data;

import java.util.List;

@Data
public class ApproveRequest {
    private String callUser;
    private String amount;
    private List<Item> items;

    @Data
    public static class Item {
        private String name;
        private String price;
    }
}