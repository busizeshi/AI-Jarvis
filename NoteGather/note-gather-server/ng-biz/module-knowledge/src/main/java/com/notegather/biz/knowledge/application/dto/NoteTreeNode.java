package com.notegather.biz.knowledge.application.dto;

import com.notegather.biz.knowledge.domain.model.Note;

import java.util.ArrayList;
import java.util.List;

public record NoteTreeNode(
        Long noteId,
        Long parentId,
        String nodeType,
        String title,
        String noteType,
        String parseStatus,
        Integer sortOrder,
        List<NoteTreeNode> children
) {
    public static NoteTreeNode from(Note note) {
        return new NoteTreeNode(note.getId(), note.getParentId(), note.getNodeType(), note.getTitle(),
                note.getNoteType(), note.getParseStatus(), note.getSortOrder(), new ArrayList<>());
    }
}
