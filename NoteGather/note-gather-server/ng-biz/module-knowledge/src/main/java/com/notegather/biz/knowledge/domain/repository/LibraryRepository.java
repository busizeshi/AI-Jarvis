package com.notegather.biz.knowledge.domain.repository;

import com.notegather.biz.knowledge.domain.model.Library;

import java.util.List;

public interface LibraryRepository {

    Library findByIdAndUserId(Long libraryId, Long userId);

    Library findDeletedByIdAndUserId(Long libraryId, Long userId);

    List<Library> findByUserId(Long userId, boolean deleted);

    void save(Library library);

    void update(Library library, Long userId);

    void delete(Long libraryId, Long userId);

    boolean restore(Long libraryId, Long userId);
}
