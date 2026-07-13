package com.notegather.common.mybatis.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 所有业务实体的公共基类
 * - id：雪花算法主键（BIGINT，由 MyBatis-Plus 自动生成）
 * - createTime / updateTime：自动填充
 * - deleted：逻辑删除标记（0=未删除，1=已删除）
 */
@Data
@EqualsAndHashCode(callSuper = false)
public abstract class BaseEntity implements Serializable {

    /** 主键（雪花算法，MyBatis-Plus 自动生成） */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 创建时间（自动填充） */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间（自动填充） */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 逻辑删除标记（0=未删除，1=已删除），MyBatis-Plus 自动处理 */
    @TableLogic(value = "0", delval = "1")
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;
}
