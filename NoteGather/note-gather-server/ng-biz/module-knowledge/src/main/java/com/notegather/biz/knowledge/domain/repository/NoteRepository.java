package com.notegather.biz.knowledge.domain.repository;

import com.notegather.biz.knowledge.domain.model.Note;

import java.util.List;

public interface NoteRepository {

    void save(Note note);

    Note findByIdAndUserId(Long noteId, Long userId);

    void updateParseStatus(Long noteId, Long userId, String parseStatus);

    Note findDeletedByIdAndUserId(Long noteId, Long userId);

    List<Note> findByLibraryId(Long libraryId, Long userId, boolean deleted);

    List<Note> findDeletedByUserId(Long userId);

    void update(Note note, Long userId);

    void delete(Long noteId, Long userId);

    boolean restore(Long noteId, Long userId, Long parentId);
}
