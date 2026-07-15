package com.notegather.biz.file.adapter.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.notegather.biz.file.application.service.FileUploadService;
import com.notegather.common.mq.constant.MqTopicConstants;
import com.notegather.common.mq.dto.ParseDoneMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(
        topic = MqTopicConstants.TOPIC_FILE,
        selectorExpression = MqTopicConstants.TAG_PARSE_DONE + " || " + MqTopicConstants.TAG_PARSE_FAILED,
        consumerGroup = MqTopicConstants.GROUP_BIZ_FILE
)
public class ParseResultConsumer implements RocketMQListener<String> {

    private final ObjectMapper objectMapper;
    private final FileUploadService fileUploadService;

    @Override
    public void onMessage(String payload) {
        try {
            ParseDoneMessage message = objectMapper.readValue(payload, ParseDoneMessage.class);
            fileUploadService.handleParseResult(message);
        } catch (JsonProcessingException exception) {
            log.error("忽略无法解析的解析结果消息 payload={}", payload, exception);
        }
    }
}
