package com.notegather.biz.file.application.port;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorage {

    String bucket();

    void store(String bucket, String objectKey, MultipartFile file);

    void deleteQuietly(String bucket, String objectKey);
}
