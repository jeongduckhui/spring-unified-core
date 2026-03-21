package com.example.demo.file.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class FileResponse {

    private Long fileId;
    private String originalName;
    private Long fileSize;
    private String contentType;
    private LocalDateTime createdAt;
    private String downloadUrl;
}