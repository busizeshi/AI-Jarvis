package com.notegather.biz.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.notegather.biz.domain.asset.entity.AsyncTask;
import com.notegather.biz.domain.asset.repository.AsyncTaskRepository;
import com.notegather.biz.domain.asset.valueobject.TaskId;
import com.notegather.biz.domain.asset.valueobject.TaskStatus;
import com.notegather.biz.infrastructure.persistence.converter.AsyncTaskConverter;
import com.notegather.biz.infrastructure.persistence.entity.AsyncTaskEntity;
import com.notegather.biz.infrastructure.persistence.mapper.AsyncTaskMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 异步任务仓储实现
 */
@Repository
@RequiredArgsConstructor
public class AsyncTaskRepositoryImpl implements AsyncTaskRepository {
    
    private final AsyncTaskMapper asyncTaskMapper;
    private final AsyncTaskConverter asyncTaskConverter;
    
    @Override
    public AsyncTask save(AsyncTask task) {
        AsyncTaskEntity entity = asyncTaskConverter.toEntity(task);
        
        if (entity.getId() == null) {
            asyncTaskMapper.insert(entity);
        } else {
            asyncTaskMapper.updateById(entity);
        }
        
        // 简化处理：直接返回原对象
        // 实际应该重新查询并转换
        return task;
    }
    
    @Override
    public Optional<AsyncTask> findById(TaskId taskId) {
        AsyncTaskEntity entity = asyncTaskMapper.selectById(taskId.getValue());
        if (entity == null) {
            return Optional.empty();
        }
        return Optional.of(asyncTaskConverter.toDomain(entity));
    }
    
    @Override
    public Optional<AsyncTask> findLatestByDocumentId(Long documentId) {
        LambdaQueryWrapper<AsyncTaskEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AsyncTaskEntity::getResourceType, "DOCUMENT")
               .eq(AsyncTaskEntity::getResourceId, documentId)
               .orderByDesc(AsyncTaskEntity::getCreatedAt)
               .last("LIMIT 1");
        
        AsyncTaskEntity entity = asyncTaskMapper.selectOne(wrapper);
        if (entity == null) {
            return Optional.empty();
        }
        return Optional.of(asyncTaskConverter.toDomain(entity));
    }
    
    @Override
    public List<AsyncTask> findByStatus(TaskStatus status, int limit) {
        LambdaQueryWrapper<AsyncTaskEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AsyncTaskEntity::getStatus, status.getCode())
               .orderByAsc(AsyncTaskEntity::getCreatedAt)
               .last("LIMIT " + limit);
        
        return asyncTaskMapper.selectList(wrapper).stream()
                .map(asyncTaskConverter::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<AsyncTask> findByUserId(Long userId, int page, int size) {
        Page<AsyncTaskEntity> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<AsyncTaskEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AsyncTaskEntity::getUserId, userId)
               .orderByDesc(AsyncTaskEntity::getCreatedAt);
        
        Page<AsyncTaskEntity> result = asyncTaskMapper.selectPage(pageParam, wrapper);
        return result.getRecords().stream()
                .map(asyncTaskConverter::toDomain)
                .collect(Collectors.toList());
    }
}
