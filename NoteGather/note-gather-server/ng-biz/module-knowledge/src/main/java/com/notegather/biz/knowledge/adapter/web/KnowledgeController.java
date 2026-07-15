package com.notegather.biz.knowledge.adapter.web;

import com.notegather.biz.knowledge.application.dto.NoteTreeNode;
import com.notegather.biz.knowledge.application.service.KnowledgeService;
import com.notegather.biz.knowledge.domain.enums.LibraryType;
import com.notegather.biz.knowledge.domain.enums.NodeType;
import com.notegather.biz.knowledge.domain.enums.NoteType;
import com.notegather.biz.knowledge.domain.model.Library;
import com.notegather.biz.knowledge.domain.model.Note;
import com.notegather.biz.knowledge.domain.model.NoteVersion;
import com.notegather.common.core.result.Result;
import com.notegather.common.security.context.UserContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class KnowledgeController {

    private final KnowledgeService knowledgeService;

    @GetMapping("/libraries")
    public Result<List<Library>> libraries() {
        return Result.ok(knowledgeService.listLibraries(UserContext.getUserId()));
    }

    @PostMapping("/libraries")
    public Result<Library> createLibrary(@Valid @RequestBody LibraryRequest request) {
        return Result.ok(knowledgeService.createLibrary(UserContext.getUserId(), request.name(), request.type(),
                request.description(), request.sortOrder()));
    }

    @GetMapping("/libraries/{libraryId}")
    public Result<Library> library(@PathVariable Long libraryId) {
        return Result.ok(knowledgeService.getLibrary(UserContext.getUserId(), libraryId));
    }

    @PutMapping("/libraries/{libraryId}")
    public Result<Library> updateLibrary(@PathVariable Long libraryId, @Valid @RequestBody LibraryUpdateRequest request) {
        return Result.ok(knowledgeService.updateLibrary(UserContext.getUserId(), libraryId, request.name(), request.type(),
                request.description(), request.sortOrder()));
    }

    @DeleteMapping("/libraries/{libraryId}")
    public Result<Void> deleteLibrary(@PathVariable Long libraryId) {
        knowledgeService.deleteLibrary(UserContext.getUserId(), libraryId);
        return Result.ok();
    }

    @PostMapping("/libraries/{libraryId}/restore")
    public Result<Void> restoreLibrary(@PathVariable Long libraryId) {
        knowledgeService.restoreLibrary(UserContext.getUserId(), libraryId);
        return Result.ok();
    }

    @GetMapping("/recycle-bin/libraries")
    public Result<List<Library>> recycleLibraries() {
        return Result.ok(knowledgeService.recycleLibraries(UserContext.getUserId()));
    }

    @GetMapping("/libraries/{libraryId}/tree")
    public Result<List<NoteTreeNode>> tree(@PathVariable Long libraryId) {
        return Result.ok(knowledgeService.tree(UserContext.getUserId(), libraryId));
    }

    @PostMapping("/notes")
    public Result<Note> createNote(@Valid @RequestBody NoteCreateRequest request) {
        return Result.ok(knowledgeService.createNote(UserContext.getUserId(), request.libraryId(), request.parentId(),
                request.nodeType(), request.title(), request.noteType(), request.content(), request.sortOrder()));
    }

    @GetMapping("/notes/{noteId}")
    public Result<Note> note(@PathVariable Long noteId) {
        return Result.ok(knowledgeService.getNote(UserContext.getUserId(), noteId));
    }

    @PutMapping("/notes/{noteId}")
    public Result<Note> updateNote(@PathVariable Long noteId, @RequestBody NoteUpdateRequest request) {
        return Result.ok(knowledgeService.updateNote(UserContext.getUserId(), noteId, request.title(), request.content(),
                request.noteType()));
    }

    @PostMapping("/notes/{noteId}/move")
    public Result<Void> move(@PathVariable Long noteId, @Valid @RequestBody NoteMoveRequest request) {
        knowledgeService.move(UserContext.getUserId(), noteId, request.targetParentId(), request.sortOrder());
        return Result.ok();
    }

    @PostMapping("/notes/reorder")
    public Result<Void> reorder(@Valid @RequestBody NoteReorderRequest request) {
        knowledgeService.reorder(UserContext.getUserId(), request.libraryId(), request.parentId(), request.orderedNoteIds());
        return Result.ok();
    }

    @DeleteMapping("/notes/{noteId}")
    public Result<Void> deleteNote(@PathVariable Long noteId) {
        knowledgeService.deleteNote(UserContext.getUserId(), noteId);
        return Result.ok();
    }

    @GetMapping("/notes/{noteId}/versions")
    public Result<List<NoteVersion>> versions(@PathVariable Long noteId) {
        return Result.ok(knowledgeService.listVersions(UserContext.getUserId(), noteId));
    }

    @PostMapping("/notes/{noteId}/versions/{version}/restore")
    public Result<Note> restoreVersion(@PathVariable Long noteId, @PathVariable Integer version) {
        return Result.ok(knowledgeService.restoreVersion(UserContext.getUserId(), noteId, version));
    }

    @GetMapping("/recycle-bin/notes")
    public Result<List<Note>> recycleNotes(@RequestParam(required = false) Long libraryId) {
        List<Note> notes = knowledgeService.recycleNotes(UserContext.getUserId());
        return Result.ok(libraryId == null ? notes : notes.stream().filter(note -> libraryId.equals(note.getLibraryId())).toList());
    }

    @PostMapping("/notes/{noteId}/restore")
    public Result<Void> restoreNote(@PathVariable Long noteId) {
        knowledgeService.restoreNote(UserContext.getUserId(), noteId);
        return Result.ok();
    }

    public record LibraryRequest(
            @NotBlank @NotNull String name,
            @NotNull LibraryType type,
            String description,
            @PositiveOrZero Integer sortOrder
    ) {
    }

    public record LibraryUpdateRequest(
            String name,
            LibraryType type,
            String description,
            @PositiveOrZero Integer sortOrder
    ) {
    }

    public record NoteCreateRequest(
            @NotNull Long libraryId,
            Long parentId,
            @NotNull NodeType nodeType,
            @NotBlank String title,
            NoteType noteType,
            String content,
            @PositiveOrZero Integer sortOrder
    ) {
    }

    public record NoteUpdateRequest(String title, String content, NoteType noteType) {
    }

    public record NoteMoveRequest(Long targetParentId, @PositiveOrZero Integer sortOrder) {
    }

    public record NoteReorderRequest(
            @NotNull Long libraryId,
            Long parentId,
            @NotEmpty List<Long> orderedNoteIds
    ) {
    }
}
