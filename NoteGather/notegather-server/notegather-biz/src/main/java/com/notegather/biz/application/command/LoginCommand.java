package com.notegather.biz.application.command;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Schema(description = "用户登录请求")
@Data
public class LoginCommand {
    
    @Schema(description = "用户名", requiredMode = Schema.RequiredMode.REQUIRED, example = "zhangsan")
    @NotBlank(message = "用户名不能为空")
    private String username;
    
    @Schema(description = "密码", requiredMode = Schema.RequiredMode.REQUIRED, example = "P@ssw0rd")
    @NotBlank(message = "密码不能为空")
    private String password;
}
