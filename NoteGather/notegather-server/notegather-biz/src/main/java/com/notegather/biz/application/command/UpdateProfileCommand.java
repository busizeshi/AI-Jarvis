package com.notegather.biz.application.command;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "更新用户资料请求")
@Data
public class UpdateProfileCommand {
    
    @Schema(description = "显示昵称", example = "张三")
    private String displayName;
    
    @Schema(description = "头像URL", example = "https://minio.example.com/avatars/1001.png")
    private String avatarUrl;
}
