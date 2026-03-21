package com.example.demo.authgroup.service;

import java.util.Set;

public interface FunctionAuthService {

    Set<Long> getAllowedAuthGroupIds(String functionId);
}