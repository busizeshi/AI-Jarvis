package com.notegather.biz.knowledge.domain.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.notegather.common.mybatis.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("t_note")
@EqualsAndHashCode(callSuper = true)
public class Note extends BaseEntity {

    private Long libraryId;
    private Long parentId;
    private Long userId;
    private String nodeType;
    private String title;
    private String content;
    private String noteType;
    private Integer sortOrder;
    private Integer version;
    private String parseStatus;
}
