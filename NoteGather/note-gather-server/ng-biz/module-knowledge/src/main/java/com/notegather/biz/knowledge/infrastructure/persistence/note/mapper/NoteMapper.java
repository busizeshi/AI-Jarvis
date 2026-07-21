package com.notegather.biz.knowledge.infrastructure.persistence.note.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.notegather.biz.knowledge.domain.model.Note;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface NoteMapper extends BaseMapper<Note> {

    @Select("SELECT * FROM t_note WHERE library_id = #{libraryId} AND user_id = #{userId} "
            + "AND deleted = #{deleted} ORDER BY sort_order, id")
    List<Note> selectByLibraryId(@Param("libraryId") Long libraryId, @Param("userId") Long userId,
                                 @Param("deleted") int deleted);

    @Select("SELECT * FROM t_note WHERE id = #{noteId} AND user_id = #{userId} "
            + "AND deleted = #{deleted} LIMIT 1")
    Note selectByIdAndUserId(@Param("noteId") Long noteId, @Param("userId") Long userId,
                             @Param("deleted") int deleted);

    @Select("SELECT * FROM t_note WHERE user_id = #{userId} AND deleted = 1 ORDER BY update_time DESC")
    List<Note> selectDeletedByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM t_note WHERE user_id = #{userId} AND library_id = #{libraryId} "
            + "AND title = #{title} AND node_type = 'NOTE' AND deleted = 0 ORDER BY id")
    List<Note> selectActiveByTitle(@Param("userId") Long userId, @Param("libraryId") Long libraryId,
                                   @Param("title") String title);

    @Update("UPDATE t_note SET deleted = 0, parent_id = #{parentId} "
            + "WHERE id = #{noteId} AND user_id = #{userId} AND deleted = 1")
    int restore(@Param("noteId") Long noteId, @Param("userId") Long userId, @Param("parentId") Long parentId);
}
