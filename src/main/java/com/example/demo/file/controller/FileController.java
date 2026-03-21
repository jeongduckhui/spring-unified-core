package com.example.demo.file.controller;

import com.example.demo.common.exception.AuthException;
import com.example.demo.common.exception.ExceptionCode;
import com.example.demo.file.domain.FileEntity;
import com.example.demo.file.dto.FileResponse;
import com.example.demo.file.dto.FileUploadResponse;
import com.example.demo.file.service.FileService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 파일 관련 API Controller
 *
 * 제공 기능:
 * - 단일 파일 업로드
 * - 다중 파일 업로드
 * - 파일 다운로드
 * - 사용자 파일 목록 조회
 * - 파일 삭제
 *
 * 보안 고려사항:
 * - 사용자 인증 기반 접근
 * - 파일 다운로드 시 Content-Disposition 헤더 인코딩 처리
 * - 헤더 인젝션 방지 (파일명 sanitize)
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/files")
public class FileController {

    private final FileService fileService;

    @Value("${file.storage.type:local}")
    private String fileStorageType;

    /**
     * 파일 다운로드
     *
     * 보안 및 인코딩 처리:
     * - 사용자 권한 검증 (서비스 계층에서 수행)
     * - 파일명 CRLF Injection 방지
     * - RFC 5987 기반 filename* 인코딩 적용
     *
     * 중요 수정 사항:
     * - 기존 filename= 제거 (Tomcat 한글 깨짐 문제 대응)
     * - filename*=UTF-8 방식만 사용하도록 수정
     *
     * @param id 파일 ID
     * @param authentication 인증 객체
     * @return 다운로드용 Resource 응답
     */
    @GetMapping("/download/{id}")
    public ResponseEntity<?> download(@PathVariable Long id, Authentication authentication) {

        Long userId = Long.valueOf(authentication.getName());

        FileEntity file = fileService.getFile(id);

        if (!file.getUserId().equals(userId)) {
            throw new AuthException(ExceptionCode.FORBIDDEN);
        }

        // =========================
        // S3 → Redirect
        // =========================
        if ("s3".equals(fileStorageType)) {
            String url = fileService.download(id, userId);

            return ResponseEntity.ok(url);
        }

        // =========================
        // Local → Stream
        // =========================
        Resource resource = fileService.load(file);

        String encodedName =
                UriUtils.encode(file.getOriginalName(), StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename*=UTF-8''" + encodedName)
                .contentType(MediaType.parseMediaType(file.getContentType()))
                .body(resource);
    }

    /**
     * 단일 파일 업로드
     *
     * @param file 업로드 파일
     * @param authentication 인증 정보
     * @param request HTTP 요청
     * @return 업로드 결과 DTO
     */
    @PostMapping("/upload")
    public FileUploadResponse upload(
            @RequestParam("file") MultipartFile file,
            Authentication authentication,
            HttpServletRequest request
    ) {

        Long userId = Long.valueOf(authentication.getName());

        FileEntity saved = fileService.upload(
                file,
                userId,
                request.getRemoteAddr(),
                request.getHeader("User-Agent"),
                request.getHeader("X-Device-Id")
        );

        return FileUploadResponse.builder()
                .fileId(saved.getId())
                .originalName(saved.getOriginalName())
                .downloadUrl("/files/download/" + saved.getId())
                .build();
    }

    /**
     * 다중 파일 업로드
     *
     * 요청 형식: multipart/form-data (key: files)
     * 처리 방식: 각 파일을 단일 업로드 로직으로 위임
     *
     * @param files 업로드 파일 리스트
     * @param authentication 인증 정보
     * @param request HTTP 요청
     * @return 업로드 결과 리스트
     */
    @PostMapping("/upload-multiple")
    public List<FileUploadResponse> uploadMultiple(
            @RequestParam("files") List<MultipartFile> files,
            Authentication authentication,
            HttpServletRequest request
    ) {

        Long userId = Long.valueOf(authentication.getName());

        return fileService.uploadMultiple(
                        files,
                        userId,
                        request.getRemoteAddr(),
                        request.getHeader("User-Agent"),
                        request.getHeader("X-Device-Id")
                ).stream()
                .map(saved -> FileUploadResponse.builder()
                        .fileId(saved.getId())
                        .originalName(saved.getOriginalName())
                        .downloadUrl("/files/download/" + saved.getId())
                        .build()
                )
                .collect(Collectors.toList());
    }

    /**
     * 로그인 사용자의 파일 목록 조회
     *
     * @param authentication 인증 정보
     * @return 파일 리스트
     */
    @GetMapping("/my")
    public List<FileResponse> getMyFiles(Authentication authentication) {
        Long userId = Long.valueOf(authentication.getName());
        return fileService.getMyFiles(userId);
    }

    /**
     * 파일 삭제 (소프트 삭제)
     *
     * @param id 파일 ID
     * @param authentication 인증 정보
     */
    @DeleteMapping("/{id}")
    public void delete(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long userId = Long.valueOf(authentication.getName());
        fileService.delete(id, userId);
    }
}