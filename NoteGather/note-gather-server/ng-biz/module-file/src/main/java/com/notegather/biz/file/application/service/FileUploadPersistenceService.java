package com.notegather.biz.file.application.service;

import com.notegather.biz.file.application.dto.FileParseStatusResponse;
import com.notegather.biz.file.application.dto.FileUploadResponse;
import com.notegather.biz.file.domain.model.FileRecord;
import com.notegather.biz.file.domain.model.ParseTask;
import com.notegather.biz.file.domain.enums.ParseStatus;
import com.notegather.biz.file.domain.repository.FileRepository;
import com.notegather.biz.file.domain.repository.ParseTaskRepository;
import com.notegather.biz.file.application.port.FileStorage;
import com.notegather.common.api.knowledge.KnowledgeFacade;
import com.notegather.common.api.knowledge.dto.FileNoteCreateRequest;
import com.notegather.common.api.knowledge.dto.FileNoteDTO;
import com.notegather.common.core.exception.BusinessException;
import com.notegather.common.core.result.ResultCode;
import com.notegather.common.mq.dto.ParseDoneMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class FileUploadPersistenceService {

    private final FileRepository fileRepository;
    private final ParseTaskRepository parseTaskRepository;
    private final KnowledgeFacade knowledgeFacade;
    private final FileStorage fileStorage;

    @Transactional(rollbackFor = Exception.class)
    public FileUploadResponse createUpload(
            Long userId,
            Long libraryId,
            Long parentId,
            String fileName,
            String fileType,
            String contentType,
            String bucket,
            String objectKey,
            long size
    ) {
        FileRecord file = new FileRecord();
        file.setUserId(userId);
        file.setLibraryId(libraryId);
        file.setFileName(fileName);
        file.setFileType(fileType);
        file.setContentType(contentType);
        file.setBucket(bucket);
        file.setObjectKey(objectKey);
        file.setSize(size);
        file.setParseStatus(ParseStatus.PENDING.name());
        file.setChunkCount(0);
        fileRepository.save(file);

        FileNoteDTO note = knowledgeFacade.createFileNote(FileNoteCreateRequest.builder()
                .userId(userId)
                .libraryId(libraryId)
                .parentId(parentId)
                .fileName(fileName)
                .build());
        fileRepository.bindNote(file.getId(), userId, note.getNoteId());

        ParseTask task = newParseTask(file.getId(), userId);
        parseTaskRepository.save(task);
        return FileUploadResponse.builder()
                .fileId(file.getId())
                .noteId(note.getNoteId())
                .parseTaskId(task.getId())
                .parseStatus(ParseStatus.PENDING.name())
                .build();
    }

    @Transactional(rollbackFor = Exception.class)
    public ParseTask createRetryTask(Long userId, Long fileId) {
        FileRecord file = requireFile(userId, fileId);
        ParseTask activeTask = parseTaskRepository.findActiveByFileId(file.getId());
        if (activeTask != null) {
            throw new BusinessException(ResultCode.CONFLICT, "当前文件已有解析任务处理中");
        }
        ParseTask task = newParseTask(file.getId(), userId);
        parseTaskRepository.save(task);
        updateFileAndNote(file, ParseStatus.PENDING, 0, null);
        return task;
    }

    @Transactional(rollbackFor = Exception.class)
    public void markDispatchFailed(Long fileId, Long parseTaskId, String errorMessage) {
        FileRecord file = fileRepository.findById(fileId);
        ParseTask task = parseTaskRepository.findById(parseTaskId);
        if (file == null || task == null || !fileId.equals(task.getFileId())) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        task.setStatus(ParseStatus.FAILED.name());
        task.setErrorMsg(errorMessage);
        task.setFinishedAt(now);
        parseTaskRepository.update(task);
        if (isLatestTask(fileId, parseTaskId)) {
            updateFileAndNote(file, ParseStatus.FAILED, 0, errorMessage);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void applyParseResult(ParseDoneMessage message) {
        Long fileId = parseId(message.getFileId());
        Long taskId = parseId(message.getParseTaskId());
        if (fileId == null || taskId == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "解析结果缺少文件或任务标识");
        }
        FileRecord file = fileRepository.findById(fileId);
        ParseTask task = parseTaskRepository.findById(taskId);
        if (file == null || task == null || !fileId.equals(task.getFileId())) {
            return;
        }
        if (isTerminal(task.getStatus()) || isStaleAttempt(task, message.getAttemptCount())) {
            return;
        }
        ParseStatus resultStatus = parseResultStatus(message.getStatus());
        int attemptCount = Math.max(1, message.getAttemptCount() == null ? 1 : message.getAttemptCount());
        LocalDateTime now = LocalDateTime.now();
        task.setAttemptCount(attemptCount);
        task.setStartedAt(task.getStartedAt() == null ? now : task.getStartedAt());
        task.setErrorMsg(message.getErrorMsg());
        if (resultStatus == ParseStatus.DONE) {
            task.setStatus(ParseStatus.DONE.name());
            task.setChunkCount(Math.max(0, message.getChunkCount() == null ? 0 : message.getChunkCount()));
            task.setErrorMsg(null);
            task.setFinishedAt(now);
        } else if (Boolean.TRUE.equals(message.getFinalFailed())) {
            task.setStatus(ParseStatus.FAILED.name());
            task.setChunkCount(0);
            task.setFinishedAt(now);
        } else {
            task.setStatus(ParseStatus.PROCESSING.name());
            task.setChunkCount(0);
        }
        parseTaskRepository.update(task);
        if (isLatestTask(fileId, taskId)) {
            updateLatestFileStatus(file, task, resultStatus, message);
        }
    }

    public FileParseStatusResponse getParseStatus(Long userId, Long fileId) {
        FileRecord file = requireFile(userId, fileId);
        ParseTask task = latestTask(file.getId());
        return FileParseStatusResponse.builder()
                .fileId(file.getId())
                .noteId(file.getNoteId())
                .parseStatus(file.getParseStatus())
                .chunkCount(file.getChunkCount())
                .parseError(file.getParseError())
                .parseTaskId(task == null ? null : task.getId())
                .attemptCount(task == null ? 0 : task.getAttemptCount())
                .taskStatus(task == null ? null : task.getStatus())
                .taskError(task == null ? null : task.getErrorMsg())
                .startedAt(task == null ? null : task.getStartedAt())
                .finishedAt(task == null ? null : task.getFinishedAt())
                .build();
    }

    public FileRecord requireFile(Long userId, Long fileId) {
        FileRecord file = fileRepository.findByIdAndUserId(fileId, userId);
        if (file == null) {
            throw new BusinessException(ResultCode.FILE_NOT_FOUND);
        }
        return file;
    }

    private ParseTask newParseTask(Long fileId, Long userId) {
        ParseTask task = new ParseTask();
        task.setFileId(fileId);
        task.setRequestedBy(userId);
        task.setStatus(ParseStatus.PENDING.name());
        task.setAttemptCount(0);
        task.setChunkCount(0);
        return task;
    }

    private void updateLatestFileStatus(
            FileRecord file,
            ParseTask task,
            ParseStatus resultStatus,
            ParseDoneMessage message
    ) {
        if (resultStatus == ParseStatus.DONE) {
            String content = fileStorage.readText(file.getBucket(), requiredObjectKey(message.getExtractedTextObjectKey()));
            knowledgeFacade.applyParsedContent(file.getUserId(), file.getNoteId(), message.getNoteVersion(), content);
            fileRepository.updateParseResult(file.getId(), file.getUserId(), ParseStatus.DONE.name(),
                    task.getChunkCount(), null);
            return;
        }
        if (Boolean.TRUE.equals(message.getFinalFailed())) {
            updateFileAndNote(file, ParseStatus.FAILED, 0, task.getErrorMsg(), message.getNoteVersion());
            return;
        }
        updateFileAndNote(file, ParseStatus.PROCESSING, 0, task.getErrorMsg(), message.getNoteVersion());
    }

    private void updateFileAndNote(FileRecord file, ParseStatus status, Integer chunkCount, String errorMessage) {
        updateFileAndNote(file, status, chunkCount, errorMessage, null);
    }

    private void updateFileAndNote(FileRecord file, ParseStatus status, Integer chunkCount, String errorMessage,
                                   Integer expectedNoteVersion) {
        fileRepository.updateParseResult(
                file.getId(), file.getUserId(), status.name(), chunkCount, errorMessage
        );
        if (expectedNoteVersion == null || isCurrentNoteVersion(file, expectedNoteVersion)) {
            knowledgeFacade.updateParseStatus(file.getUserId(), file.getNoteId(), status.name());
        }
    }

    private ParseTask latestTask(Long fileId) {
        return parseTaskRepository.findLatestByFileId(fileId);
    }

    private boolean isLatestTask(Long fileId, Long taskId) {
        ParseTask latestTask = latestTask(fileId);
        return latestTask != null && taskId.equals(latestTask.getId());
    }

    private boolean isTerminal(String status) {
        return ParseStatus.DONE.name().equals(status) || ParseStatus.FAILED.name().equals(status);
    }

    private boolean isStaleAttempt(ParseTask task, Integer attemptCount) {
        return attemptCount != null && attemptCount < task.getAttemptCount();
    }

    private ParseStatus parseResultStatus(String status) {
        try {
            ParseStatus resultStatus = ParseStatus.valueOf(status.toUpperCase(Locale.ROOT));
            if (resultStatus == ParseStatus.PENDING || resultStatus == ParseStatus.PROCESSING) {
                throw new IllegalArgumentException("unsupported result status");
            }
            return resultStatus;
        } catch (RuntimeException exception) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "解析结果状态非法");
        }
    }

    private Long parseId(String value) {
        try {
            return value == null ? null : Long.valueOf(value);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private String requiredObjectKey(String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "解析结果缺少正文对象键");
        }
        return value;
    }

    private boolean isCurrentNoteVersion(FileRecord file, Integer expectedVersion) {
        return expectedVersion.equals(knowledgeFacade.getActiveNote(file.getUserId(), file.getNoteId()).getVersion());
    }
}
