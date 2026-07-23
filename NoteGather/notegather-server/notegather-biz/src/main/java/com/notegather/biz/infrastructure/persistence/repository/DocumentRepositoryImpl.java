package com.notegather.biz.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.notegather.biz.domain.knowledge.aggregate.Document;
import com.notegather.biz.domain.knowledge.repository.DocumentRepository;
import com.notegather.biz.domain.knowledge.valueobject.DocumentId;
import com.notegather.biz.domain.knowledge.valueobject.KnowledgeBaseId;
import com.notegather.biz.infrastructure.persistence.converter.DocumentConverter;
import com.notegather.biz.infrastructure.persistence.entity.DocumentEntity;
import com.notegather.biz.infrastructure.persistence.mapper.DocumentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 文档仓储实现
 */
@Repository
@RequiredArgsConstructor
public class DocumentRepositoryImpl implements DocumentRepository {
    
    private final DocumentMapper mapper;
    private final DocumentConverter converter;
    
    @Override
    public Document save(Document document) {
        DocumentEntity entity = converter.toEntity(document);
        
        if (entity.getId() == null) {
            mapper.insert(entity);
            document.setId(DocumentId.of(entity.getId()));
        } else {
            mapper.updateById(entity);
        }
        
        return document;
    }
    
    @Override
    public Optional<Document> findById(DocumentId id) {
        DocumentEntity entity = mapper.selectById(id.getValue());
        return Optional.ofNullable(converter.toDomain(entity));
    }
    
    @Override
    public List<Document> findByKnowledgeBaseId(KnowledgeBaseId knowledgeBaseId) {
        LambdaQueryWrapper<DocumentEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DocumentEntity::getKnowledgeBaseId, knowledgeBaseId.getValue())
               .orderByAsc(DocumentEntity::getDepth)
               .orderByAsc(DocumentEntity::getOrderNum);
        
        List<DocumentEntity> entities = mapper.selectList(wrapper);
        return entities.stream()
                .map(converter::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Document> findByParentId(DocumentId parentId) {
        LambdaQueryWrapper<DocumentEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DocumentEntity::getParentId, parentId.getValue())
               .orderByAsc(DocumentEntity::getOrderNum);
        
        List<DocumentEntity> entities = mapper.selectList(wrapper);
        return entities.stream()
                .map(converter::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Document> findByKnowledgeBaseIdAndParentId(KnowledgeBaseId knowledgeBaseId, DocumentId parentId) {
        LambdaQueryWrapper<DocumentEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DocumentEntity::getKnowledgeBaseId, knowledgeBaseId.getValue());
        
        if (parentId != null) {
            wrapper.eq(DocumentEntity::getParentId, parentId.getValue());
        } else {
            wrapper.isNull(DocumentEntity::getParentId);
        }
        
        wrapper.orderByAsc(DocumentEntity::getOrderNum);
        
        List<DocumentEntity> entities = mapper.selectList(wrapper);
        return entities.stream()
                .map(converter::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Document> findRootDocuments(KnowledgeBaseId knowledgeBaseId) {
        LambdaQueryWrapper<DocumentEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DocumentEntity::getKnowledgeBaseId, knowledgeBaseId.getValue())
               .isNull(DocumentEntity::getParentId)
               .orderByAsc(DocumentEntity::getOrderNum);
        
        List<DocumentEntity> entities = mapper.selectList(wrapper);
        return entities.stream()
                .map(converter::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public void deleteById(DocumentId id) {
        mapper.deleteById(id.getValue());
    }
}
