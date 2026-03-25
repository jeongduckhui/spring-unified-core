package com.example.demo.mail.service;

import com.example.demo.file.service.S3Service;
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
    private final S3Service s3Service;

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

    public void sendUserMailWithAttachment(
            String from,
            List<String> to,
            String subject,
            String content,
            List<MultipartFile> files
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

                    validateFile(file); // 🔥 보안

                    if (file.getSize() < 5 * 1024 * 1024) {
                        helper.addAttachment(file.getOriginalFilename(), file);
                    } else {
                        String url = s3Service.upload(file);

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

    private static final List<String> ALLOWED_EXT = List.of("jpg","png","pdf","txt","xlsx");

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