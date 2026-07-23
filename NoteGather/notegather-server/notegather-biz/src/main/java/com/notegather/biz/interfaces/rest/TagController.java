package com.notegather.biz.interfaces.rest;

import cn.dev33.satoken.stp.StpUtil;
import com.notegather.biz.application.command.CreateTagCommand;
import com.notegather.biz.application.command.UpdateTagCommand;
import com.notegather.biz.application.service.TagService;
import com.notegather.biz.domain.knowledge.aggregate.Tag;
import com.notegather.biz.interfaces.rest.dto.Result;
import com.notegather.biz.interfaces.rest.dto.TagDTO;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 标签接口
 */
@io.swagger.v3.oas.annotations.tags.Tag(name = "标签管理", description = "标签相关接口")
@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {
    
    private final TagService tagService;
    
    @Operation(summary = "创建标签")
    @PostMapping
    public Result<Map<String, Object>> create(@Validated @RequestBody CreateTagCommand command) {
        Tag tag = tagService.createTag(command);
        
        Map<String, Object> data = new HashMap<>();
        data.put("id", tag.getId().getValue());
        data.put("name", tag.getName());
        data.put("color", tag.getColor());
        
        return Result.success(data);
    }
    
    @Operation(summary = "更新标签")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Validated @RequestBody UpdateTagCommand command) {
        tagService.updateTag(id, command);
        return Result.success();
    }
    
    @Operation(summary = "删除标签")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        tagService.deleteTag(id);
        return Result.success();
    }
    
    @Operation(summary = "查询标签详情")
    @GetMapping("/{id}")
    public Result<TagDTO> getById(@PathVariable Long id) {
        Tag tag = tagService.getTag(id);
        return Result.success(toDTO(tag));
    }
    
    @Operation(summary = "查询我的所有标签")
    @GetMapping
    public Result<List<TagDTO>> getMyTags() {
        List<Tag> tags = tagService.getMyTags();
        List<TagDTO> dtos = tags.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return Result.success(dtos);
    }
    
    @Operation(summary = "为文档添加标签")
    @PostMapping("/documents/{documentId}/tags/{tagId}")
    public Result<Void> attachToDocument(@PathVariable Long documentId, @PathVariable Long tagId) {
        tagService.attachTagToDocument(documentId, tagId);
        return Result.success();
    }
    
    @Operation(summary = "从文档移除标签")
    @DeleteMapping("/documents/{documentId}/tags/{tagId}")
    public Result<Void> detachFromDocument(@PathVariable Long documentId, @PathVariable Long tagId) {
        tagService.detachTagFromDocument(documentId, tagId);
        return Result.success();
    }
    
    @Operation(summary = "查询文档的所有标签")
    @GetMapping("/documents/{documentId}")
    public Result<List<TagDTO>> getDocumentTags(@PathVariable Long documentId) {
        List<Tag> tags = tagService.getDocumentTags(documentId);
        List<TagDTO> dtos = tags.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return Result.success(dtos);
    }
    
    @Operation(summary = "查询使用该标签的文档列表")
    @GetMapping("/{tagId}/documents")
    public Result<List<Long>> getDocumentsByTag(@PathVariable Long tagId) {
        List<Long> documentIds = tagService.getDocumentIdsByTag(tagId);
        return Result.success(documentIds);
    }
    
    private TagDTO toDTO(Tag tag) {
        TagDTO dto = new TagDTO();
        dto.setId(tag.getId().getValue());
        dto.setName(tag.getName());
        dto.setColor(tag.getColor());
        dto.setCreatedAt(tag.getCreatedAt());
        dto.setUpdatedAt(tag.getUpdatedAt());
        return dto;
    }
}
