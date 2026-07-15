package com.notegather.biz.knowledge.infrastructure.persistence.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.notegather.biz.knowledge.domain.model.Library;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface LibraryMapper extends BaseMapper<Library> {

    @Select("SELECT * FROM t_library WHERE user_id = #{userId} AND deleted = #{deleted} "
            + "ORDER BY sort_order, id")
    List<Library> selectByUserId(@Param("userId") Long userId, @Param("deleted") int deleted);

    @Select("SELECT * FROM t_library WHERE id = #{libraryId} AND user_id = #{userId} "
            + "AND deleted = #{deleted} LIMIT 1")
    Library selectByIdAndUserId(@Param("libraryId") Long libraryId, @Param("userId") Long userId,
                                @Param("deleted") int deleted);

    @Update("UPDATE t_library SET deleted = 0 WHERE id = #{libraryId} AND user_id = #{userId} AND deleted = 1")
    int restore(@Param("libraryId") Long libraryId, @Param("userId") Long userId);
}
