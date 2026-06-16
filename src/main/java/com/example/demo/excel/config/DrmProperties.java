package com.example.demo.excel.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

// application.yml의 excel.drm 설정을 담는 클래스.
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "excel.drm")
public class DrmProperties {

    // 업로드 원본 파일을 임시 저장할 디렉토리.
    private Path sourceDir;

    // DRM 복호화 결과 파일을 생성할 디렉토리.
    private Path decryptDir;

    // 복호화 처리 후 임시 파일 삭제 여부.
    private boolean deleteTempFile = true;
}