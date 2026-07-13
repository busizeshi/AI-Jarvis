package com.notegather.common.redis.service;

import cn.hutool.core.collection.CollUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis 通用操作封装（基于 StringRedisTemplate，value 统一用 JSON 字符串）
 * 复杂对象序列化交给调用方（建议用 hutool JSON 或 Jackson）
 */
@Component
@RequiredArgsConstructor
public class RedisService {

    private final StringRedisTemplate redisTemplate;

    // ==================== String ====================

    public void set(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }

    public void set(String key, String value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    /** 设置并返回旧值 */
    public String getAndSet(String key, String value) {
        return redisTemplate.opsForValue().getAndSet(key, value);
    }

    public String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /** key 不存在时才设置，返回是否设置成功（实现分布式幂等） */
    public Boolean setIfAbsent(String key, String value, long timeout, TimeUnit unit) {
        return redisTemplate.opsForValue().setIfAbsent(key, value, timeout, unit);
    }

    // ==================== 过期 / 删除 ====================

    public Boolean expire(String key, long timeout, TimeUnit unit) {
        return redisTemplate.expire(key, timeout, unit);
    }

    public Long getExpire(String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    public Long deleteKeys(Collection<String> keys) {
        if (CollUtil.isEmpty(keys)) {
            return 0L;
        }
        return redisTemplate.delete(keys);
    }

    // ==================== 计数器 ====================

    public Long increment(String key) {
        return redisTemplate.opsForValue().increment(key);
    }

    public Long incrementBy(String key, long delta) {
        return redisTemplate.opsForValue().increment(key, delta);
    }

    // ==================== Hash ====================

    public void hSet(String key, String field, String value) {
        redisTemplate.opsForHash().put(key, field, value);
    }

    public Object hGet(String key, String field) {
        return redisTemplate.opsForHash().get(key, field);
    }

    public Map<Object, Object> hGetAll(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    public Boolean hDelete(String key, Object... fields) {
        return redisTemplate.opsForHash().delete(key, fields) > 0;
    }

    // ==================== Set ====================

    public Long sAdd(String key, String... values) {
        return redisTemplate.opsForSet().add(key, values);
    }

    public Boolean sIsMember(String key, String value) {
        return redisTemplate.opsForSet().isMember(key, value);
    }

    public Set<String> sMembers(String key) {
        return redisTemplate.opsForSet().members(key);
    }

    // ==================== List ====================

    public Long lPush(String key, String value) {
        return redisTemplate.opsForList().leftPush(key, value);
    }

    public String rPop(String key) {
        return redisTemplate.opsForList().rightPop(key);
    }

    public List<String> lRange(String key, long start, long end) {
        return redisTemplate.opsForList().range(key, start, end);
    }
}
