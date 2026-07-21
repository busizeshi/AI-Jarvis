package com.notegather.biz.knowledge.infrastructure.persistence.library.repository;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.notegather.biz.knowledge.domain.model.Library;
import com.notegather.biz.knowledge.domain.repository.LibraryRepository;
import com.notegather.biz.knowledge.infrastructure.persistence.library.mapper.LibraryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MybatisLibraryRepository implements LibraryRepository {

    private final LibraryMapper libraryMapper;

    @Override
    public Library findByIdAndUserId(Long libraryId, Long userId) {
        return libraryMapper.selectByIdAndUserId(libraryId, userId, 0);
    }

    @Override
    public Library findById(Long libraryId) {
        return libraryMapper.selectById(libraryId);
    }

    @Override
    public Library findDeletedByIdAndUserId(Long libraryId, Long userId) {
        return libraryMapper.selectByIdAndUserId(libraryId, userId, 1);
    }

    @Override
    public java.util.List<Library> findByUserId(Long userId, boolean deleted) {
        return libraryMapper.selectByUserId(userId, deleted ? 1 : 0);
    }

    @Override
    public void save(Library library) {
        libraryMapper.insert(library);
    }

    @Override
    public void update(Library library, Long userId) {
        libraryMapper.update(library, new LambdaUpdateWrapper<Library>()
                .eq(Library::getId, library.getId())
                .eq(Library::getUserId, userId));
    }

    @Override
    public void delete(Long libraryId, Long userId) {
        libraryMapper.delete(new LambdaUpdateWrapper<Library>()
                .eq(Library::getId, libraryId)
                .eq(Library::getUserId, userId));
    }

    @Override
    public boolean restore(Long libraryId, Long userId) {
        return libraryMapper.restore(libraryId, userId) > 0;
    }
}
