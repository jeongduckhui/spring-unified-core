package com.example.demo.file.repository;

import com.example.demo.file.domain.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FileRepository extends JpaRepository<FileEntity, Long> {
    Optional<FileEntity> findByIdAndDeletedFalse(Long id);
    List<FileEntity> findByUserIdAndDeletedFalseOrderByCreatedAtDesc(Long userId);
    List<FileEntity> findByDeletedTrueAndDeletedAtBefore(LocalDateTime time);
}