package com.notegather.biz.file.domain.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.notegather.common.mybatis.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("t_file")
@EqualsAndHashCode(callSuper = true)
public class FileRecord extends BaseEntity {

    private Long userId;
    private Long libraryId;
    private Long noteId;
    private String fileName;
    private String fileType;
    private String contentType;
    private String objectKey;
    private String bucket;
    private Long size;
    private String parseStatus;
    private Integer chunkCount;
    private String parseError;
}
