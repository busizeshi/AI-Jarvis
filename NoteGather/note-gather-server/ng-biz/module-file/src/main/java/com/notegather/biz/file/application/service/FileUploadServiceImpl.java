package com.notegather.biz.file.application.service;

import com.notegather.biz.file.application.config.FileUploadProperties;
import com.notegather.biz.file.application.dto.FileParseStatusResponse;
import com.notegather.biz.file.application.dto.FileUploadResponse;
import com.notegather.biz.file.application.port.FileStorage;
import com.notegather.biz.file.application.port.FileUploadedEventPublisher;
import com.notegather.biz.file.domain.model.FileRecord;
import com.notegather.biz.file.domain.model.ParseTask;
import com.notegather.biz.file.domain.enums.FileType;
import com.notegather.biz.file.domain.enums.ParseStatus;
import com.notegather.common.api.knowledge.KnowledgeFacade;
import com.notegather.common.core.exception.BusinessException;
import com.notegather.common.core.result.ResultCode;
import com.notegather.common.mq.dto.FileUploadedMessage;
import com.notegather.common.mq.dto.ParseDoneMessage;
import com.notegather.common.security.context.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileUploadServiceImpl implements FileUploadService {

    private final FileUploadProperties fileProperties;
    private final FileStorage fileStorage;
    private final FileUploadPersistenceService persistenceService;
    private final FileUploadedEventPublisher fileUploadedEventPublisher;
    private final KnowledgeFacade knowledgeFacade;

    @Override
    public FileUploadResponse upload(Long libraryId, Long parentId, MultipartFile file) {
        Long userId = UserContext.getUserId();
        String fileName = validateUpload(libraryId, file);
        FileType fileType = FileType.fromFileName(fileName);
        String objectKey = buildObjectKey(userId, fileType);
        String bucket = fileStorage.bucket();
        fileStorage.store(bucket, objectKey, file);
        try {
            FileUploadResponse response = persistenceService.createUpload(
                    userId,
                    libraryId,
                    parentId,
                    fileName,
                    fileType.name(),
                    file.getContentType(),
                    bucket,
                    objectKey,
                    file.getSize()
            );
            if (!publishOrMarkFailed(loadFile(userId, response.getFileId()), response.getParseTaskId())) {
                response.setParseStatus(ParseStatus.FAILED.name());
                response.setParseError("解析任务消息派发失败，请重试");
            }
            return response;
        } catch (RuntimeException exception) {
            fileStorage.deleteQuietly(bucket, objectKey);
            throw exception;
        }
    }

    @Override
    public FileParseStatusResponse getParseStatus(Long fileId) {
        return persistenceService.getParseStatus(UserContext.getUserId(), fileId);
    }

    @Override
    public FileParseStatusResponse retryParse(Long fileId) {
        Long userId = UserContext.getUserId();
        FileRecord file = persistenceService.requireFile(userId, fileId);
        ParseTask task = persistenceService.createRetryTask(userId, fileId);
        publishOrMarkFailed(file, task.getId());
        return persistenceService.getParseStatus(userId, fileId);
    }

    @Override
    public void handleParseResult(ParseDoneMessage message) {
        persistenceService.applyParseResult(message);
    }

    private String validateUpload(Long libraryId, MultipartFile file) {
        if (libraryId == null || file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "知识库和上传文件不能为空");
        }
        if (fileProperties.getMaxSizeBytes() <= 0 || file.getSize() > fileProperties.getMaxSizeBytes()) {
            throw new BusinessException(ResultCode.FILE_SIZE_EXCEED);
        }
        String fileName = StringUtils.cleanPath(StringUtils.getFilename(file.getOriginalFilename()));
        if (!StringUtils.hasText(fileName) || fileName.contains("..")) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "文件名非法");
        }
        return fileName;
    }

    private String buildObjectKey(Long userId, FileType fileType) {
        return "uploads/" + userId + "/" + UUID.randomUUID() + "." + fileType.name().toLowerCase();
    }

    private FileRecord loadFile(Long userId, Long fileId) {
        return persistenceService.requireFile(userId, fileId);
    }

    private boolean publishOrMarkFailed(FileRecord file, Long parseTaskId) {
        try {
            String noteTitle = knowledgeFacade.getNoteTitle(file.getUserId(), file.getNoteId());
            fileUploadedEventPublisher.publish(FileUploadedMessage.builder()
                    .fileId(String.valueOf(file.getId()))
                    .userId(String.valueOf(file.getUserId()))
                    .noteId(String.valueOf(file.getNoteId()))
                    .parseTaskId(String.valueOf(parseTaskId))
                    .noteTitle(noteTitle)
                    .objectKey(file.getObjectKey())
                    .bucket(file.getBucket())
                    .fileName(file.getFileName())
                    .fileType(file.getFileType())
                    .fileSize(file.getSize())
                    .build());
            return true;
        } catch (RuntimeException exception) {
            log.error("解析任务消息派发失败 fileId={} taskId={}", file.getId(), parseTaskId, exception);
            persistenceService.markDispatchFailed(file.getId(), parseTaskId, "解析任务消息派发失败，请重试");
            return false;
        }
    }
}
