package com.notegather.biz.file.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileParseStatusResponse {

    private Long fileId;
    private Long noteId;
    private String parseStatus;
    private Integer chunkCount;
    private String parseError;
    private Long parseTaskId;
    private Integer attemptCount;
    private String taskStatus;
    private String taskError;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
}
