package com.notegather.biz.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.notegather.biz.domain.identity.valueobject.UserId;
import com.notegather.biz.domain.knowledge.aggregate.Tag;
import com.notegather.biz.domain.knowledge.repository.TagRepository;
import com.notegather.biz.domain.knowledge.valueobject.DocumentId;
import com.notegather.biz.domain.knowledge.valueobject.TagId;
import com.notegather.biz.infrastructure.persistence.converter.TagConverter;
import com.notegather.biz.infrastructure.persistence.entity.DocumentTagEntity;
import com.notegather.biz.infrastructure.persistence.entity.TagEntity;
import com.notegather.biz.infrastructure.persistence.mapper.DocumentTagMapper;
import com.notegather.biz.infrastructure.persistence.mapper.TagMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 标签仓储实现
 */
@Repository
@RequiredArgsConstructor
public class TagRepositoryImpl implements TagRepository {
    
    private final TagMapper tagMapper;
    private final DocumentTagMapper documentTagMapper;
    private final TagConverter tagConverter;
    
    @Override
    public Tag save(Tag tag) {
        TagEntity entity = tagConverter.toEntity(tag);
        
        if (entity.getId() == null) {
            // 新增
            tagMapper.insert(entity);
            tag.setId(entity.getId());
        } else {
            // 更新
            tagMapper.updateById(entity);
        }
        
        return tag;
    }
    
    @Override
    public Optional<Tag> findById(TagId tagId) {
        TagEntity entity = tagMapper.selectById(tagId.getValue());
        return Optional.ofNullable(tagConverter.toDomain(entity));
    }
    
    @Override
    public List<Tag> findByOwnerId(UserId ownerId) {
        LambdaQueryWrapper<TagEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TagEntity::getOwnerId, ownerId.getValue())
               .orderByAsc(TagEntity::getName);
        
        return tagMapper.selectList(wrapper).stream()
                .map(tagConverter::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public Optional<Tag> findByOwnerIdAndName(UserId ownerId, String name) {
        LambdaQueryWrapper<TagEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TagEntity::getOwnerId, ownerId.getValue())
               .eq(TagEntity::getName, name);
        
        TagEntity entity = tagMapper.selectOne(wrapper);
        return Optional.ofNullable(tagConverter.toDomain(entity));
    }
    
    @Override
    public boolean existsByOwnerIdAndName(UserId ownerId, String name) {
        LambdaQueryWrapper<TagEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TagEntity::getOwnerId, ownerId.getValue())
               .eq(TagEntity::getName, name);
        
        return tagMapper.selectCount(wrapper) > 0;
    }
    
    @Override
    public void delete(TagId tagId) {
        tagMapper.deleteById(tagId.getValue());
    }
    
    @Override
    public void attachToDocument(DocumentId documentId, TagId tagId) {
        // 检查是否已关联
        LambdaQueryWrapper<DocumentTagEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DocumentTagEntity::getDocumentId, documentId.getValue())
               .eq(DocumentTagEntity::getTagId, tagId.getValue());
        
        if (documentTagMapper.selectCount(wrapper) > 0) {
            return;  // 已存在，不重复添加
        }
        
        DocumentTagEntity entity = new DocumentTagEntity();
        entity.setDocumentId(documentId.getValue());
        entity.setTagId(tagId.getValue());
        entity.setCreatedAt(LocalDateTime.now());
        
        documentTagMapper.insert(entity);
    }
    
    @Override
    public void detachFromDocument(DocumentId documentId, TagId tagId) {
        LambdaQueryWrapper<DocumentTagEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DocumentTagEntity::getDocumentId, documentId.getValue())
               .eq(DocumentTagEntity::getTagId, tagId.getValue());
        
        documentTagMapper.delete(wrapper);
    }
    
    @Override
    public List<Tag> findByDocumentId(DocumentId documentId) {
        LambdaQueryWrapper<DocumentTagEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DocumentTagEntity::getDocumentId, documentId.getValue());
        
        List<Long> tagIds = documentTagMapper.selectList(wrapper).stream()
                .map(DocumentTagEntity::getTagId)
                .collect(Collectors.toList());
        
        if (tagIds.isEmpty()) {
            return List.of();
        }
        
        return tagMapper.selectBatchIds(tagIds).stream()
                .map(tagConverter::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Long> findDocumentIdsByTagId(TagId tagId) {
        LambdaQueryWrapper<DocumentTagEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DocumentTagEntity::getTagId, tagId.getValue());
        
        return documentTagMapper.selectList(wrapper).stream()
                .map(DocumentTagEntity::getDocumentId)
                .collect(Collectors.toList());
    }
    
    @Override
    public void deleteAllByDocumentId(DocumentId documentId) {
        LambdaQueryWrapper<DocumentTagEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DocumentTagEntity::getDocumentId, documentId.getValue());
        
        documentTagMapper.delete(wrapper);
    }
}
