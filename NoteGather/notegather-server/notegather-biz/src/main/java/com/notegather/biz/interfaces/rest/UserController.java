package com.notegather.biz.interfaces.rest;

import cn.dev33.satoken.stp.StpUtil;
import com.notegather.biz.application.command.UpdateProfileCommand;
import com.notegather.biz.application.service.UserService;
import com.notegather.biz.domain.identity.aggregate.User;
import com.notegather.common.core.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "用户管理", description = "用户信息查询、资料更新、头像上传等用户相关接口")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    
    @Operation(summary = "获取当前用户信息", description = "获取当前已登录用户的详细资料")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "401", description = "未登录", content = @Content)
    })
    @GetMapping("/me")
    public Result<UserProfileVO> getCurrentUser() {
        Long userId = StpUtil.getLoginIdAsLong();
        User user = userService.getUserById(userId);
        return Result.success(toVO(user));
    }
    
    @Operation(summary = "根据ID获取用户信息", description = "根据用户ID查询指定用户的详细资料")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "404", description = "用户不存在", content = @Content)
    })
    @GetMapping("/{userId}")
    public Result<UserProfileVO> getUserById(
            @Parameter(description = "用户ID", required = true, example = "1001")
            @PathVariable Long userId) {
        User user = userService.getUserById(userId);
        return Result.success(toVO(user));
    }
    
    @Operation(summary = "更新用户资料", description = "更新当前登录用户的个人资料（昵称、头像等）")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "更新成功"),
            @ApiResponse(responseCode = "401", description = "未登录", content = @Content)
    })
    @PutMapping("/me")
    public Result<Void> updateProfile(@RequestBody UpdateProfileCommand command) {
        Long userId = StpUtil.getLoginIdAsLong();
        userService.updateProfile(userId, command);
        return Result.success();
    }
    
    @Operation(summary = "上传用户头像", description = "上传当前登录用户的头像图片，支持 JPG/PNG 格式")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "上传成功"),
            @ApiResponse(responseCode = "400", description = "文件格式或大小不合规", content = @Content),
            @ApiResponse(responseCode = "401", description = "未登录", content = @Content)
    })
    @PostMapping("/me/avatar")
    public Result<AvatarUploadResponse> uploadAvatar(
            @Parameter(description = "头像图片文件", required = true)
            @RequestParam("file") MultipartFile file) {
        Long userId = StpUtil.getLoginIdAsLong();
        String avatarUrl = userService.uploadAvatar(userId, file);
        return Result.success(new AvatarUploadResponse(avatarUrl));
    }
    
    private UserProfileVO toVO(User user) {
        UserProfileVO vo = new UserProfileVO();
        vo.setUserId(user.getId().getValue());
        vo.setUsername(user.getUsername());
        vo.setEmail(user.getEmail().getValue());
        vo.setDisplayName(user.getDisplayName());
        vo.setAvatarUrl(user.getAvatarUrl());
        vo.setStatus(user.getStatus().getCode());
        vo.setCreatedAt(user.getCreatedAt());
        return vo;
    }
    
    @Schema(description = "用户资料视图")
    @Data
    public static class UserProfileVO {
        @Schema(description = "用户ID", example = "1001")
        private Long userId;
        @Schema(description = "用户名", example = "zhangsan")
        private String username;
        @Schema(description = "邮箱", example = "zhangsan@example.com")
        private String email;
        @Schema(description = "显示昵称", example = "张三")
        private String displayName;
        @Schema(description = "头像URL", example = "https://minio.example.com/avatars/1001.png")
        private String avatarUrl;
        @Schema(description = "账号状态: 0-禁用 1-正常", example = "1")
        private Integer status;
        @Schema(description = "注册时间")
        private java.time.LocalDateTime createdAt;
    }
    
    @Schema(description = "头像上传响应")
    @Data
    public static class AvatarUploadResponse {
        @Schema(description = "头像文件访问URL", example = "https://minio.example.com/avatars/1001.png")
        private final String avatarUrl;
    }
}
