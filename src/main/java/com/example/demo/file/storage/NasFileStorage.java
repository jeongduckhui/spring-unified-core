package com.example.demo.file.storage;

import com.example.demo.common.exception.BusinessException;
import com.example.demo.common.exception.ExceptionCode;
import com.example.demo.file.config.NasStorageProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * NAS 기반 파일 저장소 구현체.
 *
 * <p>
 * 실제 NAS는 서버에 마운트되어 일반 디렉토리처럼 접근된다고 가정.
 * 예:
 * - 로컬 테스트: E:/myplayground/nas-upload/
 * - 운영 NAS: /mnt/nas/nextgen/upload/
 * </p>
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "file.storage.type",
        havingValue = "nas"
)
public class NasFileStorage implements FileStorage {

    private final NasStorageProperties nasStorageProperties;

    /**
     * 파일을 NAS 경로에 저장.
     *
     * @param file 업로드된 MultipartFile
     * @param storedName 서버에 저장할 파일명
     * @param path 날짜 기반 상대 경로
     * @return 실제 저장된 전체 경로
     */
    @Override
    public String save(MultipartFile file, String storedName, String path) {
        try {
            // 설정한 NAS 기본 경로를 Path로 가져옴.
            Path basePath = getBasePath();

            // NAS 기본 경로 아래에 날짜 기반 상대 경로를 붙여 저장 디렉토리 경로를 만듦.
            Path targetDir = basePath.resolve(path).normalize();

            // 저장 디렉토리 경로가 NAS 기본 경로 밖으로 벗어나지 않았는지 검증.
            validateInsideBasePath(basePath, targetDir);

            // 저장 디렉토리가 없으면 생성.
            Files.createDirectories(targetDir);

            // 저장 디렉토리 아래에 저장 파일명을 붙여 최종 저장 파일 경로를 만듦.
            Path target = targetDir.resolve(storedName).normalize();

            // 최종 저장 파일 경로가 NAS 기본 경로 밖으로 벗어나지 않았는지 검증.
            validateInsideBasePath(basePath, target);

            // 업로드된 파일 바이너리를 최종 저장 파일 경로에 저장.
            file.transferTo(target);

            // 저장된 파일의 실제 전체 경로를 문자열로 반환.
            return target.toString();

        } catch (BusinessException e) {
            // 이미 비즈니스 예외로 판단된 경우에는 그대로 상위로 던짐.
            throw e;

        } catch (Exception e) {
            // 그 외 예외는 파일 업로드 실패 예외로 감싸서 상위로 던짐.
            throw new BusinessException(ExceptionCode.FILE_UPLOAD_FAILED, e);
        }
    }

    /**
     * NAS에 저장된 파일을 삭제.
     *
     * @param storedName 저장 파일명
     * @param path 날짜 기반 상대 경로
     */
    @Override
    public void delete(String storedName, String path) {
        try {
            // properties 또는 yml에서 설정한 NAS 기본 경로를 Path로 가져옴.
            Path basePath = getBasePath();

            // NAS 기본 경로, 상대 경로, 저장 파일명을 조합해 삭제 대상 파일 경로를 만듦.
            Path filePath = basePath.resolve(path).resolve(storedName).normalize();

            // 삭제 대상 파일 경로가 NAS 기본 경로 밖으로 벗어나지 않았는지 검증.
            validateInsideBasePath(basePath, filePath);

            // 파일이 존재하면 삭제하고, 존재하지 않으면 아무 작업도 하지 않음.
            Files.deleteIfExists(filePath);

        } catch (BusinessException e) {
            // 이미 비즈니스 예외로 판단된 경우에는 그대로 상위로 던짐.
            throw e;

        } catch (Exception e) {
            // 그 외 예외는 파일 삭제 실패 예외로 감싸서 상위로 던짐.
            throw new BusinessException(ExceptionCode.FILE_DELETE_FAILED, e);
        }
    }

