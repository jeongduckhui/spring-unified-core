package com.example.demo.appuser.service;

import com.example.demo.appuser.domain.AppUser;
import com.example.demo.appuser.dto.AppUserDto;
import com.example.demo.appuser.mapper.AppUserMapper;
import com.example.demo.appuser.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AppUserService {

    private final AppUserRepository appUserRepository;
    private final AppUserMapper userMapper;

    @Transactional(readOnly = true)
    public AppUser findByJpa(Long id) {
        return appUserRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found. id=" + id));
    }

    @Transactional(readOnly = true)
    public AppUserDto findByMyBatis(Long id) {
        AppUserDto user = userMapper.findById(id);
        if (user == null) {
            throw new IllegalArgumentException("User not found. id=" + id);
        }
        return user;
    }
}