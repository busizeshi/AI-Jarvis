package com.notegather.biz.ai.application.dto;

public record ChatCitationData(
        Long noteId,
        String noteTitle,
        String chunkText,
        Float score,
        String chunkId,
        Integer chunkIndex,
        Integer startOffset,
        Integer endOffset,
        java.util.List<String> retrievalSources
) {
}
