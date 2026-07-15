package com.notegather.biz.file.infrastructure.persistence.parsetask.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.notegather.biz.file.domain.enums.ParseStatus;
import com.notegather.biz.file.domain.model.ParseTask;
import com.notegather.biz.file.domain.repository.ParseTaskRepository;
import com.notegather.biz.file.infrastructure.persistence.parsetask.mapper.ParseTaskMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MybatisParseTaskRepository implements ParseTaskRepository {

    private final ParseTaskMapper parseTaskMapper;

    @Override
    public void save(ParseTask task) {
        parseTaskMapper.insert(task);
    }

    @Override
    public ParseTask findById(Long taskId) {
        return parseTaskMapper.selectById(taskId);
    }

    @Override
    public ParseTask findActiveByFileId(Long fileId) {
        return parseTaskMapper.selectOne(new LambdaQueryWrapper<ParseTask>()
                .eq(ParseTask::getFileId, fileId)
                .in(ParseTask::getStatus, ParseStatus.PENDING.name(), ParseStatus.PROCESSING.name())
                .orderByDesc(ParseTask::getId)
                .last("LIMIT 1"));
    }

    @Override
    public ParseTask findLatestByFileId(Long fileId) {
        return parseTaskMapper.selectOne(new LambdaQueryWrapper<ParseTask>()
                .eq(ParseTask::getFileId, fileId)
                .orderByDesc(ParseTask::getCreateTime)
                .orderByDesc(ParseTask::getId)
                .last("LIMIT 1"));
    }

    @Override
    public void update(ParseTask task) {
        parseTaskMapper.updateById(task);
    }
}