    /**
     * NAS는 S3처럼 외부 다운로드 URL을 만들지 않음.
     *
     * <p>
     * 다운로드는 FileController의 /files/download/{id} API에서
     * Resource 스트리밍 방식으로 처리.
     * </p>
     *
     * @param storedName 저장 파일명
     * @param path 날짜 기반 상대 경로
     * @param originalName 원본 파일명
     * @return NAS에서는 외부 다운로드 URL을 사용하지 않으므로 null을 반환.
     */
    @Override
    public String getDownloadUrl(String storedName, String path, String originalName) {
        // NAS는 외부 URL을 발급하지 않고 Controller에서 파일을 직접 스트리밍함.
        return null;
    }

    /**
     * NAS에 저장된 파일을 Resource로 로드.
     *
     * @param storedName 저장 파일명
     * @param path 날짜 기반 상대 경로
     * @return 다운로드 응답에 사용할 Resource
     */
    @Override
    public Resource load(String storedName, String path) {
        try {
            // properties 또는 yml에서 설정한 NAS 기본 경로를 Path로 가져옴.
            Path basePath = getBasePath();

            // NAS 기본 경로, 상대 경로, 저장 파일명을 조합해 다운로드 대상 파일 경로를 만듦.
            Path filePath = basePath.resolve(path).resolve(storedName).normalize();

            // 다운로드 대상 파일 경로가 NAS 기본 경로 밖으로 벗어나지 않았는지 검증.
            validateInsideBasePath(basePath, filePath);

            // 파일 시스템 경로를 Spring Resource 객체로 변환.
            Resource resource = new UrlResource(filePath.toUri());

            // 파일이 존재하지 않거나 읽을 수 없는 경우를 확인.
            if (!resource.exists() || !resource.isReadable()) {
                // 다운로드 대상 파일이 없으면 파일 없음 예외를 던짐.
                throw new BusinessException(ExceptionCode.FILE_NOT_FOUND);
            }

            // 정상적으로 읽을 수 있는 Resource를 반환.
            return resource;

        } catch (BusinessException e) {
            // 이미 비즈니스 예외로 판단된 경우에는 그대로 상위로 던짐.
            throw e;

        } catch (MalformedURLException e) {
            // 파일 경로를 Resource로 변환하다 실패하면 다운로드 실패 예외로 감싸서 던짐.
            throw new BusinessException(ExceptionCode.FILE_DOWNLOAD_FAILED, e);
        }
    }

    /**
     * NAS 기본 경로를 Path로 변환.
     *
     * @return 정규화된 NAS 기본 경로
     */
    private Path getBasePath() {
        // properties 또는 yml에서 읽어온 NAS 기본 경로 문자열을 가져옴.
        String baseDir = nasStorageProperties.getBaseDir();

        // NAS 기본 경로가 설정되지 않았는지 확인.
        if (baseDir == null || baseDir.isBlank()) {
            // NAS 기본 경로가 없으면 파일 저장/조회가 불가능하므로 업로드 실패 예외를 던짐.
            throw new BusinessException(ExceptionCode.FILE_UPLOAD_FAILED);
        }

        // NAS 기본 경로 문자열을 절대 경로 Path로 변환하고 정규화해서 반환.
        return Paths.get(baseDir).toAbsolutePath().normalize();
    }

    /**
     * 계산된 경로가 NAS 기본 경로 밖으로 벗어나지 않는지 확인.
     *
     * <p>
     * path 값에 ../ 같은 값이 들어와도 baseDir 밖으로 빠져나가지 못하게 막음.
     * </p>
     *
     * @param basePath NAS 기본 경로
     * @param targetPath 검증 대상 경로
     */
    private void validateInsideBasePath(Path basePath, Path targetPath) {
        // 검증 대상 경로가 NAS 기본 경로로 시작하지 않는지 확인.
        if (!targetPath.startsWith(basePath)) {
            // NAS 기본 경로 밖으로 벗어난 경로이면 업로드 실패 예외를 던짐.
            throw new BusinessException(ExceptionCode.FILE_UPLOAD_FAILED);
        }
    }
}