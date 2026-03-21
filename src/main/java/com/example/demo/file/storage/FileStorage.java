package com.example.demo.file.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorage {

    String save(MultipartFile file, String storedName, String path);

    void delete(String storedName, String path);

    String getDownloadUrl(String storedName, String path, String originalName);

    Resource load(String storedName, String path);
}