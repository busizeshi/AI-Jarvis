package com.notegather.admin.application.user.service;

import com.notegather.admin.application.user.dto.LoginRequest;
import com.notegather.admin.application.user.dto.LogoutRequest;
import com.notegather.admin.application.user.dto.RefreshTokenRequest;
import com.notegather.admin.application.user.dto.RegisterRequest;
import com.notegather.admin.application.user.dto.TokenResponse;
import com.notegather.admin.application.user.dto.UpdateProfileRequest;
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

    /**
     * 上传并更新当前用户头像：
     * 1. 校验图片类型与大小
     * 2. 上传新头像至 MinIO
     * 3. 更新 DB 中的 avatar_url
     * 4. 删除 MinIO 中的旧头像（异步静默）
     *
     * @param userId    当前用户 ID
     * @param file      新头像图片
     * @param oldAvatarUrl 旧头像 URL（可为 null）
     * @return 更新后的用户信息
     */
    UserResponse uploadAvatar(Long userId, org.springframework.web.multipart.MultipartFile file, String oldAvatarUrl);

    /**
     * 更新当前用户基本资料（昵称、简介）
     *
     * @param userId  当前用户 ID
     * @param request 更新请求
     * @return 更新后的用户信息
     */
    UserResponse updateProfile(Long userId, UpdateProfileRequest request);
}
