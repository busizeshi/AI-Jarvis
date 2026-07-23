package com.notegather.biz.application.service;

import cn.dev33.satoken.stp.StpUtil;
import com.notegather.biz.domain.identity.valueobject.UserId;
import com.notegather.biz.domain.knowledge.aggregate.Document;
import com.notegather.biz.domain.knowledge.repository.DocumentRepository;
import com.notegather.biz.domain.knowledge.valueobject.DocumentId;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 文档文件上传服务
 */
@Service
@RequiredArgsConstructor
public class DocumentFileService {
    
    private final DocumentRepository documentRepository;
    private final MinioClient minioClient;
    
    @Value("${minio.endpoint}")
    private String minioEndpoint;
    
    @Value("${minio.bucket-name}")
    private String bucketName;
    
    /**
     * 允许上传的文件类型
     */
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
        "text/plain",                                                      // txt
        "text/markdown",                                                   // md
        "application/pdf",                                                 // pdf
        "application/msword",                                              // doc
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",  // docx
        "application/vnd.ms-excel",                                        // xls
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",        // xlsx
        "application/vnd.ms-powerpoint",                                   // ppt
        "application/vnd.openxmlformats-officedocument.presentationml.presentation" // pptx
    );
    
    /**
     * 允许上传的文件扩展名
     */
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
        ".txt", ".md", ".pdf", ".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx"
    );
    
    /**
     * 最大文件大小：50MB
     */
    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024;
    
    /**
     * 上传文档文件
     */
    @Transactional(rollbackFor = Exception.class)
    public String uploadDocumentFile(Long documentId, MultipartFile file) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        UserId userId = UserId.of(currentUserId);
        
        // 1. 校验文档权限
        Document document = documentRepository.findById(DocumentId.of(documentId))
                .orElseThrow(() -> new RuntimeException("文档不存在"));
        
        if (!document.isOwnedBy(userId)) {
            throw new RuntimeException("无权限操作该文档");
        }
        
        if (document.isFolder()) {
            throw new RuntimeException("文件夹不能上传文件");
        }
        
        // 2. 校验文件
        validateFile(file);
        
        // 3. 上传到 MinIO
        String fileUrl = uploadToMinio(document, file);
        
        // 4. 更新文档信息
        document.setFileInfo(file.getOriginalFilename(), file.getSize(), fileUrl);
        documentRepository.save(document);
        
        return fileUrl;
    }
    
    /**
     * 校验文件
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }
        
        // 校验文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException("文件大小不能超过50MB");
        }
        
        // 校验文件名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new RuntimeException("文件名不能为空");
        }
        
        // 校验文件扩展名
        String extension = getFileExtension(originalFilename);
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new RuntimeException("不支持的文件类型，仅支持: " + String.join(", ", ALLOWED_EXTENSIONS));
        }
        
        // 校验 Content-Type
        String contentType = file.getContentType();
        if (contentType != null && !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            // 某些浏览器可能发送 application/octet-stream，这里只做警告
            // 主要依赖文件扩展名校验
        }
    }
    
    /**
     * 上传文件到 MinIO
     */
    private String uploadToMinio(Document document, MultipartFile file) {
        try {
            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);
            
            // MinIO 路径：documents/{kbId}/{docId}/{uuid}{extension}
            String objectKey = String.format(
                "documents/%d/%d/%s%s",
                document.getKnowledgeBaseId().getValue(),
                document.getId().getValue(),
                UUID.randomUUID().toString(),
                extension
            );
            
            // 上传到 MinIO
            try (InputStream inputStream = file.getInputStream()) {
                minioClient.putObject(
                    PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectKey)
                        .stream(inputStream, file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build()
                );
            }
            
            // 生成访问 URL
            return minioEndpoint + "/" + bucketName + "/" + objectKey;
            
        } catch (Exception e) {
            throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex);
    }
}
