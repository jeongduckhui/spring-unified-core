package com.example.demo.excel.util;

import lombok.extern.slf4j.Slf4j;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
public class DeleteOnCloseInputStream extends FilterInputStream {

    // 삭제 대상 파일 경로
    private final Path filePath;

    public DeleteOnCloseInputStream(
            InputStream in,
            Path filePath
    ) {

        super(in);

        this.filePath = filePath;
    }

    @Override
    public void close() throws IOException {

        try {

            // InputStream 종료
            super.close();

        } finally {

            try {

                // 임시 파일 삭제
                Files.deleteIfExists(filePath);

            } catch (IOException e) {

                log.warn(
                        "임시 파일 삭제 실패. path={}",
                        filePath,
                        e
                );
            }
        }
    }
}