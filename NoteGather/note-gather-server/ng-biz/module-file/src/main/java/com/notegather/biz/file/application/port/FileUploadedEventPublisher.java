package com.notegather.biz.file.application.port;

import com.notegather.common.mq.dto.FileUploadedMessage;

public interface FileUploadedEventPublisher {

    void publish(FileUploadedMessage message);
}
