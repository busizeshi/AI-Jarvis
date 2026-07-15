package com.notegather.biz.knowledge.domain.model;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_note_version")
public class NoteVersion {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long noteId;
    private Integer version;
    private String title;
    private String content;
    private Long createdBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
