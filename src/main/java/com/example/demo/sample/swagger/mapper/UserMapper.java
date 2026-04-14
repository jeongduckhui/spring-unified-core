package com.example.demo.sample.swagger.mapper;

import com.example.demo.sample.swagger.dto.UserResponse;
import com.example.demo.sample.swagger.dto.UserSearchRequest;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserMapper {

    List<UserResponse> selectUsers(UserSearchRequest param);
}