package com.notegather.biz.file.infrastructure.messaging;

import com.notegather.biz.file.application.port.FileUploadedEventPublisher;
import com.notegather.common.core.exception.BusinessException;
import com.notegather.common.core.result.ResultCode;
import com.notegather.common.mq.constant.MqTopicConstants;
import com.notegather.common.mq.dto.FileUploadedMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RocketMqFileMessageProducer implements FileUploadedEventPublisher {

    private final RocketMQTemplate rocketMQTemplate;

    @Override
    public void publish(FileUploadedMessage message) {
        try {
            SendResult result = rocketMQTemplate.syncSend(
                    MqTopicConstants.TOPIC_FILE + ":" + MqTopicConstants.TAG_FILE_UPLOADED, message);
            if (result == null || result.getSendStatus() != SendStatus.SEND_OK) {
                throw new IllegalStateException("File upload message was not accepted");
            }
        } catch (RuntimeException exception) {
            log.error("File parse publication failed fileId={} noteId={} userId={}",
                    message.getFileId(), message.getNoteId(), message.getUserId(), exception);
            throw new BusinessException(ResultCode.SERVICE_UNAVAILABLE, "文件解析服务暂不可用，请稍后重试");
        }
    }
}
