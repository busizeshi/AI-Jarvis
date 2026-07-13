package com.notegather.common.redis.lock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 分布式锁工具（基于 Redisson）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DistributedLock {

    private final RedissonClient redissonClient;

    private static final String LOCK_PREFIX = "ng:lock:";

    /**
     * 尝试加锁并执行任务，执行完自动释放
     *
     * @param lockKey   锁名称（不含前缀）
     * @param waitSec   等待加锁超时（秒）
     * @param leaseSec  锁持有最长时间（秒），-1 表示 watchdog 自动续期
     * @param task      需在锁保护下执行的任务（有返回值）
     * @return 任务返回值；加锁失败返回 null
     */
    public <T> T tryLock(String lockKey, long waitSec, long leaseSec, Supplier<T> task) {
        RLock lock = redissonClient.getLock(LOCK_PREFIX + lockKey);
        boolean acquired = false;
        try {
            acquired = lock.tryLock(waitSec, leaseSec, TimeUnit.SECONDS);
            if (!acquired) {
                log.warn("[DistributedLock] 加锁失败，key={}", lockKey);
                return null;
            }
            return task.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[DistributedLock] 等待锁被中断，key={}", lockKey, e);
            return null;
        } finally {
            if (acquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 无返回值版本
     */
    public void tryLock(String lockKey, long waitSec, long leaseSec, Runnable task) {
        tryLock(lockKey, waitSec, leaseSec, () -> {
            task.run();
            return null;
        });
    }
}
