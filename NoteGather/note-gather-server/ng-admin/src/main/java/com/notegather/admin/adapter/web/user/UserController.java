package com.notegather.admin.adapter.web.user;

import com.notegather.admin.application.user.dto.LoginRequest;
import com.notegather.admin.application.user.dto.LogoutRequest;
import com.notegather.admin.application.user.dto.PasswordKeyResponse;
import com.notegather.admin.application.user.dto.RefreshTokenRequest;
import com.notegather.admin.application.user.dto.RegisterRequest;
import com.notegather.admin.application.user.dto.TokenResponse;
import com.notegather.admin.application.user.dto.UpdateProfileRequest;
import com.notegather.admin.application.user.dto.UserResponse;
import com.notegather.admin.application.user.service.UserService;
import com.notegather.admin.application.user.security.PasswordEnvelopeService;
import com.notegather.common.core.result.Result;
import com.notegather.common.security.context.UserContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserService userService;
    private final PasswordEnvelopeService passwordEnvelopeService;

    @GetMapping("/password-key")
    public Result<PasswordKeyResponse> passwordKey() {
        return Result.ok(passwordEnvelopeService.publicKey());
    }

    @PostMapping("/register")
    public Result<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        return Result.ok(userService.register(request));
    }

    @PostMapping("/login")
    public Result<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.ok(userService.login(request));
    }

    @PostMapping("/refresh-token")
    public Result<TokenResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return Result.ok(userService.refreshToken(request));
    }

    @PostMapping("/logout")
    public Result<Void> logout(@RequestBody(required = false) LogoutRequest request) {
        userService.logout(request);
        return Result.ok();
    }

    @GetMapping("/me")
    public Result<UserResponse> me() {
        return Result.ok(userService.getCurrentUser());
    }

    /**
     * 上传头像接口
     * POST /api/v1/user/me/avatar
     * Content-Type: multipart/form-data
     */
    @PostMapping("/me/avatar")
    public Result<UserResponse> uploadAvatar(@RequestParam("file") MultipartFile file) {
        Long userId = UserContext.getUserId();
        // 获取旧头像 URL，用于上传成功后删除
        UserResponse current = userService.getCurrentUser();
        String oldAvatarUrl = current != null ? current.getAvatarUrl() : null;
        return Result.ok(userService.uploadAvatar(userId, file, oldAvatarUrl));
    }

    /**
     * 更新用户基本资料接口
     * PUT /api/v1/user/me
     * Content-Type: application/json
     */
    @PutMapping("/me")
    public Result<UserResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return Result.ok(userService.updateProfile(UserContext.getUserId(), request));
    }
}
