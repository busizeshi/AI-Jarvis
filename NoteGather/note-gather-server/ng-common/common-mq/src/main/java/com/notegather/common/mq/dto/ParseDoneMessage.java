package com.notegather.common.mq.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Python 解析完成消息体（Python 解析服务 → biz）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParseDoneMessage implements Serializable {

    /** 文件ID */
    private String fileId;

    /** 关联笔记ID */
    private String noteId;

    /** 对应的解析任务ID */
    private String parseTaskId;

    /** 用户ID，用于下游图谱任务隔离 */
    private String userId;

    /** 知识库ID，用于构图任务和图查询隔离 */
    private String libraryId;

    /** 笔记标题快照 */
    private String noteTitle;

    /** MinIO 对象键 */
    private String objectKey;

    /** MinIO Bucket */
    private String bucket;

    /** 文件类型 */
    private String fileType;

    /** 解析状态：DONE / FAILED */
    private String status;

    /** 成功入库的分块数量 */
    private Integer chunkCount;

    /** MinIO 中的解析正文 sidecar 对象键，用于回写上传文件对应笔记正文。 */
    private String extractedTextObjectKey;

    /** 手工笔记索引任务对应的笔记版本。 */
    private Integer noteVersion;

    /** 失败原因（status=FAILED 时填充） */
    private String errorMsg;

    /** 当前消息对应的解析尝试次数，从 1 开始 */
    private Integer attemptCount;

    /** 是否已经达到重试上限并进入最终失败 */
    private Boolean finalFailed;
}
