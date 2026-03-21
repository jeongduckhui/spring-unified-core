package com.example.demo.file.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FileUploadResponse {

    private Long fileId;
    private String originalName;
    private String downloadUrl;
}