package com.example.demo.file.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 파일 업로드 정책 설정을 바인딩하는 클래스.
 *
 * <p>
 * properties 또는 yml에 정의된 file.* 설정 중
 * 업로드 검증에 필요한 설정을 읽음.
 * </p>
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "file")
public class FileUploadProperties {

    /**
     * 로컬 파일 저장 경로.
     *
     * <p>
     * LocalFileStorage에서 사용하는 기존 설정 유지.
     * </p>
     */
    private String uploadDir;

    /**
     * 업로드 허용 확장자 목록.
     *
     * <p>
     * 예: jpg,jpeg,png,pdf,txt,xlsx,xls,csv
     * </p>
     */
    private Set<String> allowedExtensions = new LinkedHashSet<>();

    /**
     * 업로드 허용 MIME 타입 목록.
     *
     * <p>
     * 예: image/jpeg,application/pdf,text/plain
     * </p>
     */
    private Set<String> allowedMimeTypes = new LinkedHashSet<>();
}