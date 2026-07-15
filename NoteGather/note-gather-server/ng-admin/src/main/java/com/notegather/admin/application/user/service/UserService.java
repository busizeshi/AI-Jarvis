package com.notegather.admin.application.user.service;

import com.notegather.admin.application.user.dto.LoginRequest;
import com.notegather.admin.application.user.dto.LogoutRequest;
import com.notegather.admin.application.user.dto.RefreshTokenRequest;
import com.notegather.admin.application.user.dto.RegisterRequest;
import com.notegather.admin.application.user.dto.TokenResponse;
import com.notegather.admin.application.user.dto.UserResponse;

import java.util.List;

public interface UserService {

    UserResponse register(RegisterRequest request);

    TokenResponse login(LoginRequest request);

    TokenResponse refreshToken(RefreshTokenRequest request);

    void logout(LogoutRequest request);

    UserResponse getCurrentUser();

    UserResponse getById(Long userId);

    UserResponse getByUsername(String username);

    List<UserResponse> listByIds(List<Long> userIds);

    boolean updateStatus(Long userId, Integer status);
}
