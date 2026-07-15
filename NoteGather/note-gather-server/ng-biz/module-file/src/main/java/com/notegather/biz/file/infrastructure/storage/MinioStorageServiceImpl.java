package com.notegather.biz.file.infrastructure.storage;

import com.notegather.biz.file.application.port.FileStorage;
import com.notegather.biz.file.infrastructure.config.MinioProperties;
import com.notegather.common.core.exception.BusinessException;
import com.notegather.common.core.result.ResultCode;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@Service
@Slf4j
@RequiredArgsConstructor
public class MinioStorageServiceImpl implements FileStorage {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    @Override
    public String bucket() {
        return minioProperties.getBucket();
    }

    @Override
    public void store(String bucket, String objectKey, MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            ensureBucket(bucket);
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectKey)
                    .stream(inputStream, file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());
        } catch (Exception exception) {
            throw new BusinessException(ResultCode.FILE_UPLOAD_FAIL, "文件写入对象存储失败");
        }
    }

    @Override
    public void deleteQuietly(String bucket, String objectKey) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucket).object(objectKey).build());
        } catch (Exception exception) {
            log.warn("MinIO 孤儿对象清理失败 bucket={} objectKey={}", bucket, objectKey, exception);
        }
    }

    private void ensureBucket(String bucket) throws Exception {
        if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
        }
    }
}
