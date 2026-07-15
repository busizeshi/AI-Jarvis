package com.notegather.biz.ai.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatStreamRequest(
        Long sessionId,
        @NotBlank(message = "问题不能为空")
        @Size(max = 4000, message = "问题不能超过 4000 个字符")
        String question
) {
}
