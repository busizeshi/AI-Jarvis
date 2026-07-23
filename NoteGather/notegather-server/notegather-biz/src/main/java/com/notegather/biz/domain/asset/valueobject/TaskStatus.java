package com.notegather.biz.domain.asset.valueobject;

/**
 * 任务状态枚举
 */
public enum TaskStatus {
    /**
     * 待处理：任务已创建，等待执行
     */
    PENDING(0, "待处理"),
    
    /**
     * 处理中：任务正在执行
     */
    PROCESSING(1, "处理中"),
    
    /**
     * 已完成：任务执行成功
     */
    COMPLETED(2, "已完成"),
    
    /**
     * 失败：任务执行失败
     */
    FAILED(3, "失败");
    
    private final int code;
    private final String description;
    
    TaskStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public int getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    public static TaskStatus fromCode(int code) {
        for (TaskStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown task status code: " + code);
    }
}
