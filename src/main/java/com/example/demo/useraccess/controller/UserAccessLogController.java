package com.example.demo.useraccess.controller;

import com.example.demo.useraccess.service.UserAccessLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserAccessLogController {

    private final UserAccessLogService service;

    @PostMapping("/user-access-log")
    public void save(Authentication authentication,
                     HttpServletRequest request) {

        if (authentication == null) {
            return;
        }

        Long userId = Long.parseLong(authentication.getName());

        String funcId = request.getHeader("X-Func-Id");
        String systemTypeCode = request.getHeader("X-System-Type-Code");

        service.save(userId, funcId, systemTypeCode);
    }

    @PostMapping("/user-access-log/end")
    public void end(Authentication authentication,
                    HttpServletRequest request) {

        Long userId = null;

        if (authentication != null) {
            userId = Long.parseLong(authentication.getName());
        }

        // 🔥 1️⃣ 헤더에서 funcId (탭 닫기)
        String funcId = request.getHeader("X-Func-Id");

        // 🔥 2️⃣ sendBeacon fallback (body)
        if (funcId == null) {
            try {
                String body = request.getReader().lines()
                        .reduce("", (acc, line) -> acc + line);

                if (body != null && body.contains("funcId")) {
                    funcId = body.replaceAll(".*\"funcId\":\"(.*?)\".*", "$1");
                }
            } catch (Exception ignored) {}
        }

        // 🔥 핵심 분기
        if (funcId != null && userId != null) {
            service.endByFuncId(userId, funcId); // ✅ 정확 종료
        } else {
            service.endLastActive(userId); // 🔥 fallback
        }
    }
}