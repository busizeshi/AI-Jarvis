package com.notegather.biz.interfaces.rest;

import cn.dev33.satoken.stp.StpUtil;
import com.notegather.biz.application.command.LoginCommand;
import com.notegather.biz.application.command.RegisterCommand;
import com.notegather.biz.application.service.AuthService;
import com.notegather.biz.application.service.UserService;
import com.notegather.biz.domain.identity.aggregate.User;
import com.notegather.common.core.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "认证管理", description = "用户注册、登录、登出、Token 刷新等认证相关接口")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    private final UserService userService;
    
    @Operation(summary = "用户注册", description = "注册新用户账号，成功后返回用户ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "注册成功"),
            @ApiResponse(responseCode = "400", description = "参数校验失败", content = @Content),
            @ApiResponse(responseCode = "409", description = "用户名或邮箱已存在", content = @Content)
    })
    @PostMapping("/register")
    public Result<RegisterResponse> register(@Validated @RequestBody RegisterCommand command) {
        Long userId = authService.register(command);
        return Result.success(new RegisterResponse(userId));
    }
    
    @Operation(summary = "用户登录", description = "通过用户名和密码登录，成功后返回 Token 和用户ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "登录成功"),
            @ApiResponse(responseCode = "401", description = "用户名或密码错误", content = @Content)
    })
    @PostMapping("/login")
    public Result<LoginResponse> login(@Validated @RequestBody LoginCommand command) {
        String token = authService.login(command);
        Long userId = authService.getCurrentUserId();
        return Result.success(new LoginResponse(token, userId));
    }
    
    @Operation(summary = "退出登录", description = "注销当前用户的登录状态")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "登出成功")
    })
    @PostMapping("/logout")
    public Result<Void> logout() {
        authService.logout();
        return Result.success();
    }
    
    @Operation(summary = "刷新 Token", description = "刷新当前用户的认证 Token，延长有效期")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "刷新成功"),
            @ApiResponse(responseCode = "401", description = "Token 无效或已过期", content = @Content)
    })
    @PostMapping("/refresh")
    public Result<Void> refresh() {
        authService.refreshToken();
        return Result.success();
    }
    
    @Operation(summary = "获取当前登录用户信息", description = "获取当前已登录用户的基本信息")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "401", description = "未登录", content = @Content)
    })
    @GetMapping("/me")
    public Result<UserInfoResponse> getCurrentUser() {
        Long userId = authService.getCurrentUserId();
        User user = userService.getUserById(userId);
        return Result.success(new UserInfoResponse(
            user.getId().getValue(),
            user.getUsername(),
            user.getEmail().getValue()
        ));
    }
    
    @Schema(description = "注册响应")
    @Data
    @AllArgsConstructor
    public static class RegisterResponse {
        @Schema(description = "新注册的用户ID", example = "1001")
        private final Long userId;
    }
    
    @Schema(description = "登录响应")
    @Data
    @AllArgsConstructor
    public static class LoginResponse {
        @Schema(description = "认证 Token", example = "eyJ0eXAiOiJKV1QiLCJhbGci...")
        private final String token;
        @Schema(description = "用户ID", example = "1001")
        private final Long userId;
    }
    
    @Schema(description = "用户信息响应")
    @Data
    @AllArgsConstructor
    public static class UserInfoResponse {
        @Schema(description = "用户ID", example = "1001")
        private final Long userId;
        @Schema(description = "用户名", example = "zhangsan")
        private final String username;
        @Schema(description = "邮箱", example = "zhangsan@example.com")
        private final String email;
    }
}
