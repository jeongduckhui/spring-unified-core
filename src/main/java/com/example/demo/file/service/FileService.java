package com.example.demo.file.service;

import com.example.demo.common.exception.AuthException;
import com.example.demo.common.exception.BusinessException;
import com.example.demo.common.exception.ExceptionCode;
import com.example.demo.file.config.MultipartProperties;
import com.example.demo.file.domain.FileEntity;
import com.example.demo.file.dto.FileResponse;
import com.example.demo.file.repository.FileRepository;
import com.example.demo.file.storage.FileStorage;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FileService {

    private final FileRepository fileRepository;
    private final FileStorage fileStorage;
    private final MultipartProperties multipartProperties;

    private final Tika tika = new Tika(); // 싱글 인스턴스

    @Value("${file.upload-dir}")
    private String baseDir;

    @Value("${file.allowed-extensions}")
    private String allowedExtensions;

    private Set<String> allowedExtSet;

    // ✅ MIME 화이트리스트 추가 (실무 핵심)
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/gif",
            "application/pdf",
            "text/plain"
    );

    @PostConstruct
    public void init() {
        allowedExtSet = Arrays.stream(allowedExtensions.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
    }

    public Resource load(FileEntity file) {
        return fileStorage.load(
                file.getStoredName(),
                file.getFilePath()
        );
    }

    public String download(Long fileId, Long userId) {

        FileEntity file = fileRepository.findByIdAndDeletedFalse(fileId)
                .orElseThrow(() -> new BusinessException(ExceptionCode.FILE_NOT_FOUND));

        if (!file.getUserId().equals(userId)) {
            throw new AuthException(ExceptionCode.FORBIDDEN);
        }

        if (Boolean.TRUE.equals(file.getMailAttachment())  && file.getExpireAt() != null) {
            if (file.getExpireAt().isBefore(LocalDateTime.now())) {
                throw new BusinessException(ExceptionCode.FILE_EXPIRED);
            }
        }

        return fileStorage.getDownloadUrl(
                file.getStoredName(),
                file.getFilePath(),
                file.getOriginalName()
        );
    }

    public FileEntity getFile(Long fileId) {
        return fileRepository.findByIdAndDeletedFalse(fileId)
                .orElseThrow(() -> new BusinessException(ExceptionCode.FILE_NOT_FOUND));
    }

    @Transactional
    public FileEntity upload(
            MultipartFile file,
            Long userId,
            String ip,
            String userAgent,
            String deviceId
    ) {
        validate(file);

        String originalName = sanitizeFileName(file.getOriginalFilename());
        String extension = extractExtension(originalName).toLowerCase();
        String storedName = UUID.randomUUID() + "." + extension;
        String path = generatePath();

        try {
            fileStorage.save(file, storedName, path);

            FileEntity entity = FileEntity.builder()
                    .userId(userId)
                    .originalName(originalName)
                    .storedName(storedName)
                    .filePath(path)
                    .extension(extension)
                    .contentType(defaultContentType(file.getContentType()))
                    .fileSize(file.getSize())
                    .ipAddress(ip)
                    .userAgent(userAgent)
                    .deviceId(deviceId)
                    .build();

            return fileRepository.save(entity);

        } catch (BusinessException e) {
            safeDeleteUploadedFile(storedName, path);
            throw e;
        } catch (Exception e) {
            safeDeleteUploadedFile(storedName, path);
            throw new BusinessException(ExceptionCode.FILE_UPLOAD_FAILED, e);
        }
    }

    @Transactional
    public List<FileEntity> uploadMultiple(
            List<MultipartFile> files,
            Long userId,
            String ip,
            String userAgent,
            String deviceId
    ) {
        if (files == null || files.isEmpty()) {
            throw new BusinessException(ExceptionCode.FILE_EMPTY);
        }

        return files.stream()
                .map(file -> upload(file, userId, ip, userAgent, deviceId))
                .toList();
    }

    public List<FileResponse> getMyFiles(Long userId) {
        return fileRepository
                .findByUserIdAndDeletedFalseOrderByCreatedAtDesc(userId)
                .stream()
                .map(file -> FileResponse.builder()
                        .fileId(file.getId())
                        .originalName(file.getOriginalName())
                        .fileSize(file.getFileSize())
                        .contentType(file.getContentType())
                        .createdAt(file.getCreatedAt())
                        .downloadUrl("/files/download/" + file.getId())
                        .build())
                .toList();
    }

    @Transactional
    public FileEntity uploadForMail(
            MultipartFile file,
            Long userId,
            String ip,
            String userAgent,
            String deviceId
    ) {
        FileEntity entity = upload(file, userId, ip, userAgent, deviceId);

        entity.setMailAttachment(true);
        entity.setExpireAt(LocalDateTime.now().plusDays(3));

        return fileRepository.save(entity);
    }

    @Transactional
    public void delete(Long fileId, Long userId) {
        FileEntity file = fileRepository.findByIdAndDeletedFalse(fileId)
                .orElseThrow(() -> new BusinessException(ExceptionCode.FILE_NOT_FOUND));

        if (!file.getUserId().equals(userId)) {
            throw new AuthException(ExceptionCode.FORBIDDEN);
        }

        file.delete();
        fileRepository.save(file);
    }

    public String getDownloadUrlForMail(FileEntity file) {
        return fileStorage.getDownloadUrl(
                file.getStoredName(),
                file.getFilePath(),
                file.getOriginalName()
        );
    }

    // =========================
    // 핵심: 파일 검증 강화
    // =========================
    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ExceptionCode.FILE_EMPTY);
        }

        long maxSize = multipartProperties.getMaxFileSize().toBytes();

        if (file.getSize() > maxSize) {
            throw new BusinessException(ExceptionCode.FILE_SIZE_EXCEEDED);
        }

        String originalFilename = sanitizeFileName(file.getOriginalFilename());
        String ext = extractExtension(originalFilename).toLowerCase();

        if (!allowedExtSet.contains(ext)) {
            throw new BusinessException(ExceptionCode.FILE_INVALID_EXTENSION);
        }

        // Tika MIME 검증 추가
        try {
            String detectedType = tika.detect(file.getInputStream());

            if (!ALLOWED_MIME_TYPES.contains(detectedType)) {
                throw new BusinessException(ExceptionCode.FILE_INVALID_TYPE);
            }

        } catch (Exception e) {
            throw new BusinessException(ExceptionCode.FILE_INVALID_TYPE, e);
        }
    }

    private String sanitizeFileName(String filename) {
        if (filename == null || filename.isBlank()) {
            throw new BusinessException(ExceptionCode.FILE_NAME_INVALID);
        }

        String sanitized = filename
                .replaceAll("[\\\\/\\r\\n\\t]", "_")
                .replaceAll("\\s+", " ")
                .trim();

        if (sanitized.isBlank() || ".".equals(sanitized) || "..".equals(sanitized)) {
            throw new BusinessException(ExceptionCode.FILE_NAME_INVALID);
        }

        return sanitized;
    }

    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            throw new BusinessException(ExceptionCode.FILE_INVALID_EXTENSION);
        }

        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex == filename.length() - 1) {
            throw new BusinessException(ExceptionCode.FILE_INVALID_EXTENSION);
        }

        return filename.substring(lastDotIndex + 1);
    }

    private String generatePath() {
        LocalDateTime now = LocalDateTime.now();
        return now.getYear() + "/" + now.getMonthValue() + "/";
    }

    private String defaultContentType(String contentType) {
        return (contentType == null || contentType.isBlank())
                ? "application/octet-stream"
                : contentType;
    }

    private void safeDeleteUploadedFile(String storedName, String path) {
        try {
            fileStorage.delete(storedName, path);
        } catch (Exception ignored) {
        }
    }
}