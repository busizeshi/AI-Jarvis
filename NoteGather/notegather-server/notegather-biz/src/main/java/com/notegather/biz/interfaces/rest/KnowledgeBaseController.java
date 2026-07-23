package com.notegather.biz.interfaces.rest;

import com.notegather.biz.application.command.CreateKnowledgeBaseCommand;
import com.notegather.biz.application.command.UpdateKnowledgeBaseCommand;
import com.notegather.biz.application.service.KnowledgeBaseService;
import com.notegather.biz.domain.knowledge.aggregate.KnowledgeBase;
import com.notegather.biz.interfaces.rest.dto.KnowledgeBaseDTO;
import com.notegather.biz.interfaces.rest.dto.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 知识库接口
 * 
 * @author NoteGather
 * @since 1.0.0
 */
@Tag(name = "知识库管理", description = "知识库相关接口")
@RestController
@RequestMapping("/api/knowledge-bases")
@RequiredArgsConstructor
public class KnowledgeBaseController {
    
    private final KnowledgeBaseService knowledgeBaseService;
    
    @Operation(summary = "创建知识库")
    @PostMapping
    public Result<Map<String, Object>> create(@RequestBody CreateKnowledgeBaseCommand command) {
        KnowledgeBase knowledgeBase = knowledgeBaseService.createKnowledgeBase(command);
        
        Map<String, Object> data = new HashMap<>();
        data.put("id", knowledgeBase.getId().getValue());
        data.put("name", knowledgeBase.getName());
        data.put("ownerId", knowledgeBase.getOwnerId().getValue());
        
        return Result.success(data);
    }
    
    @Operation(summary = "更新知识库")
    @PutMapping("/{id}")
    public Result<Void> update(
            @PathVariable Long id,
            @RequestBody UpdateKnowledgeBaseCommand command) {
        command.setId(id);
        knowledgeBaseService.updateKnowledgeBase(command);
        return Result.success();
    }
    
    @Operation(summary = "删除知识库")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        knowledgeBaseService.deleteKnowledgeBase(id);
        return Result.success();
    }
    
    @Operation(summary = "查询知识库详情")
    @GetMapping("/{id}")
    public Result<KnowledgeBaseDTO> getById(@PathVariable Long id) {
        KnowledgeBase knowledgeBase = knowledgeBaseService.getKnowledgeBase(id);
        return Result.success(toDTO(knowledgeBase));
    }
    
    @Operation(summary = "查询我的知识库列表")
    @GetMapping
    public Result<Map<String, Object>> list(
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size) {
        
        List<KnowledgeBase> knowledgeBases;
        long total;
        
        if (page != null && size != null) {
            knowledgeBases = knowledgeBaseService.getMyKnowledgeBasesWithPage(page, size);
            total = knowledgeBaseService.countMyKnowledgeBases();
        } else {
            knowledgeBases = knowledgeBaseService.getMyKnowledgeBases();
            total = knowledgeBases.size();
        }
        
        List<KnowledgeBaseDTO> dtoList = knowledgeBases.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        
        Map<String, Object> data = new HashMap<>();
        data.put("total", total);
        data.put("items", dtoList);
        data.put("page", page);
        data.put("size", size);
        
        return Result.success(data);
    }
    
    /**
     * 领域模型转 DTO
     */
    private KnowledgeBaseDTO toDTO(KnowledgeBase kb) {
        KnowledgeBaseDTO dto = new KnowledgeBaseDTO();
        dto.setId(kb.getId().getValue());
        dto.setOwnerId(kb.getOwnerId().getValue());
        dto.setName(kb.getName());
        dto.setDescription(kb.getDescription());
        dto.setIcon(kb.getIcon());
        dto.setVisibility(kb.getVisibility().getCode());
        dto.setDocCount(kb.getDocCount());
        dto.setCreatedAt(kb.getCreatedAt());
        dto.setUpdatedAt(kb.getUpdatedAt());
        return dto;
    }
}
