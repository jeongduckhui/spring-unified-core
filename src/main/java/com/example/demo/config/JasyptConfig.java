package com.example.demo.config;

import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.iv.RandomIvGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.util.Properties;

@Configuration
public class JasyptConfig {

    @Bean("jasyptStringEncryptor")
    public StringEncryptor stringEncryptor() {
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();

        String password = loadPassword();

        if (password == null || password.isBlank()) {
            throw new IllegalStateException("❌ JASYPT password not found (keys.properties or env)");
        }

        encryptor.setPassword(password);
        encryptor.setAlgorithm("PBEWITHHMACSHA512ANDAES_256");
        encryptor.setIvGenerator(new RandomIvGenerator());

        return encryptor;
    }

    /**
     * 우선순위
     * 1. keys.properties
     * 2. 환경변수 (JASYPT_PASSWORD)
     */
    private String loadPassword() {
        // 1. keys.properties
        try {
            ClassPathResource resource = new ClassPathResource("keys.properties");
            if (resource.exists()) {
                Properties props = new Properties();
                try (InputStream is = resource.getInputStream()) {
                    props.load(is);
                    String key = props.getProperty("jasypt.password");
                    if (key != null && !key.isBlank()) {
                        return key;
                    }
                }
            }
        } catch (Exception e) {
            // 로그만 남기고 fallback
            System.out.println("⚠️ keys.properties load 실패: " + e.getMessage());
        }

        // 2. 환경변수 fallback
        return System.getenv("JASYPT_PASSWORD");
    }
}