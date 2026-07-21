package com.notegather.admin.application.user.service;

import cn.hutool.core.util.StrUtil;
import com.notegather.admin.application.user.assembler.UserAssembler;
import com.notegather.admin.application.user.dto.LoginRequest;
import com.notegather.admin.application.user.dto.LogoutRequest;
import com.notegather.admin.application.user.dto.RefreshTokenRequest;
import com.notegather.admin.application.user.dto.RegisterRequest;
import com.notegather.admin.application.user.dto.TokenResponse;
import com.notegather.admin.application.user.dto.UpdateProfileRequest;
import com.notegather.admin.application.user.dto.UserResponse;
import com.notegather.admin.application.user.security.PasswordEnvelopeService;
import com.notegather.admin.domain.user.model.User;
import com.notegather.admin.domain.user.repository.UserRepository;
import com.notegather.admin.application.user.service.UserService;
import com.notegather.admin.infrastructure.avatar.AvatarStorageService;
import org.springframework.web.multipart.MultipartFile;
import com.notegather.common.core.constant.CommonConstants;
import com.notegather.common.core.exception.BusinessException;
import com.notegather.common.core.result.ResultCode;
import com.notegather.common.redis.lock.DistributedLock;
import com.notegather.common.redis.session.AuthSessionStore;
import com.notegather.common.security.config.JwtProperties;
import com.notegather.common.security.context.UserContext;
import com.notegather.common.security.model.LoginUser;
import com.notegather.common.security.model.TokenPayload;
import com.notegather.common.security.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private static final int STATUS_DISABLED = 0;
    private static final int STATUS_NORMAL = 1;
    private static final String TOKEN_TYPE = "Bearer";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final JwtProperties jwtProperties;
    private final AuthSessionStore authSessionStore;
    private final DistributedLock distributedLock;
    private final AvatarStorageService avatarStorageService;
    private final PasswordEnvelopeService passwordEnvelopeService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserResponse register(RegisterRequest request) {
        String username = normalizeUsername(request.getUsername());
        ensureUsernameAvailable(username);

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(decryptAndValidatePassword(request.getPasswordEnvelope())));
        user.setNickname(StrUtil.blankToDefault(StrUtil.trim(request.getNickname()), username));
        user.setStatus(STATUS_NORMAL);
        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ResultCode.USER_ALREADY_EXISTS);
        }
        return UserAssembler.toResponse(user);
    }

    @Override
    public TokenResponse login(LoginRequest request) {
        String username = normalizeUsername(request.getUsername());
        User user = findActiveByUsername(username);
        String password = decryptAndValidatePassword(request.getPasswordEnvelope());
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }
        ensureEnabled(user);
        return issueTokens(user);
    }

    @Override
    public TokenResponse refreshToken(RefreshTokenRequest request) {
        TokenPayload oldPayload = parseRefreshToken(request.getRefreshToken());
        TokenResponse response = distributedLock.tryLock(
                CommonConstants.REDIS_REFRESH_LOCK_PREFIX + oldPayload.getJti(),
                1,
                5,
                () -> rotateRefreshToken(oldPayload)
        );
        if (response == null) {
            throw new BusinessException(ResultCode.REFRESH_TOKEN_INVALID);
        }
        return response;
    }

    @Override
    public void logout(LogoutRequest request) {
        LoginUser loginUser = UserContext.requireLoginUser();
        authSessionStore.revokeAccessSession(loginUser.getAccessTokenJti());
        blacklist(loginUser.getAccessTokenJti(), loginUser.getAccessTokenExpiresAt());
        if (request != null && StrUtil.isNotBlank(request.getRefreshToken())) {
            TokenPayload refreshPayload = parseRefreshToken(request.getRefreshToken());
            if (Objects.equals(refreshPayload.getUserId(), loginUser.getUserId())) {
                deleteRefreshSession(refreshPayload.getJti());
                blacklist(refreshPayload.getJti(), refreshPayload.getExpiresAt().toEpochMilli());
            }
        }
    }

    @Override
    public UserResponse getCurrentUser() {
        return getById(UserContext.getUserId());
    }

    @Override
    public UserResponse getById(Long userId) {
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        return UserAssembler.toResponse(user);
    }

    @Override
    public UserResponse getByUsername(String username) {
        return UserAssembler.toResponse(findActiveByUsername(normalizeUsername(username)));
    }

    @Override
    public List<UserResponse> listByIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyList();
        }
        return userRepository.findByIds(userIds).stream()
                .map(UserAssembler::toResponse)
                .toList();
    }

    @Override
    public boolean updateStatus(Long userId, Integer status) {
        if (!Objects.equals(status, STATUS_NORMAL) && !Objects.equals(status, STATUS_DISABLED)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "用户状态只能是 0 或 1");
        }
        return userRepository.updateStatus(userId, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserResponse uploadAvatar(Long userId, MultipartFile file, String oldAvatarUrl) {
        // 1. 校验文件
        avatarStorageService.validate(file);

        // 2. 查询更新前用户信息用于日志对比
        User before = userRepository.findById(userId);
        if (before == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        log.info("更新用户头像 userId={} before=[avatarUrl={}] fileName={} size={} contentType={}",
                userId, before.getAvatarUrl(), file.getOriginalFilename(), file.getSize(), file.getContentType());

        // 3. 上传新头像到 MinIO，获取新 URL
        String newAvatarUrl = avatarStorageService.upload(userId, file);

        // 4. 更新 DB（事务内）
        boolean updated = userRepository.updateAvatarUrl(userId, newAvatarUrl);
        if (!updated) {
            // 上传成功但 DB 更新失败，回滚 MinIO（事务回滚钩子无法覆盖外部存储，手动清理）
            avatarStorageService.deleteQuietly(newAvatarUrl);
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "头像信息保存失败，请重试");
        }
        log.info("头像更新成功 userId={} before=[avatarUrl={}] after=[avatarUrl={}]",
                userId, before.getAvatarUrl(), newAvatarUrl);

        // 5. 事务提交后异步删除旧头像（DB 已完成，此时静默失败不影响主流程）
        if (oldAvatarUrl != null && !oldAvatarUrl.isBlank()) {
            log.info("删除旧头像 userId={} oldAvatarUrl={}", userId, oldAvatarUrl);
            avatarStorageService.deleteQuietly(oldAvatarUrl);
        }

        return getById(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {
        String nickname = StrUtil.trim(request.getNickname());
        if (StrUtil.isBlank(nickname)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "昵称不能为空");
        }
        String bio = StrUtil.nullToEmpty(request.getBio());

        // 查询更新前的用户信息用于日志对比
        User before = userRepository.findById(userId);
        if (before == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        log.info("更新用户资料 userId={} before=[nickname={},bio={}] after=[nickname={},bio={}]",
                userId, before.getNickname(), before.getBio(), nickname, bio);

        boolean updated = userRepository.updateProfile(userId, nickname, bio);
        if (!updated) {
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "用户资料更新失败，请重试");
        }
        return getById(userId);
    }

    private TokenResponse rotateRefreshToken(TokenPayload oldPayload) {
        String sessionUserId = authSessionStore.getRefreshSessionUserId(oldPayload.getJti());
        if (!Objects.equals(sessionUserId, oldPayload.getUserId())) {
            throw new BusinessException(ResultCode.REFRESH_TOKEN_INVALID);
        }
        User user = userRepository.findById(Long.valueOf(oldPayload.getUserId()));
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        ensureEnabled(user);
        authSessionStore.revokeRefreshSession(oldPayload.getJti());
        safeSetBlacklist(oldPayload.getJti(), oldPayload.remainingMillis());
        return issueTokens(user);
    }

    private TokenResponse issueTokens(User user) {
        LoginUser loginUser = LoginUser.builder()
                .userId(String.valueOf(user.getId()))
                .username(user.getUsername())
                .nickname(user.getNickname())
                .build();
        String accessToken = jwtUtils.generateAccessToken(loginUser);
        String refreshToken = jwtUtils.generateRefreshToken(loginUser);
        TokenPayload accessPayload = jwtUtils.parseAccessToken(accessToken);
        TokenPayload refreshPayload = jwtUtils.parseRefreshToken(refreshToken);
        authSessionStore.saveAccessSession(loginUser.getUserId(), accessPayload.getJti(), accessPayload.remainingMillis());
        authSessionStore.saveRefreshSession(loginUser.getUserId(), refreshPayload.getJti(), refreshPayload.remainingMillis());
        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType(TOKEN_TYPE)
                .accessTokenExpiresIn(jwtProperties.getAccessTokenExpireMs() / 1000)
                .refreshTokenExpiresIn(jwtProperties.getRefreshTokenExpireMs() / 1000)
                .user(UserAssembler.toResponse(user))
                .build();
    }

    private void ensureUsernameAvailable(String username) {
        if (findActiveByUsername(username) != null) {
            throw new BusinessException(ResultCode.USER_ALREADY_EXISTS);
        }
    }

    private User findActiveByUsername(String username) {
        if (StrUtil.isBlank(username)) {
            return null;
        }
        return userRepository.findByUsername(username);
    }

    private void ensureEnabled(User user) {
        if (!Objects.equals(user.getStatus(), STATUS_NORMAL)) {
            throw new BusinessException(ResultCode.ACCOUNT_DISABLED);
        }
    }

    private TokenPayload parseRefreshToken(String token) {
        try {
            return jwtUtils.parseRefreshToken(token);
        } catch (BusinessException e) {
            throw new BusinessException(ResultCode.REFRESH_TOKEN_INVALID);
        }
    }

    private String normalizeUsername(String username) {
        return StrUtil.trim(username);
    }

    private String decryptAndValidatePassword(com.notegather.admin.application.user.dto.PasswordEnvelopeRequest envelope) {
        String password = passwordEnvelopeService.decrypt(envelope);
        if (StrUtil.isBlank(password) || password.length() < 8 || password.length() > 128) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "Password length must be between 8 and 128");
        }
        return password;
    }

    private void deleteRefreshSession(String jti) {
        authSessionStore.revokeRefreshSession(jti);
    }

    private void blacklist(String jti, Long expiresAt) {
        if (StrUtil.isBlank(jti) || expiresAt == null) {
            return;
        }
        safeSetBlacklist(jti, Math.max(0L, expiresAt - System.currentTimeMillis()));
    }

    private void safeSetBlacklist(String jti, long ttlMillis) {
        if (ttlMillis <= 0) {
            return;
        }
        authSessionStore.blacklist(jti, ttlMillis);
    }
}
