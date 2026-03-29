package com.example.demo.file.batch;

import com.example.demo.file.domain.FileEntity;
import com.example.demo.file.repository.FileRepository;
import com.example.demo.file.storage.FileStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MailFileCleanupBatch {

    private final FileRepository fileRepository;
    private final FileStorage fileStorage;

    @Scheduled(cron = "0 0 3 * * *") // 매일 3시
    public void cleanup() {

        List<FileEntity> expiredFiles =
                fileRepository.findByMailAttachmentTrueAndExpireAtBeforeAndDeletedFalse(LocalDateTime.now());

        for (FileEntity file : expiredFiles) {

            try {
                // 1. 스토리지 삭제
                fileStorage.delete(file.getStoredName(), file.getFilePath());

                // 2. DB 완전 삭제
                fileRepository.delete(file);

                log.info("메일 첨부파일 삭제 완료: {}", file.getId());

            } catch (Exception e) {
                log.error("메일 첨부파일 삭제 실패: {}", file.getId(), e);
            }
        }
    }
}
