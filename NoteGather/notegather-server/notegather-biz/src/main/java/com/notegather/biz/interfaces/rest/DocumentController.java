package com.notegather.biz.interfaces.rest;

import com.notegather.biz.application.command.CreateDocumentCommand;
import com.notegather.biz.application.command.MoveDocumentCommand;
import com.notegather.biz.application.command.UpdateDocumentCommand;
import com.notegather.biz.application.service.DocumentFileService;
import com.notegather.biz.application.service.DocumentService;
import com.notegather.biz.domain.knowledge.aggregate.Document;
import com.notegather.biz.interfaces.rest.dto.DocumentDTO;
import com.notegather.biz.interfaces.rest.dto.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 文档接口
 */
@Tag(name = "文档管理", description = "文档相关接口")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DocumentController {
    
    private final DocumentService documentService;
    private final DocumentFileService documentFileService;
    
    @Operation(summary = "创建文档")
    @PostMapping("/knowledge-bases/{kbId}/documents")
    public Result<Map<String, Object>> create(
            @PathVariable Long kbId,
            @RequestBody CreateDocumentCommand command) {
        command.setKnowledgeBaseId(kbId);
        Document document = documentService.createDocument(command);
        
        Map<String, Object> data = new HashMap<>();
        data.put("id", document.getId().getValue());
        data.put("title", document.getTitle());
        data.put("type", document.getType().getCode());
        data.put("knowledgeBaseId", document.getKnowledgeBaseId().getValue());
        
        return Result.success(data);
    }
    
    @Operation(summary = "更新文档")
    @PutMapping("/documents/{id}")
    public Result<Void> update(
            @PathVariable Long id,
            @RequestBody UpdateDocumentCommand command) {
        command.setId(id);
        documentService.updateDocument(command);
        return Result.success();
    }
    
    @Operation(summary = "移动文档")
    @PutMapping("/documents/{id}/move")
    public Result<Void> move(
            @PathVariable Long id,
            @RequestBody MoveDocumentCommand command) {
        command.setId(id);
        documentService.moveDocument(command);
        return Result.success();
    }
    
    @Operation(summary = "删除文档")
    @DeleteMapping("/documents/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        documentService.deleteDocument(id);
        return Result.success();
    }
    
    @Operation(summary = "查询文档详情")
    @GetMapping("/documents/{id}")
    public Result<DocumentDTO> getById(@PathVariable Long id) {
        Document document = documentService.getDocument(id);
        return Result.success(toDTO(document));
    }
    
    @Operation(summary = "查询知识库下的所有文档")
    @GetMapping("/knowledge-bases/{kbId}/documents")
    public Result<List<DocumentDTO>> listByKnowledgeBase(@PathVariable Long kbId) {
        List<Document> documents = documentService.getDocumentsByKnowledgeBase(kbId);
        List<DocumentDTO> dtoList = documents.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return Result.success(dtoList);
    }
    
    @Operation(summary = "查询知识库根目录文档")
    @GetMapping("/knowledge-bases/{kbId}/documents/root")
    public Result<List<DocumentDTO>> listRootDocuments(@PathVariable Long kbId) {
        List<Document> documents = documentService.getRootDocuments(kbId);
        List<DocumentDTO> dtoList = documents.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return Result.success(dtoList);
    }
    
    @Operation(summary = "查询子文档列表")
    @GetMapping("/documents/{parentId}/children")
    public Result<List<DocumentDTO>> listChildren(@PathVariable Long parentId) {
        List<Document> documents = documentService.getChildDocuments(parentId);
        List<DocumentDTO> dtoList = documents.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return Result.success(dtoList);
    }
    
    @Operation(summary = "上传文档文件")
    @PostMapping("/documents/{id}/upload")
    public Result<Map<String, String>> uploadFile(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        String fileUrl = documentFileService.uploadDocumentFile(id, file);
        
        Map<String, String> data = new HashMap<>();
        data.put("fileUrl", fileUrl);
        
        return Result.success(data);
    }
    
    /**
     * 领域模型转 DTO
     */
    private DocumentDTO toDTO(Document doc) {
        DocumentDTO dto = new DocumentDTO();
        dto.setId(doc.getId().getValue());
        dto.setKnowledgeBaseId(doc.getKnowledgeBaseId().getValue());
        dto.setParentId(doc.getParentId() != null ? doc.getParentId().getValue() : null);
        dto.setOwnerId(doc.getOwnerId().getValue());
        dto.setTitle(doc.getTitle());
        dto.setType(doc.getType().getCode());
        dto.setContent(doc.getContent());
        dto.setContentType(doc.getContentType());
        dto.setFileName(doc.getFileName());
        dto.setFileSize(doc.getFileSize());
        dto.setFileUrl(doc.getFileUrl());
        dto.setDepth(doc.getDepth());
        dto.setOrderNum(doc.getOrderNum());
        dto.setStatus(doc.getStatus().getCode());
        dto.setCreatedAt(doc.getCreatedAt());
        dto.setUpdatedAt(doc.getUpdatedAt());
        return dto;
    }
}
