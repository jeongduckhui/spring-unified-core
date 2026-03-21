package com.example.demo.file.storage;

import com.example.demo.common.exception.BusinessException;
import com.example.demo.common.exception.ExceptionCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "file.storage.type",
        havingValue = "local",
        matchIfMissing = true
)
public class LocalFileStorage implements FileStorage {

    @Value("${file.upload-dir}")
    private String baseDir;

    @Override
    public String save(MultipartFile file, String storedName, String path) {
        try {
            Path basePath = Paths.get(baseDir).toAbsolutePath().normalize();
            Path targetDir = basePath.resolve(path).normalize();

            Files.createDirectories(targetDir);

            Path target = targetDir.resolve(storedName);
            file.transferTo(target);

            return target.toString();

        } catch (Exception e) {
            throw new BusinessException(ExceptionCode.FILE_UPLOAD_FAILED, e);
        }
    }

    @Override
    public void delete(String storedName, String path) {
        try {
            Path basePath = Paths.get(baseDir).toAbsolutePath().normalize();
            Path filePath = basePath.resolve(path).resolve(storedName);

            Files.deleteIfExists(filePath);

        } catch (Exception e) {
            throw new BusinessException(ExceptionCode.FILE_DELETE_FAILED, e);
        }
    }

    @Override
    public String getDownloadUrl(String storedName, String path, String originalName) {
        return "/files/download/local/" + path + storedName;
    }

    @Override
    public Resource load(String storedName, String path) {
        try {
            Path basePath = Paths.get(baseDir).toAbsolutePath().normalize();
            Path filePath = basePath.resolve(path).resolve(storedName).normalize();

            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                throw new BusinessException(ExceptionCode.FILE_NOT_FOUND);
            }

            return resource;

        } catch (MalformedURLException e) {
            throw new BusinessException(ExceptionCode.FILE_DOWNLOAD_FAILED, e);
        }
    }
}