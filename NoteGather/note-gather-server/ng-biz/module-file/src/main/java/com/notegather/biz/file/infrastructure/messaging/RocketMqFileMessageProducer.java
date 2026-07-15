package com.notegather.biz.file.infrastructure.messaging;

import com.notegather.biz.file.application.port.FileUploadedEventPublisher;
import com.notegather.common.mq.constant.MqTopicConstants;
import com.notegather.common.mq.dto.FileUploadedMessage;
import lombok.RequiredArgsConstructor;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RocketMqFileMessageProducer implements FileUploadedEventPublisher {

    private final RocketMQTemplate rocketMQTemplate;

    @Override
    public void publish(FileUploadedMessage message) {
        SendResult sendResult = rocketMQTemplate.syncSend(
                MqTopicConstants.TOPIC_FILE + ":" + MqTopicConstants.TAG_FILE_UPLOADED,
                message
        );
        if (sendResult == null || sendResult.getSendStatus() != SendStatus.SEND_OK) {
            throw new IllegalStateException("文件上传消息未成功投递");
        }
    }
}
