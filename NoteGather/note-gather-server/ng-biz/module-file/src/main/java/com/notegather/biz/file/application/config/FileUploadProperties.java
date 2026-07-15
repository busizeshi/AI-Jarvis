package com.notegather.biz.file.application.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "ng.file")
public class FileUploadProperties {

    private long maxSizeBytes;
}
