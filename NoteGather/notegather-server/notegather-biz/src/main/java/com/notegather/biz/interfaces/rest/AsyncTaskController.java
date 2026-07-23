package com.notegather.biz.interfaces.rest;

import com.notegather.biz.application.service.AsyncTaskService;
import com.notegather.biz.domain.asset.entity.AsyncTask;
import com.notegather.biz.interfaces.rest.dto.AsyncTaskDTO;
import com.notegather.biz.interfaces.rest.dto.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 异步任务接口
 */
@Tag(name = "异步任务", description = "异步任务相关接口")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AsyncTaskController {
    
    private final AsyncTaskService asyncTaskService;
    
    @Operation(summary = "创建文档解析任务")
    @PostMapping("/documents/{documentId}/parse")
    public Result<Map<String, Object>> createParseTask(@PathVariable Long documentId) {
        AsyncTask task = asyncTaskService.createDocumentParseTask(documentId);
        
        Map<String, Object> data = new HashMap<>();
        data.put("taskId", task.getId().getValue());
        data.put("status", task.getStatus().getDescription());
        
        return Result.success(data);
    }
    
    @Operation(summary = "查询任务详情")
    @GetMapping("/tasks/{taskId}")
    public Result<AsyncTaskDTO> getTask(@PathVariable Long taskId) {
        AsyncTask task = asyncTaskService.getTask(taskId);
        
        AsyncTaskDTO dto = new AsyncTaskDTO();
        dto.setId(task.getId().getValue());
        dto.setTaskType(task.getTaskType().getDescription());
        dto.setStatus(task.getStatus().getCode());
        dto.setStatusDesc(task.getStatus().getDescription());
        dto.setDocumentId(task.getRelatedDocumentId().getValue());
        dto.setErrorMessage(task.getErrorMessage());
        dto.setRetryCount(task.getRetryCount());
        dto.setCreatedAt(task.getCreatedAt());
        dto.setCompletedAt(task.getCompletedAt());
        dto.setExecutionDuration(task.getExecutionDuration());
        
        return Result.success(dto);
    }
}
