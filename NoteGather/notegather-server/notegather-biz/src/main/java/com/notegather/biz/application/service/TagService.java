package com.notegather.biz.application.service;

import cn.dev33.satoken.stp.StpUtil;
import com.notegather.biz.application.command.CreateTagCommand;
import com.notegather.biz.application.command.UpdateTagCommand;
import com.notegather.biz.domain.identity.valueobject.UserId;
import com.notegather.biz.domain.knowledge.aggregate.Tag;
import com.notegather.biz.domain.knowledge.repository.TagRepository;
import com.notegather.biz.domain.knowledge.valueobject.DocumentId;
import com.notegather.biz.domain.knowledge.valueobject.TagId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 标签应用服务
 */
@Service
@RequiredArgsConstructor
public class TagService {
    
    private final TagRepository tagRepository;
    
    /**
     * 创建标签
     */
    @Transactional(rollbackFor = Exception.class)
    public Tag createTag(CreateTagCommand command) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        UserId userId = UserId.of(currentUserId);
        
        // 检查标签名称是否已存在
        if (tagRepository.existsByOwnerIdAndName(userId, command.getName())) {
            throw new RuntimeException("标签名称已存在");
        }
        
        // 创建标签
        Tag tag = Tag.create(userId, command.getName(), command.getColor());
        
        // 保存标签
        return tagRepository.save(tag);
    }
    
    /**
     * 更新标签
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateTag(Long id, UpdateTagCommand command) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        UserId userId = UserId.of(currentUserId);
        
        Tag tag = tagRepository.findById(TagId.of(id))
                .orElseThrow(() -> new RuntimeException("标签不存在"));
        
        // 权限检查
        if (!tag.isOwnedBy(userId)) {
            throw new RuntimeException("无权限操作该标签");
        }
        
        // 如果修改了名称，检查新名称是否已存在
        if (command.getName() != null && !command.getName().equals(tag.getName())) {
            if (tagRepository.existsByOwnerIdAndName(userId, command.getName())) {
                throw new RuntimeException("标签名称已存在");
            }
        }
        
        // 更新标签
        tag.update(command.getName(), command.getColor());
        tagRepository.save(tag);
    }
    
    /**
     * 删除标签
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteTag(Long id) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        UserId userId = UserId.of(currentUserId);
        
        Tag tag = tagRepository.findById(TagId.of(id))
                .orElseThrow(() -> new RuntimeException("标签不存在"));
        
        // 权限检查
        if (!tag.isOwnedBy(userId)) {
            throw new RuntimeException("无权限操作该标签");
        }
        
        // 删除标签（会级联删除关联关系）
        tagRepository.delete(TagId.of(id));
    }
    
    /**
     * 查询标签详情
     */
    public Tag getTag(Long id) {
        return tagRepository.findById(TagId.of(id))
                .orElseThrow(() -> new RuntimeException("标签不存在"));
    }
    
    /**
     * 查询我的所有标签
     */
    public List<Tag> getMyTags() {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        UserId userId = UserId.of(currentUserId);
        
        return tagRepository.findByOwnerId(userId);
    }
    
    /**
     * 为文档添加标签
     */
    @Transactional(rollbackFor = Exception.class)
    public void attachTagToDocument(Long documentId, Long tagId) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        UserId userId = UserId.of(currentUserId);
        
        // 检查标签权限
        Tag tag = tagRepository.findById(TagId.of(tagId))
                .orElseThrow(() -> new RuntimeException("标签不存在"));
        
        if (!tag.isOwnedBy(userId)) {
            throw new RuntimeException("无权限操作该标签");
        }
        
        // 添加关联
        tagRepository.attachToDocument(DocumentId.of(documentId), TagId.of(tagId));
    }
    
    /**
     * 从文档移除标签
     */
    @Transactional(rollbackFor = Exception.class)
    public void detachTagFromDocument(Long documentId, Long tagId) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        UserId userId = UserId.of(currentUserId);
        
        // 检查标签权限
        Tag tag = tagRepository.findById(TagId.of(tagId))
                .orElseThrow(() -> new RuntimeException("标签不存在"));
        
        if (!tag.isOwnedBy(userId)) {
            throw new RuntimeException("无权限操作该标签");
        }
        
        // 移除关联
        tagRepository.detachFromDocument(DocumentId.of(documentId), TagId.of(tagId));
    }
    
    /**
     * 查询文档的所有标签
     */
    public List<Tag> getDocumentTags(Long documentId) {
        return tagRepository.findByDocumentId(DocumentId.of(documentId));
    }
    
    /**
     * 查询使用该标签的文档ID列表
     */
    public List<Long> getDocumentIdsByTag(Long tagId) {
        return tagRepository.findDocumentIdsByTagId(TagId.of(tagId));
    }
}
