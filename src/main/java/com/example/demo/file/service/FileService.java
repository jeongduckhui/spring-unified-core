package com.example.demo.file.service;

import com.example.demo.common.exception.AuthException;
import com.example.demo.common.exception.BusinessException;
import com.example.demo.common.exception.ExceptionCode;
import com.example.demo.file.config.FileUploadProperties;
import com.example.demo.file.config.MultipartProperties;
import com.example.demo.file.domain.FileEntity;
import com.example.demo.file.dto.FileResponse;
import com.example.demo.file.repository.FileRepository;
import com.example.demo.file.storage.FileStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 파일 업로드, 다운로드, 조회, 삭제를 처리하는 서비스.
 *
 * <p>
 * 파일 바이너리는 FileStorage 구현체에 저장하고,
 * 파일 메타정보는 DB에 저장함.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    private final FileRepository fileRepository;

    private final FileStorage fileStorage;

    private final MultipartProperties multipartProperties;

    private final FileUploadProperties fileUploadProperties;

    private final Tika tika = new Tika();

    /**
     * 파일을 Resource로 로드함.
     *
     * @param file 파일 엔티티
     * @return 다운로드 응답에 사용할 Resource
     */
    public Resource load(FileEntity file) {
        // 저장소 구현체를 통해 실제 파일 Resource를 로드함.
        return fileStorage.load(
                file.getStoredName(),
                file.getFilePath()
        );
    }

    /**
     * 파일 다운로드 URL을 조회함.
     *
     * <p>
     * S3는 presigned URL을 반환하고,
     * NAS/Local은 Controller에서 Resource 스트리밍으로 처리함.
     * </p>
     *
     * @param fileId 파일 ID
     * @param userId 사용자 ID
     * @return 파일 다운로드 URL
     */
    public String download(Long fileId, Long userId) {
        // 삭제되지 않은 파일 정보를 DB에서 조회함.
        FileEntity file = fileRepository.findByIdAndDeletedFalse(fileId)
                .orElseThrow(() -> new BusinessException(ExceptionCode.FILE_NOT_FOUND));

        // 파일 소유자와 요청 사용자가 같은지 확인함.
        if (!file.getUserId().equals(userId)) {
            // 소유자가 다르면 권한 없음 예외를 던짐.
            throw new AuthException(ExceptionCode.FORBIDDEN);
        }

        // 메일 첨부파일이고 만료일자가 존재하는지 확인함.
        if (Boolean.TRUE.equals(file.getMailAttachment()) && file.getExpireAt() != null) {
            // 만료일자가 현재 시각보다 이전인지 확인함.
            if (file.getExpireAt().isBefore(LocalDateTime.now())) {
                // 만료된 파일이면 파일 만료 예외를 던짐.
                throw new BusinessException(ExceptionCode.FILE_EXPIRED);
            }
        }

        // 저장소 구현체에서 다운로드 URL을 생성함.
        return fileStorage.getDownloadUrl(
                file.getStoredName(),
                file.getFilePath(),
                file.getOriginalName()
        );
    }

    /**
     * 파일 ID로 파일 정보를 조회함.
     *
     * @param fileId 파일 ID
     * @return 파일 엔티티
     */
    public FileEntity getFile(Long fileId) {
        // 삭제되지 않은 파일 정보를 DB에서 조회함.
        return fileRepository.findByIdAndDeletedFalse(fileId)
                .orElseThrow(() -> new BusinessException(ExceptionCode.FILE_NOT_FOUND));
    }

    /**
     * 단일 파일을 업로드함.
     *
     * @param file 업로드 파일
     * @param userId 사용자 ID
     * @param ip 요청 IP
     * @param userAgent 사용자 에이전트
     * @param deviceId 디바이스 ID
     * @return 저장된 파일 엔티티
     */
    @Transactional
    public FileEntity upload(
            MultipartFile file,
            Long userId,
            String ip,
            String userAgent,
            String deviceId
    ) {
        // 업로드 파일 유효성 검증 수행.
        validateWithTika(file);

        // 원본 파일명을 정리함.
        String originalName = sanitizeFileName(file.getOriginalFilename());

        // 원본 파일명에서 확장자를 추출함.
        String extension = extractExtension(originalName).toLowerCase(Locale.ROOT);

        // UUID 기반 저장 파일명을 생성함.
        String storedName = UUID.randomUUID() + "." + extension;

        // 날짜 기반 상대 저장 경로를 생성함.
        String path = generatePath();

        try {
            // 실제 파일 바이너리를 저장소에 저장함.
            fileStorage.save(file, storedName, path);

            // DB에 저장할 파일 엔티티를 생성함.
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

            // 파일 메타정보를 DB에 저장함.
            return fileRepository.save(entity);

        } catch (BusinessException e) {
            // DB 저장 전후 예외 발생 시 이미 저장된 물리 파일 삭제 시도.
            safeDeleteUploadedFile(storedName, path);

            // 기존 비즈니스 예외를 그대로 던짐.
            throw e;

        } catch (Exception e) {
            // DB 저장 전후 예외 발생 시 이미 저장된 물리 파일 삭제 시도.
            safeDeleteUploadedFile(storedName, path);

            // 파일 업로드 실패 예외로 감싸서 던짐.
            throw new BusinessException(ExceptionCode.FILE_UPLOAD_FAILED, e);
        }
    }

    /**
     * 여러 파일을 업로드함.
     *
     * @param files 업로드 파일 목록
     * @param userId 사용자 ID
     * @param ip 요청 IP
     * @param userAgent 사용자 에이전트
     * @param deviceId 디바이스 ID
     * @return 저장된 파일 엔티티 목록
     */
    @Transactional
    public List<FileEntity> uploadMultiple(
            List<MultipartFile> files,
            Long userId,
            String ip,
            String userAgent,
            String deviceId
    ) {
        // 업로드 파일 목록이 비어 있는지 확인함.
        if (files == null || files.isEmpty()) {
            // 파일이 없으면 파일 비어 있음 예외를 던짐.
            throw new BusinessException(ExceptionCode.FILE_EMPTY);
        }

        // 각 파일을 단일 업로드 로직으로 처리함.
        return files.stream()
                .map(file -> upload(file, userId, ip, userAgent, deviceId))
                .toList();
    }

    /**
     * 내 파일 목록을 조회함.
     *
     * @param userId 사용자 ID
     * @return 파일 응답 목록
     */
    public List<FileResponse> getMyFiles(Long userId) {
        // 사용자 ID 기준으로 삭제되지 않은 파일 목록을 최신순으로 조회함.
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

    /**
     * 메일 첨부용 파일을 업로드함.
     *
     * @param file 업로드 파일
     * @param userId 사용자 ID
     * @param ip 요청 IP
     * @param userAgent 사용자 에이전트
     * @param deviceId 디바이스 ID
     * @return 저장된 파일 엔티티
     */
    @Transactional
    public FileEntity uploadForMail(
            MultipartFile file,
            Long userId,
            String ip,
            String userAgent,
            String deviceId
    ) {
        // 일반 파일 업로드 로직으로 파일을 저장함.
        FileEntity entity = upload(file, userId, ip, userAgent, deviceId);

        // 메일 첨부파일 여부를 true로 설정함.
        entity.setMailAttachment(true);

        // 메일 첨부파일 만료일자를 현재 시각 기준 3일 뒤로 설정함.
        entity.setExpireAt(LocalDateTime.now().plusDays(3));

        // 변경된 메일 첨부 정보를 DB에 저장함.
        return fileRepository.save(entity);
    }

    /**
     * 파일을 삭제 처리함.
     *
     * @param fileId 파일 ID
     * @param userId 사용자 ID
     */
    @Transactional
    public void delete(Long fileId, Long userId) {
        // 삭제되지 않은 파일 정보를 DB에서 조회함.
        FileEntity file = fileRepository.findByIdAndDeletedFalse(fileId)
                .orElseThrow(() -> new BusinessException(ExceptionCode.FILE_NOT_FOUND));

        // 파일 소유자와 요청 사용자가 같은지 확인함.
        if (!file.getUserId().equals(userId)) {
            // 소유자가 다르면 권한 없음 예외를 던짐.
            throw new AuthException(ExceptionCode.FORBIDDEN);
        }

        // 파일 엔티티를 삭제 상태로 변경함.
        file.delete();

        // 삭제 상태로 변경된 파일 엔티티를 DB에 저장함.
        fileRepository.save(file);

        // DB에서 삭제된 파일의 물리 파일을 삭제함.
        // 파일삭제 배치가 없을 경우 DB 상태변경 후 바로 물리 삭제
        // 물리파일 삭제 시 예외를 던지기 때문에 transaction rollback이 일어남
        // 반대로 DB 삭제 실패 시 물리파일은 정상적으로 삭제됨
        // 이후 삭제 재시도 시 DB 삭제 성공 후 삭제할 물리파일이 없더라도 정상 흐름 유지
        deleteStoredFileOrThrow(file.getStoredName(), file.getFilePath());
    }

    /**
     * 메일 첨부파일 다운로드 URL을 조회함.
     *
     * @param file 파일 엔티티
     * @return 다운로드 URL
     */
    public String getDownloadUrlForMail(FileEntity file) {
        // 저장소 구현체에서 다운로드 URL을 생성함.
        return fileStorage.getDownloadUrl(
                file.getStoredName(),
                file.getFilePath(),
                file.getOriginalName()
        );
    }

    /**
     * Tika를 사용해서 업로드 파일을 검증함.
     *
     * <p>
     * 확장자 검증과 실제 파일 내용 기반 MIME 타입 검증을 함께 수행함.
     * </p>
     *
     * @param file 업로드 파일
     */
    private void validateWithTika(MultipartFile file) {
        // 파일 공통 검증을 수행하고 확장자를 반환받음.
        String ext = validateCommonAndReturnExtension(file);

        try {
            // Tika로 파일 내용을 분석해서 MIME 타입을 감지함.
            String detectedType = tika.detect(file.getInputStream()).toLowerCase(Locale.ROOT);

            // 감지된 MIME 타입이 허용 목록에 포함되어 있는지 확인함.
            if (!getAllowedMimeTypes().contains(detectedType)) {
                // 허용되지 않은 MIME 타입이면 파일 타입 오류 예외를 던짐.
                throw new BusinessException(ExceptionCode.FILE_INVALID_TYPE);
            }

        } catch (BusinessException e) {
            // 이미 비즈니스 예외로 판단된 경우에는 그대로 던짐.
            throw e;

        } catch (Exception e) {
            // MIME 타입 감지 중 오류가 발생하면 파일 타입 오류 예외로 감싸서 던짐.
            throw new BusinessException(ExceptionCode.FILE_INVALID_TYPE, e);
        }
    }

    /**
     * Tika 없이 업로드 파일을 검증함.
     *
     * <p>
     * 외부 라이브러리 사용이 어려운 경우 클라이언트가 전달한 Content-Type 기준으로 검증함.
     * 단, 이 방식은 조작 가능성이 있으므로 Tika 방식보다 신뢰도가 낮음.
     * </p>
     *
     * @param file 업로드 파일
     */
    private void validateWithoutTika(MultipartFile file) {
        // 파일 공통 검증을 수행하고 확장자를 반환받음.
        String ext = validateCommonAndReturnExtension(file);

        // 업로드 요청에 포함된 Content-Type 값을 기본값 처리해서 가져옴.
        String contentType = defaultContentType(file.getContentType()).toLowerCase(Locale.ROOT);

        // Content-Type 값이 허용 MIME 타입 목록에 포함되어 있는지 확인함.
        if (!getAllowedMimeTypes().contains(contentType)) {
            // 허용되지 않은 MIME 타입이면 파일 타입 오류 예외를 던짐.
            throw new BusinessException(ExceptionCode.FILE_INVALID_TYPE);
        }
    }

    /**
     * 파일 공통 검증을 수행하고 확장자를 반환함.
     *
     * @param file 업로드 파일
     * @return 검증된 확장자
     */
    private String validateCommonAndReturnExtension(MultipartFile file) {
        // 파일이 없거나 비어 있는지 확인함.
        if (file == null || file.isEmpty()) {
            // 파일이 없으면 파일 비어 있음 예외를 던짐.
            throw new BusinessException(ExceptionCode.FILE_EMPTY);
        }

        // 설정된 최대 파일 크기를 byte 단위로 가져옴.
        long maxSize = multipartProperties.getMaxFileSize().toBytes();

        // 업로드 파일 크기가 최대 크기를 초과하는지 확인함.
        if (file.getSize() > maxSize) {
            // 최대 크기를 초과하면 파일 크기 초과 예외를 던짐.
            throw new BusinessException(ExceptionCode.FILE_SIZE_EXCEEDED);
        }

        // 원본 파일명을 안전한 형태로 정리함.
        String originalFilename = sanitizeFileName(file.getOriginalFilename());

        // 원본 파일명에서 확장자를 추출함.
        String ext = extractExtension(originalFilename).toLowerCase(Locale.ROOT);

        // 확장자가 허용 확장자 목록에 포함되어 있는지 확인함.
        if (!getAllowedExtensions().contains(ext)) {
            // 허용되지 않은 확장자이면 파일 확장자 오류 예외를 던짐.
            throw new BusinessException(ExceptionCode.FILE_INVALID_EXTENSION);
        }

        // 검증된 확장자를 반환함.
        return ext;
    }

    /**
     * 원본 파일명을 안전한 형태로 정리함.
     *
     * @param filename 원본 파일명
     * @return 정리된 파일명
     */
    private String sanitizeFileName(String filename) {
        // 파일명이 없거나 공백인지 확인함.
        if (filename == null || filename.isBlank()) {
            // 파일명이 유효하지 않으면 파일명 오류 예외를 던짐.
            throw new BusinessException(ExceptionCode.FILE_NAME_INVALID);
        }

        // 경로 구분자, 개행, 탭 문자를 제거용 문자로 치환함.
        String sanitized = filename
                .replaceAll("[\\\\/\\r\\n\\t]", "_")
                .replaceAll("\\s+", " ")
                .trim();

        // 정리된 파일명이 비었거나 특수 경로명인지 확인함.
        if (sanitized.isBlank() || ".".equals(sanitized) || "..".equals(sanitized)) {
            // 정리된 파일명이 유효하지 않으면 파일명 오류 예외를 던짐.
            throw new BusinessException(ExceptionCode.FILE_NAME_INVALID);
        }

        // 정리된 파일명을 반환함.
        return sanitized;
    }

    /**
     * 파일명에서 확장자를 추출함.
     *
     * @param filename 파일명
     * @return 확장자
     */
    private String extractExtension(String filename) {
        // 파일명이 없거나 점 문자가 없는지 확인함.
        if (filename == null || !filename.contains(".")) {
            // 확장자가 없으면 파일 확장자 오류 예외를 던짐.
            throw new BusinessException(ExceptionCode.FILE_INVALID_EXTENSION);
        }

        // 파일명에서 마지막 점 위치를 찾음.
        int lastDotIndex = filename.lastIndexOf(".");

        // 마지막 점이 파일명 끝에 있는지 확인함.
        if (lastDotIndex == filename.length() - 1) {
            // 점 뒤에 확장자가 없으면 파일 확장자 오류 예외를 던짐.
            throw new BusinessException(ExceptionCode.FILE_INVALID_EXTENSION);
        }

        // 마지막 점 뒤의 문자열을 확장자로 반환함.
        return filename.substring(lastDotIndex + 1);
    }

    /**
     * 날짜 기반 파일 저장 경로를 생성함.
     *
     * @return 날짜 기반 상대 경로
     */
    private String generatePath() {
        // 현재 날짜와 시간을 가져옴.
        LocalDateTime now = LocalDateTime.now();

        // 연도와 월을 조합해 상대 경로를 생성함.
        return now.getYear() + "/" + now.getMonthValue() + "/";
    }

    /**
     * Content-Type 기본값을 반환함.
     *
     * @param contentType 요청 Content-Type
     * @return 기본값이 적용된 Content-Type
     */
    private String defaultContentType(String contentType) {
        // Content-Type이 없거나 공백이면 application/octet-stream을 반환함.
        return (contentType == null || contentType.isBlank())
                ? "application/octet-stream"
                : contentType;
    }

    /**
     * 업로드 중 실패한 물리 파일 삭제를 시도함.
     *
     * @param storedName 저장 파일명
     * @param path 저장 상대 경로
     */
    private void safeDeleteUploadedFile(String storedName, String path) {
        try {
            // 저장소 구현체를 통해 물리 파일 삭제를 시도함.
            fileStorage.delete(storedName, path);

        } catch (Exception ignored) {
            // 보정 삭제 실패는 원래 예외를 가리지 않기 위해 무시함.
        }
    }

    /**
     * 삭제 처리된 파일의 물리 파일 삭제를 시도함.
     *
     * @param storedName 저장 파일명
     * @param path 저장 상대 경로
     */
    private void deleteStoredFileOrThrow(String storedName, String path) {
        try {
            // 저장소 구현체를 통해 물리 파일 삭제를 시도함.
            fileStorage.delete(storedName, path);

        } catch (Exception e) {
            // 물리 파일 삭제 실패 정보를 로그로 남김.
            log.error("물리 파일 삭제 실패. storedName={}, path={}", storedName, path, e);

            // 물리 파일 삭제 실패 예외를 던짐.
            throw new BusinessException(ExceptionCode.FILE_DELETE_FAILED, e);
        }
    }

    /**
     * 허용 확장자 목록을 소문자 Set으로 반환함.
     *
     * @return 허용 확장자 Set
     */
    private Set<String> getAllowedExtensions() {
        // 설정에서 읽은 확장자 목록을 소문자로 변환해서 반환함.
        return fileUploadProperties.getAllowedExtensions()
                .stream()
                .map(value -> value.toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
    }

    /**
     * 허용 MIME 타입 목록을 소문자 Set으로 반환함.
     *
     * @return 허용 MIME 타입 Set
     */
    private Set<String> getAllowedMimeTypes() {
        // 설정에서 읽은 MIME 타입 목록을 소문자로 변환해서 반환함.
        return fileUploadProperties.getAllowedMimeTypes()
                .stream()
                .map(value -> value.toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
    }
}