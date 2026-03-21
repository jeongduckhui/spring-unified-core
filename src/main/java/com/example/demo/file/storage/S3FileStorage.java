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
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "file.storage.type",
        havingValue = "s3"
)
public class S3FileStorage implements FileStorage {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Override
    public String save(MultipartFile file, String storedName, String path) {

        String key = path + storedName;

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(
                            file.getContentType() != null
                                    ? file.getContentType()
                                    : "application/octet-stream"
                    )
                    .build();

            s3Client.putObject(
                    request,
                    RequestBody.fromInputStream(
                            file.getInputStream(),
                            file.getSize()
                    )
            );

            return key;

        } catch (Exception e) {
            throw new BusinessException(ExceptionCode.FILE_UPLOAD_FAILED, e);
        }
    }
    /*
    public String save(MultipartFile file, String storedName, String path) {

        String key = path + storedName;

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));

            return key;

        } catch (Exception e) {
            throw new BusinessException(ExceptionCode.FILE_UPLOAD_FAILED, e);
        }
    }
    */

    @Override
    public void delete(String storedName, String path) {

        String key = path + storedName;

        s3Client.deleteObject(builder -> builder
                .bucket(bucket)
                .key(key)
        );
    }

    @Override
    public String getDownloadUrl(String storedName, String path, String originalName) {

        String key = path + storedName;

        String encoded = URLEncoder.encode(originalName, StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .responseContentDisposition(
                        "attachment; filename*=UTF-8''" + encoded
                )
                .build();

        GetObjectPresignRequest presignRequest =
                GetObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofMinutes(5))
                        .getObjectRequest(getObjectRequest)
                        .build();

        PresignedGetObjectRequest presignedRequest =
                s3Presigner.presignGetObject(presignRequest);

        return presignedRequest.url().toString();
    }

    @Override
    public Resource load(String storedName, String path) {

        String key = path + storedName;

        try {
            String url = s3Client.utilities()
                    .getUrl(builder -> builder
                            .bucket(bucket)
                            .key(key))
                    .toExternalForm();

            return new UrlResource(url);

        } catch (Exception e) {
            throw new BusinessException(ExceptionCode.FILE_DOWNLOAD_FAILED, e);
        }
    }
}