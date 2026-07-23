package com.notegather.biz.domain.asset.valueobject;

/**
 * 任务类型枚举
 */
public enum TaskType {
    /**
     * 文档解析：解析文档内容、提取文本、生成向量
     */
    DOCUMENT_PARSE(0, "文档解析"),
    
    /**
     * 向量索引：构建向量索引
     */
    VECTOR_INDEX(1, "向量索引"),
    
    /**
     * 知识图谱：构建知识图谱
     */
    KNOWLEDGE_GRAPH(2, "知识图谱");
    
    private final int code;
    private final String description;
    
    TaskType(int code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public int getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    public static TaskType fromCode(int code) {
        for (TaskType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown task type code: " + code);
    }
}
