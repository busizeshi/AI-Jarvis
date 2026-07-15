package com.notegather.biz.file.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResponse {

    private Long fileId;
    private Long noteId;
    private Long parseTaskId;
    private String parseStatus;
    private String parseError;
}
