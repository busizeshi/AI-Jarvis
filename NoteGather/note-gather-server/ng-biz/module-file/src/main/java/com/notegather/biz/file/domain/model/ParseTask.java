package com.notegather.biz.file.domain.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.notegather.common.mybatis.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@TableName("t_parse_task")
@EqualsAndHashCode(callSuper = true)
public class ParseTask extends BaseEntity {

    private Long fileId;
    private Long requestedBy;
    private String status;
    private Integer attemptCount;
    private Integer chunkCount;
    private String errorMsg;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
}
