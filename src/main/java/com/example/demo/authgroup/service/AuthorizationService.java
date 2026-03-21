package com.example.demo.authgroup.service;

public interface AuthorizationService {

    boolean isAuthorized(Long userId, String requestUri);
}