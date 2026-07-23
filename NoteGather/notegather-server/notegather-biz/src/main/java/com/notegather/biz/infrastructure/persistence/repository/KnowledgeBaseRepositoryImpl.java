package com.notegather.biz.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.notegather.biz.domain.identity.valueobject.UserId;
import com.notegather.biz.domain.knowledge.aggregate.KnowledgeBase;
import com.notegather.biz.domain.knowledge.repository.KnowledgeBaseRepository;
import com.notegather.biz.domain.knowledge.valueobject.KnowledgeBaseId;
import com.notegather.biz.infrastructure.persistence.converter.KnowledgeBaseConverter;
import com.notegather.biz.infrastructure.persistence.entity.KnowledgeBaseEntity;
import com.notegather.biz.infrastructure.persistence.mapper.KnowledgeBaseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 知识库仓储实现
 * 
 * @author NoteGather
 * @since 1.0.0
 */
@Repository
@RequiredArgsConstructor
public class KnowledgeBaseRepositoryImpl implements KnowledgeBaseRepository {
    
    private final KnowledgeBaseMapper mapper;
    private final KnowledgeBaseConverter converter;
    
    @Override
    public KnowledgeBase save(KnowledgeBase knowledgeBase) {
        KnowledgeBaseEntity entity = converter.toEntity(knowledgeBase);
        
        if (entity.getId() == null) {
            // 新增
            mapper.insert(entity);
            knowledgeBase.setId(KnowledgeBaseId.of(entity.getId()));
        } else {
            // 更新
            mapper.updateById(entity);
        }
        
        return knowledgeBase;
    }
    
    @Override
    public Optional<KnowledgeBase> findById(KnowledgeBaseId id) {
        KnowledgeBaseEntity entity = mapper.selectById(id.getValue());
        return Optional.ofNullable(converter.toDomain(entity));
    }
    
    @Override
    public List<KnowledgeBase> findByOwnerId(UserId ownerId) {
        LambdaQueryWrapper<KnowledgeBaseEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeBaseEntity::getOwnerId, ownerId.getValue())
               .orderByDesc(KnowledgeBaseEntity::getCreatedAt);
        
        List<KnowledgeBaseEntity> entities = mapper.selectList(wrapper);
        return entities.stream()
                .map(converter::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<KnowledgeBase> findByOwnerIdWithPage(UserId ownerId, int page, int size) {
        Page<KnowledgeBaseEntity> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<KnowledgeBaseEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeBaseEntity::getOwnerId, ownerId.getValue())
               .orderByDesc(KnowledgeBaseEntity::getCreatedAt);
        
        Page<KnowledgeBaseEntity> result = mapper.selectPage(pageParam, wrapper);
        return result.getRecords().stream()
                .map(converter::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public long countByOwnerId(UserId ownerId) {
        LambdaQueryWrapper<KnowledgeBaseEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeBaseEntity::getOwnerId, ownerId.getValue());
        return mapper.selectCount(wrapper);
    }
    
    @Override
    public void deleteById(KnowledgeBaseId id) {
        mapper.deleteById(id.getValue());
    }
}
