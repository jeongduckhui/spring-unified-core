package com.example.demo.sample.swagger.service;

import com.example.demo.sample.swagger.dto.UserResponse;
import com.example.demo.sample.swagger.dto.UserSearchRequest;
import com.example.demo.sample.swagger.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper mapper;

    public List<UserResponse> search(UserSearchRequest param) {
        return mapper.selectUsers(param);
    }
}