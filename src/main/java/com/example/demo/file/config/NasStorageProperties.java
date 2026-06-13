package com.example.demo.file.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * NAS 파일 저장소 설정을 바인딩하는 클래스.
 *
 * <p>
 * properties 또는 yml에 정의된 file.nas.* 설정을 읽음.
 * 예:
 * file.nas.base-dir=E:/myplayground/nas-upload/
 * </p>
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "file.nas")
public class NasStorageProperties {

    /**
     * NAS 기본 저장 경로.
     *
     * <p>
     * 로컬 테스트에서는 일반 로컬 폴더를 지정하고,
     * 운영에서는 NAS가 마운트된 경로를 지정.
     * </p>
     */
    private String baseDir;
}