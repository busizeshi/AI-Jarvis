package com.notegather.biz.application.command;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 更新标签命令
 */
@Schema(description = "更新标签请求")
@Data
public class UpdateTagCommand {
    
    @Schema(description = "标签名称", example = "Java")
    @Size(max = 32, message = "标签名称不能超过32个字符")
    private String name;
    
    @Schema(description = "标签颜色（十六进制）", example = "#FF5733")
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "颜色格式不正确，应为 #RRGGBB 格式")
    private String color;
}
