package com.notegather.common.mq.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 文件上传消息体（biz → Python 解析服务）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadedMessage implements Serializable {

    /** 文件ID */
    private String fileId;

    /** 用户ID */
    private String userId;

    /** 关联笔记ID（可为空，后续关联） */
    private String noteId;

    /** MinIO 对象键 */
    private String objectKey;

    /** MinIO Bucket */
    private String bucket;

    /** 文件原始名称 */
    private String fileName;

    /** 文件类型（PDF / TXT / MD / IMAGE / AUDIO / VIDEO） */
    private String fileType;

    /** 文件大小（字节） */
    private Long fileSize;
}
