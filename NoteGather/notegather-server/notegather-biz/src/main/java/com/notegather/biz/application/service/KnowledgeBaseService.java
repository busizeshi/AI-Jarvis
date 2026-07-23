package com.notegather.biz.application.service;

import cn.dev33.satoken.stp.StpUtil;
import com.notegather.biz.application.command.CreateKnowledgeBaseCommand;
import com.notegather.biz.application.command.UpdateKnowledgeBaseCommand;
import com.notegather.biz.domain.identity.valueobject.UserId;
import com.notegather.biz.domain.knowledge.aggregate.KnowledgeBase;
import com.notegather.biz.domain.knowledge.repository.KnowledgeBaseRepository;
import com.notegather.biz.domain.knowledge.valueobject.KnowledgeBaseId;
import com.notegather.biz.domain.knowledge.valueobject.Visibility;
import com.notegather.biz.domain.permission.aggregate.ResourceGrant;
import com.notegather.biz.domain.permission.repository.ResourceGrantRepository;
import com.notegather.biz.domain.permission.valueobject.PermissionLevel;
import com.notegather.biz.domain.permission.valueobject.ResourceId;
import com.notegather.biz.domain.permission.valueobject.ResourceType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 知识库应用服务
 * 
 * @author NoteGather
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class KnowledgeBaseService {
    
    private final KnowledgeBaseRepository knowledgeBaseRepository;
    private final ResourceGrantRepository resourceGrantRepository;
    
    /**
     * 创建知识库
     */
    @Transactional(rollbackFor = Exception.class)
    public KnowledgeBase createKnowledgeBase(CreateKnowledgeBaseCommand command) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        UserId ownerId = UserId.of(currentUserId);
        
        Visibility visibility = command.getVisibility() != null 
            ? Visibility.fromCode(command.getVisibility())
            : Visibility.PRIVATE;
        
        KnowledgeBase knowledgeBase = KnowledgeBase.create(
            ownerId,
            command.getName(),
            command.getDescription(),
            visibility
        );
        
        knowledgeBase = knowledgeBaseRepository.save(knowledgeBase);
        
        // 自动为创建者添加 OWNER 权限
        ResourceGrant ownerGrant = ResourceGrant.create(
            ResourceType.KNOWLEDGE_BASE,
            ResourceId.of(knowledgeBase.getId().getValue()),
            ownerId,
            PermissionLevel.OWNER,
            ownerId
        );
        resourceGrantRepository.save(ownerGrant);
        
        return knowledgeBase;
    }
    
    /**
     * 更新知识库
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateKnowledgeBase(UpdateKnowledgeBaseCommand command) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        UserId userId = UserId.of(currentUserId);
        
        KnowledgeBase knowledgeBase = knowledgeBaseRepository
            .findById(KnowledgeBaseId.of(command.getId()))
            .orElseThrow(() -> new RuntimeException("知识库不存在"));
        
        // 权限检查：只有所有者可以更新
        if (!knowledgeBase.isOwnedBy(userId)) {
            throw new RuntimeException("无权限操作该知识库");
        }
        
        Visibility visibility = command.getVisibility() != null
            ? Visibility.fromCode(command.getVisibility())
            : null;
        
        knowledgeBase.updateInfo(
            command.getName(),
            command.getDescription(),
            visibility
        );
        
        knowledgeBaseRepository.save(knowledgeBase);
    }
    
    /**
     * 删除知识库（软删除）
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteKnowledgeBase(Long id) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        UserId userId = UserId.of(currentUserId);
        
        KnowledgeBase knowledgeBase = knowledgeBaseRepository
            .findById(KnowledgeBaseId.of(id))
            .orElseThrow(() -> new RuntimeException("知识库不存在"));
        
        // 权限检查
        if (!knowledgeBase.isOwnedBy(userId)) {
            throw new RuntimeException("无权限操作该知识库");
        }
        
        // 使用 MyBatis Plus 的逻辑删除
        knowledgeBaseRepository.deleteById(KnowledgeBaseId.of(id));
    }
    
    /**
     * 查询知识库详情
     */
    public KnowledgeBase getKnowledgeBase(Long id) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        UserId userId = UserId.of(currentUserId);
        
        KnowledgeBase knowledgeBase = knowledgeBaseRepository
            .findById(KnowledgeBaseId.of(id))
            .orElseThrow(() -> new RuntimeException("知识库不存在"));
        
        // 权限检查：暂时只允许所有者查看
        if (!knowledgeBase.isOwnedBy(userId)) {
            throw new RuntimeException("无权限访问该知识库");
        }
        
        return knowledgeBase;
    }
    
    /**
     * 查询我的知识库列表
     */
    public List<KnowledgeBase> getMyKnowledgeBases() {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        UserId ownerId = UserId.of(currentUserId);
        
        return knowledgeBaseRepository.findByOwnerId(ownerId);
    }
    
    /**
     * 分页查询我的知识库
     */
    public List<KnowledgeBase> getMyKnowledgeBasesWithPage(int page, int size) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        UserId ownerId = UserId.of(currentUserId);
        
        return knowledgeBaseRepository.findByOwnerIdWithPage(ownerId, page, size);
    }
    
    /**
     * 统计我的知识库数量
     */
    public long countMyKnowledgeBases() {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        UserId ownerId = UserId.of(currentUserId);
        
        return knowledgeBaseRepository.countByOwnerId(ownerId);
    }
}
