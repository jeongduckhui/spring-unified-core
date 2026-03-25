package com.example.demo.mail.repository;

import com.example.demo.mail.domain.MailLog;
import com.example.demo.mail.domain.MailSendStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MailLogRepository extends JpaRepository<MailLog, Long> {

    @Query("""
        select m
        from MailLog m
        where m.status = :status
          and m.retryCount < m.maxRetryCount
        order by m.createdAt asc
    """)
    List<MailLog> findRetryTargets(MailSendStatus status);

    @Query("""
        select m
        from MailLog m
        where (:recipient is null or m.recipient like %:recipient%)
          and (:subject is null or m.subject like %:subject%)
          and (:status is null or m.status = :status)
        order by m.createdAt desc
    """)
    List<MailLog> search(
            @Param("recipient") String recipient,
            @Param("subject") String subject,
            @Param("status") MailSendStatus status
    );
}