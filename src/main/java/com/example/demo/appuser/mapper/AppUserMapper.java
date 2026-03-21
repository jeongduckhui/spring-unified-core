package com.example.demo.appuser.mapper;

import com.example.demo.appuser.dto.AppUserDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AppUserMapper {

    AppUserDto findById(@Param("id") Long id);
}