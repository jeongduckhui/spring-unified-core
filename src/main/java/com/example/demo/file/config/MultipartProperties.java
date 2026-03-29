package com.example.demo.file.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;

@Component
@ConfigurationProperties(prefix = "spring.servlet.multipart")
@Getter
@Setter
public class MultipartProperties {

    private DataSize maxFileSize;
}
