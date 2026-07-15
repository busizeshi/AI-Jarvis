package com.notegather.biz.file.infrastructure.persistence.file.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.notegather.biz.file.domain.model.FileRecord;
import com.notegather.biz.file.domain.repository.FileRepository;
import com.notegather.biz.file.infrastructure.persistence.file.mapper.FileMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MybatisFileRepository implements FileRepository {

    private final FileMapper fileMapper;

    @Override
    public void save(FileRecord file) {
        fileMapper.insert(file);
    }

    @Override
    public FileRecord findById(Long fileId) {
        return fileMapper.selectById(fileId);
    }

    @Override
    public FileRecord findByIdAndUserId(Long fileId, Long userId) {
        return fileMapper.selectOne(new LambdaQueryWrapper<FileRecord>()
                .eq(FileRecord::getId, fileId)
                .eq(FileRecord::getUserId, userId)
                .last("LIMIT 1"));
    }

    @Override
    public void bindNote(Long fileId, Long userId, Long noteId) {
        FileRecord update = new FileRecord();
        update.setNoteId(noteId);
        fileMapper.update(update, ownedFile(fileId, userId));
    }

    @Override
    public void updateParseResult(Long fileId, Long userId, String parseStatus, Integer chunkCount, String parseError) {
        FileRecord update = new FileRecord();
        update.setParseStatus(parseStatus);
        update.setChunkCount(chunkCount);
        update.setParseError(parseError);
        fileMapper.update(update, ownedFile(fileId, userId));
    }

    private LambdaQueryWrapper<FileRecord> ownedFile(Long fileId, Long userId) {
        return new LambdaQueryWrapper<FileRecord>()
                .eq(FileRecord::getId, fileId)
                .eq(FileRecord::getUserId, userId);
    }
}
