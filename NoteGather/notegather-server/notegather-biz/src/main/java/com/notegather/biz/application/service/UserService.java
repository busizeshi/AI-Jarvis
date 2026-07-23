package com.notegather.biz.application.service;

import com.notegather.biz.application.command.UpdateProfileCommand;
import com.notegather.biz.domain.identity.aggregate.User;
import com.notegather.biz.domain.identity.repository.UserRepository;
import com.notegather.biz.domain.identity.valueobject.UserId;
import com.notegather.common.core.exception.BusinessException;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

/**
 * 用户应用服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final MinioClient minioClient;
    
    @Value("${minio.bucket-name:notegather}")
    private String bucketName;
    
    @Value("${minio.endpoint:http://192.168.1.12:9000}")
    private String minioEndpoint;
    
    /**
     * 根据ID查询用户
     */
    public User getUserById(Long userId) {
        return userRepository.findById(UserId.of(userId))
            .orElseThrow(() -> new BusinessException("用户不存在"));
    }
    
    /**
     * 更新用户资料
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateProfile(Long userId, UpdateProfileCommand command) {
        User user = getUserById(userId);
        user.updateProfile(command.getDisplayName(), command.getAvatarUrl());
        userRepository.save(user);
    }
    
    /**
     * 上传用户头像
     */
    public String uploadAvatar(Long userId, MultipartFile file) {
        try {
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new BusinessException("只支持上传图片文件");
            }
            
            if (file.getSize() > 50 * 1024 * 1024) {
                throw new BusinessException("头像文件不能超过50MB");
            }
            
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                : ".jpg";
            String objectKey = "avatars/" + userId + "/" + UUID.randomUUID() + extension;
            
            try (InputStream inputStream = file.getInputStream()) {
                minioClient.putObject(
                    PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectKey)
                        .stream(inputStream, file.getSize(), -1)
                        .contentType(contentType)
                        .build()
                );
            }
            
            String avatarUrl = minioEndpoint + "/" + bucketName + "/" + objectKey;
            
            User user = getUserById(userId);
            user.updateProfile(null, avatarUrl);
            userRepository.save(user);
            
            return avatarUrl;
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("上传头像失败", e);
            throw new BusinessException("上传头像失败: " + e.getMessage());
        }
    }
}
