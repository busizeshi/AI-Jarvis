package com.notegather.biz.ai.application.dto;

public record ChatCitationData(
        Long noteId,
        String noteTitle,
        String chunkText,
        Float score
) {
}
