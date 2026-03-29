package com.example.demo.file.batch;

import com.example.demo.file.domain.FileEntity;
import com.example.demo.file.repository.FileRepository;
import com.example.demo.file.storage.FileStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileCleanupBatch {

    private final FileRepository fileRepository;
    private final FileStorage fileStorage;

    @Value("${file.cleanup.days}")
    private int cleanupDays;

//    @Scheduled(cron = "0 * * * * *") // test 용
    @Scheduled(cron = "0 0 3 * * *")
    public void cleanup() {

        LocalDateTime cutoffDateTime = LocalDateTime.now().minusDays(cleanupDays);

        List<FileEntity> targets =
                fileRepository.findByDeletedTrueAndDeletedAtBefore(cutoffDateTime);

        log.info("파일 정리 시작 - 대상 수: {}", targets.size());

        for (FileEntity file : targets) {
            try {
                log.info("삭제 대상 fileId={}, path={}, storedName={}",
                        file.getId(),
                        file.getFilePath(),
                        file.getStoredName()
                );

                // 1. 스토리지 삭제
                fileStorage.delete(
                        file.getStoredName(),
                        file.getFilePath()
                );

                // 2. DB 완전 삭제 (핵심)
                fileRepository.delete(file);

                log.info("파일 삭제 성공: fileId={}", file.getId());

            } catch (Exception e) {
                log.error("파일 삭제 실패: {}", file.getId(), e);
            }
        }

        log.info("파일 정리 종료");
    }
}