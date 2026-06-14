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

/**
 * nas 마운트 경로 확인 필요 🎃🎃🎃
 * 파일 사이즈, 유효한 타입 등 파일 설정값 어떻게 설정할 지 확인 필요 🎃🎃🎃
 * MIME 타입 체크용 TIKA 라이브러리 사용 여부 체크
 * ~properties.java 파일 하나로 통일하는 방향으로 코딩하기
 * MultipartProperties.java는 필드가 하나이니 @Value로 받는 것 고려하기
 *
 * 삭제 프로세스 설명하기.
 * db상태만 바꾸고 배치로 물리삭제하는 방식도 많은데, 여기서는 즉시 삭제로 하고 있다는 것 설명
 *
 * [ DB 상태 변경 중 에러 발생 ]
 * 1. 삭제 요청
 * 2. DB deleted=true 처리 중 에러 발생
 * 3. 트랜잭션 롤백
 * 4. 물리 파일 삭제 로직까지 도달하지 않음
 *
 * [ 물리 파일 삭제 성공 후 DB 커밋 실패 ]
 * 1. 삭제 요청
 * 2. DB deleted=true 처리
 * 3. fileRepository.save(file)
 * 4. 물리 파일 삭제 성공
 * 5. 메서드 종료
 * 6. 트랜잭션 커밋 시점에 DB 커밋 실패
 * 7. DB는 롤백
 * 8. 물리 파일은 이미 삭제됨
 * 9. 삭제 재시도 시 Files.deleteIfExists(...) 이므로 예외 없이 통과
 *
 * [ 물리 파일 삭제 실패 시 DB 롤백 ]
 * 1. 삭제 요청
 * 2. DB deleted=true 처리
 * 3. fileRepository.save(file)
 * 4. 물리 파일 삭제 실패
 * 5. 트랜잭션 롤백
 */

/**
 * [ 확인해야 하는 사항 ] 🎃🎃🎃
 * 1. 실제 NAS 마운트 경로 확인
 * 2. WAS 실행 계정의 NAS 읽기/쓰기/삭제 권한 확인
 * 3. 운영 yml/properties에 file.storage.type=nas 설정
 * 4. 운영 yml/properties에 file.nas.base-dir=실제 마운트 경로 설정
 * 5. 허용 확장자/MIME 타입 목록 PL 확인
 * 6. Tika 사용 여부 PL 확인
 * 7. 삭제 정책 최종 확인
 *    - 현재: DB 상태 변경 + 물리 파일 즉시 삭제
 *    - 물리 삭제 실패 시 예외 발생
 * 8. /test/files → /files 원복
 * 9. 프론트 메뉴에서 S3/NAS 화면을 실무에서 둘 다 둘지, NAS만 둘지 결정
 * 
 * 🎃 기존 ccc 어드민 파일 서비스/레포지토리 확인해서 적용해야 함
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/files")
//@RequestMapping("/test/files")
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
//        Long userId = 1111L;

        FileEntity file = fileService.getFile(id);

        /*
        // 본인만 파일 다운로드 가능하다면
        if (!file.getUserId().equals(userId)) {
            throw new AuthException(ExceptionCode.FORBIDDEN);
        }
        */

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
        // 저장소에서 실제 파일 Resource를 로드함.
        Resource resource = fileService.load(file);

        // 원본 파일명을 UTF-8 방식으로 인코딩함.
        String encodedName = UriUtils.encode(file.getOriginalName(), StandardCharsets.UTF_8);

        // 파일 Content-Type이 없으면 기본 바이너리 타입으로 설정함.
        MediaType mediaType = file.getContentType() == null || file.getContentType().isBlank()
                ? MediaType.APPLICATION_OCTET_STREAM
                : MediaType.parseMediaType(file.getContentType());

        // 파일 다운로드 응답을 생성해서 반환함.
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedName)
                .contentType(mediaType)
                .contentLength(file.getFileSize())
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
//        Long userId = 1111L;

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
//        Long userId = 1111L;

        fileService.delete(id, userId);
    }
}