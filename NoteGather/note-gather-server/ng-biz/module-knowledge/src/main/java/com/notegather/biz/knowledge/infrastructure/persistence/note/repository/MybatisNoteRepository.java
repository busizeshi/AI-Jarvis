package com.notegather.biz.knowledge.infrastructure.persistence.note.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.notegather.biz.knowledge.domain.model.Note;
import com.notegather.biz.knowledge.domain.repository.NoteRepository;
import com.notegather.biz.knowledge.infrastructure.persistence.note.mapper.NoteMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MybatisNoteRepository implements NoteRepository {

    private final NoteMapper noteMapper;

    @Override
    public void save(Note note) {
        noteMapper.insert(note);
    }

    @Override
    public Note findByIdAndUserId(Long noteId, Long userId) {
        return noteMapper.selectByIdAndUserId(noteId, userId, 0);
    }

    @Override
    public void updateParseStatus(Long noteId, Long userId, String parseStatus) {
        Note update = new Note();
        update.setParseStatus(parseStatus);
        noteMapper.update(update, new LambdaQueryWrapper<Note>()
                .eq(Note::getId, noteId)
                .eq(Note::getUserId, userId));
    }

    @Override
    public Note findDeletedByIdAndUserId(Long noteId, Long userId) {
        return noteMapper.selectByIdAndUserId(noteId, userId, 1);
    }

    @Override
    public java.util.List<Note> findByLibraryId(Long libraryId, Long userId, boolean deleted) {
        return noteMapper.selectByLibraryId(libraryId, userId, deleted ? 1 : 0);
    }

    @Override
    public java.util.List<Note> findDeletedByUserId(Long userId) {
        return noteMapper.selectDeletedByUserId(userId);
    }

    @Override
    public void update(Note note, Long userId) {
        noteMapper.update(note, new LambdaUpdateWrapper<Note>()
                .eq(Note::getId, note.getId())
                .eq(Note::getUserId, userId));
    }

    @Override
    public void delete(Long noteId, Long userId) {
        noteMapper.delete(new LambdaQueryWrapper<Note>()
                .eq(Note::getId, noteId)
                .eq(Note::getUserId, userId));
    }

    @Override
    public boolean restore(Long noteId, Long userId, Long parentId) {
        return noteMapper.restore(noteId, userId, parentId) > 0;
    }
}
