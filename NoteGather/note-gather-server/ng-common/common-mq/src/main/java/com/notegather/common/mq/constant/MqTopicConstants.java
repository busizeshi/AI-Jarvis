package com.notegather.common.mq.constant;

/**
 * RocketMQ Topic / Tag 常量
 */
public final class MqTopicConstants {

    private MqTopicConstants() {}

    // ==================== Topic ====================
    /** 文件相关消息 */
    public static final String TOPIC_FILE = "NG_FILE_TOPIC";
    /** 知识图谱构建消息 */
    public static final String TOPIC_GRAPH = "NG_GRAPH_TOPIC";
    /** 记忆卡 AI 生成消息 */
    public static final String TOPIC_FLASHCARD = "NG_FLASHCARD_TOPIC";
    /** Agent 工作流运行请求 */
    public static final String TOPIC_WORKFLOW = "NG_WORKFLOW_TOPIC";

    // ==================== Tag ====================
    /** 文件已上传，待 Python 解析 */
    public static final String TAG_FILE_UPLOADED = "FILE_UPLOADED";
    /** Python 解析完成，通知 Java 更新状态 */
    public static final String TAG_PARSE_DONE = "PARSE_DONE";
    /** Python 解析失败 */
    public static final String TAG_PARSE_FAILED = "PARSE_FAILED";
    /** 笔记正文保存后请求 Python 重建检索索引 */
    public static final String TAG_NOTE_CONTENT_UPDATED = "NOTE_CONTENT_UPDATED";
    /** 笔记正文清空或删除后移除检索索引 */
    public static final String TAG_NOTE_CONTENT_DELETED = "NOTE_CONTENT_DELETED";
    /** 笔记正文检索索引更新完成 */
    public static final String TAG_NOTE_CONTENT_INDEX_DONE = "NOTE_CONTENT_INDEX_DONE";
    /** 笔记正文检索索引更新失败 */
    public static final String TAG_NOTE_CONTENT_INDEX_FAILED = "NOTE_CONTENT_INDEX_FAILED";

    /** 请求 Python 构建或重建图谱 */
    public static final String TAG_GRAPH_BUILD_REQUESTED = "GRAPH_BUILD_REQUESTED";
    /** Python 图谱构建完成 */
    public static final String TAG_GRAPH_BUILD_DONE = "GRAPH_BUILD_DONE";
    /** Python 图谱构建最终失败 */
    public static final String TAG_GRAPH_BUILD_FAILED = "GRAPH_BUILD_FAILED";
    /** 请求删除笔记对应图谱投影 */
    public static final String TAG_GRAPH_NOTE_DELETED = "GRAPH_NOTE_DELETED";
    /** 请求 Python 为笔记生成候选卡 */
    public static final String TAG_FLASHCARD_GENERATION_REQUESTED = "FLASHCARD_GENERATION_REQUESTED";
    /** Python 候选卡生成完成 */
    public static final String TAG_FLASHCARD_GENERATION_DONE = "FLASHCARD_GENERATION_DONE";
    /** Python 候选卡生成最终失败 */
    public static final String TAG_FLASHCARD_GENERATION_FAILED = "FLASHCARD_GENERATION_FAILED";
    /** 笔记删除，异步失活关联卡片 */
    public static final String TAG_FLASHCARD_NOTE_DELETED = "FLASHCARD_NOTE_DELETED";
    /** 请求 agent_service 执行或恢复工作流运行 */
    public static final String TAG_WORKFLOW_RUN_REQUESTED = "WORKFLOW_RUN_REQUESTED";

    // ==================== Consumer Group ====================
    /** Python 解析服务消费组 */
    public static final String GROUP_AI_PARSE = "ng-ai-parse-group";
    /** biz 服务消费 PARSE_DONE 的消费组 */
    public static final String GROUP_BIZ_FILE = "ng-biz-file-group";
    /** biz 服务消费手工笔记索引结果 */
    public static final String GROUP_BIZ_NOTE_INDEX = "ng-biz-note-index-group";
    /** biz 服务消费图谱构建结果 */
    public static final String GROUP_BIZ_GRAPH = "ng-biz-graph-group";
    /** Python 图谱构建服务消费组 */
    public static final String GROUP_AI_GRAPH = "ng-ai-graph-group";
    /** Python 记忆卡生成服务消费组 */
    public static final String GROUP_AI_FLASHCARD = "ng-ai-flashcard-group";
    /** biz 服务消费记忆卡生成结果及笔记删除事件 */
    public static final String GROUP_BIZ_FLASHCARD = "ng-biz-flashcard-group";
    /** biz 服务消费笔记删除后的记忆卡失活事件 */
    public static final String GROUP_BIZ_FLASHCARD_CLEANUP = "ng-biz-flashcard-cleanup-group";
    /** Python Agent 工作流执行服务消费组 */
    public static final String GROUP_AI_WORKFLOW = "ng-ai-workflow-group";
}
