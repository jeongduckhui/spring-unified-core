package com.example.demo.excel.util;

import com.example.demo.excel.config.DrmProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

// DRM 복호화 공통 유틸 클래스.
@Slf4j
@Component
@RequiredArgsConstructor
public class DrmDecryptUtils {

    // application.yml의 DRM 디렉토리 설정값을 주입받음.
    private final DrmProperties drmProperties;

    /**
     * 업로드 파일을 DRM 복호화한 뒤 복호화된 파일 InputStream을 반환.
     *
     * @param file 업로드 파일
     * @return 복호화된 파일 InputStream
     */
    public InputStream decrypt(MultipartFile file) {

        // 업로드 파일 기본 검증을 수행.
        validateFile(file);

        // source 임시 파일 경로를 생성.
        Path sourceFilePath = createSourceFilePath(file);

        // decrypt 결과 파일 경로를 생성.
        Path decryptFilePath = createDecryptFilePath(file);

        // source 파일 경로 로그 출력
        log.info("sourceFile={}", sourceFilePath);

        // decrypt 파일 경로 로그 출력
        log.info("decryptFile={}", decryptFilePath);

        try {
            // source 디렉토리를 생성.
            Files.createDirectories(drmProperties.getSourceDir());

            // decrypt 디렉토리를 생성.
            Files.createDirectories(drmProperties.getDecryptDir());

            // MultipartFile을 source 임시 파일로 저장.
            file.transferTo(sourceFilePath.toFile());

            // source 파일을 입력 스트림으로 연다.
            try (
                    BufferedInputStream ins2 =
                            new BufferedInputStream(
                                    new FileInputStream(sourceFilePath.toFile()));

                    BufferedOutputStream out2 =
                            new BufferedOutputStream(
                                    new FileOutputStream(decryptFilePath.toFile()))
            ) {

                /*
                 * TODO 실제 DRM 솔루션 호출 위치
                 *
                 * 실제 실무 코드는 아래 형태로 들어가면 된다.
                 *
                 * int iRet = drmClient.decrypt(
                 *         ins2,
                 *         out2,
                 *         ... 나머지 파라미터
                 * );
                 *
                 * if (iRet != 0) {
                 *     throw new IllegalStateException("DRM 복호화에 실패했습니다. resultCode=" + iRet);
                 * }
                 */

                // 현재는 더미 복호화 처리.
                // 실제 DRM 연동 전까지는 source 파일 내용을 decrypt 파일로 그대로 복사.
                ins2.transferTo(out2);
            }

            // 복호화 결과 파일을 검증.
            validateDecryptFile(decryptFilePath);

            // 복호화 파일 InputStream 생성
            InputStream inputStream =
                    Files.newInputStream(decryptFilePath);

            // 파일 자동 삭제 InputStream 반환
            return new DeleteOnCloseInputStream(
                    inputStream,
                    decryptFilePath
            );

        } catch (IOException e) {

            // 파일 저장, 파일 읽기, 디렉토리 생성 중 발생한 오류를 처리.
            throw new IllegalStateException("DRM 복호화 파일 처리 중 오류가 발생했습니다.", e);

        } catch (Exception e) {

            // DRM 솔루션 호출 등 기타 오류를 처리.
            throw new IllegalStateException("DRM 복호화 처리 중 오류가 발생했습니다.", e);

        } finally {

            // 설정이 true이면 임시 파일을 삭제.
            if (drmProperties.isDeleteTempFile()) {

                // source 임시 파일을 삭제.
                deleteQuietly(sourceFilePath);

                /*
                 * 주의:
                 * decryptFilePath는 여기서 바로 삭제하면 안 됨.
                 * 위에서 Files.newInputStream(decryptFilePath)를 반환했기 때문.
                 *
                 * 운영에서 완전 삭제까지 하려면
                 * InputStream close 시점에 삭제되는 커스텀 InputStream 구조로 확장해야 함.
                 */
            }
        }
    }

    // 업로드 파일을 검증.
    private void validateFile(MultipartFile file) {

        // 파일이 없거나 비어 있으면 오류를 발생시킴.
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("DRM 복호화할 업로드 파일이 없습니다.");
        }

        // 원본 파일명이 없으면 오류를 발생시킴.
        if (file.getOriginalFilename() == null || file.getOriginalFilename().isBlank()) {
            throw new IllegalArgumentException("업로드 파일명이 없습니다.");
        }
    }

    // source 임시 파일 경로를 생성.
    private Path createSourceFilePath(MultipartFile file) {

        // 안전한 파일명을 생성.
        String safeFileName = createSafeFileName(file);

        // source 디렉토리 아래에 임시 원본 파일 경로를 만듦.
        return drmProperties.getSourceDir()
                .resolve(UUID.randomUUID() + "_source_" + safeFileName);
    }

    // decrypt 결과 파일 경로를 생성.
    private Path createDecryptFilePath(MultipartFile file) {

        // 안전한 파일명을 생성.
        String safeFileName = createSafeFileName(file);

        // decrypt 디렉토리 아래에 복호화 파일 경로를 만듦.
        return drmProperties.getDecryptDir()
                .resolve(UUID.randomUUID() + "_decrypt_" + safeFileName);
    }

    // 파일명에서 위험 문자를 제거.
    private String createSafeFileName(MultipartFile file) {

        // 원본 파일명을 가져옴.
        String originalFilename = file.getOriginalFilename();

        // 경로 정보가 섞여 들어와도 파일명만 추출.
        String fileName = Path.of(originalFilename)
                .getFileName()
                .toString();

        // 윈도우/리눅스에서 문제가 될 수 있는 문자를 치환.
        return fileName.replaceAll("[\\\\/:*?\"<>|]", "_");
    }

    // 복호화 결과 파일을 검증.
    private void validateDecryptFile(Path decryptFilePath) throws IOException {

        // 복호화 파일이 생성되지 않았으면 오류를 발생시킴.
        if (!Files.exists(decryptFilePath)) {
            throw new IllegalStateException("DRM 복호화 파일이 생성되지 않았습니다.");
        }

        // 복호화 파일 크기가 0이면 오류를 발생시킴.
        if (Files.size(decryptFilePath) == 0) {
            throw new IllegalStateException("DRM 복호화 파일 크기가 0입니다.");
        }
    }

    // 파일 삭제 실패가 전체 업로드 실패로 번지지 않게 조용히 삭제.
    private void deleteQuietly(Path path) {

        try {
            // 파일이 존재하면 삭제.
            Files.deleteIfExists(path);

        } catch (IOException e) {

            // 삭제 실패는 경고 로그만 남김.
            log.warn("DRM 임시 파일 삭제에 실패했습니다. path={}", path, e);
        }
    }
}