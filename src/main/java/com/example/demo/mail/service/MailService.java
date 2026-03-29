package com.example.demo.mail.service;

import com.example.demo.file.domain.FileEntity;
import com.example.demo.file.service.FileService;
import com.example.demo.mail.config.MailProperties;
import com.example.demo.mail.domain.MailType;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;
    private final MailProperties mailProperties;
    private final TemplateEngine templateEngine;

    // 🔥 변경: S3Service 제거
    private final FileService fileService;

    // =========================
    // 시스템 메일
    // =========================
    public void sendSystemMail(
            List<String> to,
            String subject,
            String content,
            MailType type
    ) {

        try {

            Context context = new Context();

            switch (type) {

                case AUTH -> {
                    context.setVariable("title", subject);
                    context.setVariable("content", "아래 버튼을 클릭하여 인증을 완료해주세요.");
                    context.setVariable("buttonText", "인증하기");
                    context.setVariable("buttonUrl", content);
                }

                case PASSWORD_RESET -> {
                    context.setVariable("title", subject);
                    context.setVariable("content", "비밀번호를 재설정해주세요.");
                    context.setVariable("buttonText", "재설정");
                    context.setVariable("buttonUrl", content);
                }

                case NOTIFICATION -> {
                    context.setVariable("title", subject);
                    context.setVariable("content", content);
                    context.setVariable("buttonText", null);
                    context.setVariable("buttonUrl", null);
                }
            }

            String html = templateEngine.process("mail/layout", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(new InternetAddress(
                    mailProperties.getFrom(),
                    "GSCM 시스템",
                    "UTF-8"
            ));

            helper.setTo(to.toArray(new String[0]));
            helper.setSubject(subject);
            helper.setText(html, true);

            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("시스템 메일 발송 실패", e);
        }
    }

    // =========================
    // 개인 메일
    // =========================
    public void sendUserMail(
            String from,
            List<String> to,
            String subject,
            String content
    ) {

        try {

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(from);
            helper.setTo(to.toArray(new String[0]));
            helper.setSubject(subject);
            helper.setText(content, true);

            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("사용자 메일 발송 실패", e);
        }
    }

    // =========================
    // 개인 메일 + 첨부파일
    // =========================
    public void sendUserMailWithAttachment(
            Long userId,
            String from,
            List<String> to,
            String subject,
            String content,
            List<MultipartFile> files,
            String ip,
            String userAgent,
            String deviceId

    ) {

        try {

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(from);
            helper.setTo(to.toArray(new String[0]));
            helper.setSubject(subject);

            StringBuilder finalContent = new StringBuilder(content);

            if (files != null) {
                for (MultipartFile file : files) {

                    validateFile(file);

                    // 5MB 이하 → 메일 첨부
//                    if (file.getSize() < 5 * 1024 * 1024) {
                    if (file.getSize() > 5 * 1024 * 1024) {
                        helper.addAttachment(file.getOriginalFilename(), file);
                    }
                    // 5MB 초과 → 파일 업로드 + 링크 삽입
                    else {

                        FileEntity uploadedFile = fileService.uploadForMail(
                                file,
                                userId,
                                ip,
                                userAgent,
                                deviceId
                        );

                        String url = fileService.getDownloadUrlForMail(uploadedFile);

                        finalContent.append("<br><br>")
                                .append("<a href='").append(url)
                                .append("' style='padding:10px;background:#4CAF50;color:white;'>")
                                .append("파일 다운로드: ")
                                .append(file.getOriginalFilename())
                                .append("</a>");
                    }
                }
            }

            helper.setText(finalContent.toString(), true);
            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("메일 발송 실패", e);
        }
    }

    // =========================
    // 파일 검증
    // =========================
    private static final List<String> ALLOWED_EXT =
            List.of("jpg", "png", "pdf", "txt", "xlsx", "mp4");

    private void validateFile(MultipartFile file) {

        String ext = file.getOriginalFilename()
                .substring(file.getOriginalFilename().lastIndexOf(".") + 1)
                .toLowerCase();

        if (!ALLOWED_EXT.contains(ext)) {
            throw new RuntimeException("허용되지 않은 확장자");
        }

        if (file.getSize() > 20 * 1024 * 1024) {
            throw new RuntimeException("파일 크기 초과");
        }
    }
}