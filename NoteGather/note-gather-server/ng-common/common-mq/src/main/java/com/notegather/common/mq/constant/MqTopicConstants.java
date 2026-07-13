package com.notegather.common.mq.constant;

/**
 * RocketMQ Topic / Tag 常量
 */
public final class MqTopicConstants {

    private MqTopicConstants() {}

    // ==================== Topic ====================
    /** 文件相关消息 */
    public static final String TOPIC_FILE = "NG_FILE_TOPIC";

    // ==================== Tag ====================
    /** 文件已上传，待 Python 解析 */
    public static final String TAG_FILE_UPLOADED = "FILE_UPLOADED";
    /** Python 解析完成，通知 Java 更新状态 */
    public static final String TAG_PARSE_DONE = "PARSE_DONE";
    /** Python 解析失败 */
    public static final String TAG_PARSE_FAILED = "PARSE_FAILED";

    // ==================== Consumer Group ====================
    /** Python 解析服务消费组 */
    public static final String GROUP_AI_PARSE = "ng-ai-parse-group";
    /** biz 服务消费 PARSE_DONE 的消费组 */
    public static final String GROUP_BIZ_FILE = "ng-biz-file-group";
}
