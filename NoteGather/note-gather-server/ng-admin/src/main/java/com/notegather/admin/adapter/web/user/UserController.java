package com.notegather.admin.adapter.web.user;

import com.notegather.admin.application.user.dto.LoginRequest;
import com.notegather.admin.application.user.dto.LogoutRequest;
import com.notegather.admin.application.user.dto.RefreshTokenRequest;
import com.notegather.admin.application.user.dto.RegisterRequest;
import com.notegather.admin.application.user.dto.TokenResponse;
import com.notegather.admin.application.user.dto.UserResponse;
import com.notegather.admin.application.user.service.UserService;
import com.notegather.common.core.result.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserService userService;

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
}
