package com.example.demo.converter.sample;

import example.Test;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/test")
public class TestController {

    @PostMapping("/dto")
    public TestDTO test(@RequestBody TestDTO testDTO) {

        log.info("###################  testDTO: ", testDTO);

        return testDTO;
    }
}
