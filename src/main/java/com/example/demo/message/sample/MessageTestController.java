package com.example.demo.message.sample;

import com.example.demo.common.exception.BusinessException;
import com.example.demo.common.exception.ExceptionCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class MessageTestController {

    @GetMapping("/find")
    public String find(@RequestParam String id) {
        if ("none".equals(id)) {
            throw new BusinessException(ExceptionCode.USER_NOT_FOUND);
        }
        return "OK";
    }

    @PostMapping("/save")
    public String save(@RequestBody @Valid TestRequest req) {
        return "saved";
    }
}
