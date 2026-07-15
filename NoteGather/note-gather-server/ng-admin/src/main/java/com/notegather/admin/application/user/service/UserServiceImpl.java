package com.notegather.admin.application.user.service;

import cn.hutool.core.util.StrUtil;
import com.notegather.admin.application.user.assembler.UserAssembler;
import com.notegather.admin.application.user.dto.LoginRequest;
import com.notegather.admin.application.user.dto.LogoutRequest;
import com.notegather.admin.application.user.dto.RefreshTokenRequest;
import com.notegather.admin.application.user.dto.RegisterRequest;
import com.notegather.admin.application.user.dto.TokenResponse;
import com.notegather.admin.application.user.dto.UserResponse;
import com.notegather.admin.domain.user.model.User;
import com.notegather.admin.domain.user.repository.UserRepository;
import com.notegather.admin.application.user.service.UserService;
import com.notegather.common.core.constant.CommonConstants;
import com.notegather.common.core.exception.BusinessException;
import com.notegather.common.core.result.ResultCode;
import com.notegather.common.redis.lock.DistributedLock;
import com.notegather.common.redis.service.RedisService;
import com.notegather.common.security.config.JwtProperties;
import com.notegather.common.security.context.UserContext;
import com.notegather.common.security.model.LoginUser;
import com.notegather.common.security.model.TokenPayload;
import com.notegather.common.security.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final int STATUS_DISABLED = 0;
    private static final int STATUS_NORMAL = 1;
    private static final String TOKEN_TYPE = "Bearer";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final JwtProperties jwtProperties;
    private final RedisService redisService;
    private final DistributedLock distributedLock;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserResponse register(RegisterRequest request) {
        String username = normalizeUsername(request.getUsername());
        ensureUsernameAvailable(username);

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
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
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
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

    private TokenResponse rotateRefreshToken(TokenPayload oldPayload) {
        String sessionKey = refreshKey(oldPayload.getJti());
        String sessionUserId = safeRedisGet(sessionKey);
        if (!Objects.equals(sessionUserId, oldPayload.getUserId())) {
            throw new BusinessException(ResultCode.REFRESH_TOKEN_INVALID);
        }
        User user = userRepository.findById(Long.valueOf(oldPayload.getUserId()));
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        ensureEnabled(user);
        safeRedisDelete(sessionKey);
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
        TokenPayload refreshPayload = jwtUtils.parseRefreshToken(refreshToken);
        safeRedisSet(refreshKey(refreshPayload.getJti()), loginUser.getUserId(), refreshPayload.remainingMillis());
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

    private String refreshKey(String jti) {
        return CommonConstants.REDIS_REFRESH_TOKEN_PREFIX + jti;
    }

    private String blacklistKey(String jti) {
        return CommonConstants.REDIS_TOKEN_BLACKLIST_PREFIX + jti;
    }

    private void deleteRefreshSession(String jti) {
        safeRedisDelete(refreshKey(jti));
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
        safeRedisSet(blacklistKey(jti), "1", ttlMillis);
    }

    private void safeRedisSet(String key, String value, long ttlMillis) {
        try {
            redisService.set(key, value, Math.max(1L, ttlMillis / 1000), TimeUnit.SECONDS);
        } catch (DataAccessException e) {
            throw new BusinessException(ResultCode.SERVICE_UNAVAILABLE, "认证状态服务不可用");
        }
    }

    private String safeRedisGet(String key) {
        try {
            return redisService.get(key);
        } catch (DataAccessException e) {
            throw new BusinessException(ResultCode.SERVICE_UNAVAILABLE, "认证状态服务不可用");
        }
    }

    private void safeRedisDelete(String key) {
        try {
            redisService.delete(key);
        } catch (DataAccessException e) {
            throw new BusinessException(ResultCode.SERVICE_UNAVAILABLE, "认证状态服务不可用");
        }
    }
}
