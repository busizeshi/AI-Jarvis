package com.notegather.biz.application.command;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "用户注册请求")
@Data
public class RegisterCommand {
    
    @Schema(description = "用户名", requiredMode = Schema.RequiredMode.REQUIRED, example = "zhangsan")
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 32, message = "用户名长度必须在3-32位之间")
    private String username;
    
    @Schema(description = "邮箱地址", requiredMode = Schema.RequiredMode.REQUIRED, example = "zhangsan@example.com")
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;
    
    @Schema(description = "密码", requiredMode = Schema.RequiredMode.REQUIRED, example = "P@ssw0rd")
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 32, message = "密码长度必须在6-32位之间")
    private String password;
    
    @Schema(description = "显示昵称", example = "张三")
    private String displayName;
}
