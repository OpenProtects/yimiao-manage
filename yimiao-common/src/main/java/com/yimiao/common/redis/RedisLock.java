package com.yimiao.common.redis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class RedisLock {

    private static final String LOCK_PREFIX = "lock:";
    private static final String UNLOCK_SCRIPT = 
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
            "    return redis.call('del', KEYS[1]) " +
            "else " +
            "    return 0 " +
            "end";

    private final StringRedisTemplate redisTemplate;

    public RedisLock(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean tryLock(String key, String value, long expireTime, TimeUnit timeUnit) {
        String lockKey = LOCK_PREFIX + key;
        try {
            Boolean result = redisTemplate.opsForValue()
                    .setIfAbsent(lockKey, value, expireTime, timeUnit);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.error("获取分布式锁失败: key={}", lockKey, e);
            return false;
        }
    }

    public boolean tryLock(String key, String value, long expireTime, TimeUnit timeUnit, long waitTime) {
        long startTime = System.currentTimeMillis();
        while (true) {
            if (tryLock(key, value, expireTime, timeUnit)) {
                return true;
            }
            if (System.currentTimeMillis() - startTime >= waitTime) {
                return false;
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
    }

    public boolean unlock(String key, String value) {
        String lockKey = LOCK_PREFIX + key;
        try {
            DefaultRedisScript<Long> script = new DefaultRedisScript<>(UNLOCK_SCRIPT, Long.class);
            Long result = redisTemplate.execute(script, Collections.singletonList(lockKey), value);
            return result != null && result == 1L;
        } catch (Exception e) {
            log.error("释放分布式锁失败: key={}", lockKey, e);
            return false;
        }
    }

    public boolean isLocked(String key) {
        String lockKey = LOCK_PREFIX + key;
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(lockKey));
        } catch (Exception e) {
            log.error("检查锁状态失败: key={}", lockKey, e);
            return false;
        }
    }
}
