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

    /** 解析状态：DONE / FAILED */
    private String status;

    /** 成功入库的分块数量 */
    private Integer chunkCount;

    /** 失败原因（status=FAILED 时填充） */
    private String errorMsg;

    /** 当前消息对应的解析尝试次数，从 1 开始 */
    private Integer attemptCount;

    /** 是否已经达到重试上限并进入最终失败 */
    private Boolean finalFailed;
}
