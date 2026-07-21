package com.notegather.biz.file.adapter.web;

import com.notegather.biz.file.application.dto.FileParseStatusResponse;
import com.notegather.biz.file.application.dto.FileUploadResponse;
import com.notegather.biz.file.application.service.FileUploadService;
import com.notegather.common.core.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/files")
public class FileController {

    private final FileUploadService fileUploadService;

    @PostMapping("/upload")
    public Result<FileUploadResponse> upload(
            @RequestParam("libraryId") Long libraryId,
            @RequestParam(value = "parentId", required = false) Long parentId,
            @RequestParam("file") MultipartFile file
    ) {
        return Result.ok(fileUploadService.upload(libraryId, parentId, file));
    }

    @GetMapping("/{fileId}/parse-status")
    public Result<FileParseStatusResponse> getParseStatus(@PathVariable("fileId") Long fileId) {
        return Result.ok(fileUploadService.getParseStatus(fileId));
    }

    @PostMapping("/{fileId}/parse/retry")
    public Result<FileParseStatusResponse> retryParse(@PathVariable("fileId") Long fileId) {
        return Result.ok(fileUploadService.retryParse(fileId));
    }
}
