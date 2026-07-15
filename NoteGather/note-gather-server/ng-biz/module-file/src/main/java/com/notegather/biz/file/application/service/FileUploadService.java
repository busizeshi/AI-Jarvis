package com.notegather.biz.file.application.service;

import com.notegather.biz.file.application.dto.FileParseStatusResponse;
import com.notegather.biz.file.application.dto.FileUploadResponse;
import com.notegather.common.mq.dto.ParseDoneMessage;
import org.springframework.web.multipart.MultipartFile;

public interface FileUploadService {

    FileUploadResponse upload(Long libraryId, Long parentId, MultipartFile file);

    FileParseStatusResponse getParseStatus(Long fileId);

    FileParseStatusResponse retryParse(Long fileId);

    void handleParseResult(ParseDoneMessage message);
}
