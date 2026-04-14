package com.example.demo.sample.swagger.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

// .requestMatchers("/internal/meta").permitAll()
@Slf4j
@RestController
@Profile({"local", "dev"}) // 핵심: 운영에서 차단
public class MetaController {

    private static final String META_COOKIE_NAME = "META";

    @GetMapping("/internal/meta")
    public String getMeta(HttpServletRequest request) {

        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            log.warn("META cookie not found (no cookies)");
            return "";
        }

        for (Cookie cookie : cookies) {
            if (META_COOKIE_NAME.equals(cookie.getName())) {
                log.info("META cookie found");
                return cookie.getValue();
            }
        }

        log.warn("META cookie not found");
        return "";
    }
}