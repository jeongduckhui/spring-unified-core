package com.example.demo.file.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "files")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 업로드 사용자 ID (SSO userId)
     */
    @Column(nullable = false)
    private Long userId;

    /**
     * 원본 파일명 (사용자가 업로드한 이름)
     */
    @Column(nullable = false)
    private String originalName;

    /**
     * 저장 파일명 (UUID)
     */
    @Column(nullable = false, unique = true)
    private String storedName;

    /**
     * 저장 경로 (디렉토리 포함)
     */
    @Column(nullable = false)
    private String filePath;

    /**
     * 파일 확장자
     */
    @Column(nullable = false)
    private String extension;

    /**
     * MIME 타입 (image/png 등)
     */
    @Column(nullable = false)
    private String contentType;

    /**
     * 파일 크기 (bytes)
     */
    @Column(nullable = false)
    private Long fileSize;

    /**
     * 삭제 여부 (Soft Delete)
     */
    @Column(nullable = false)
    private boolean deleted;

    /**
     * 삭제 시간
     */
    private LocalDateTime deletedAt;

    /**
     * 생성 시간
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 수정 시간
     */
    private LocalDateTime updatedAt;

    /**
     * 업로드 IP
     */
    private String ipAddress;

    /**
     * 업로드 User-Agent
     */
    private String userAgent;

    /**
     * 디바이스 ID (SSO 연동)
     */
    private String deviceId;

    /**
     * 메일 첨부파일 여부
     */
    @Column(name = "mail_attachment")
    private Boolean mailAttachment;

    /**
     * 메일 첨부파일 만료일
     */
    @Column(name = "expire_at")
    private LocalDateTime expireAt;
    
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.deleted = false;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void delete() {
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
    }
}