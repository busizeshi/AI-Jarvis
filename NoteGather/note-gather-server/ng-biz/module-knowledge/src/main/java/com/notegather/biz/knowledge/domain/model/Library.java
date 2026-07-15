package com.notegather.biz.knowledge.domain.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.notegather.common.mybatis.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("t_library")
@EqualsAndHashCode(callSuper = true)
public class Library extends BaseEntity {

    private Long userId;
    private String name;
    private String type;
    private String description;
    private Integer sortOrder;
}
