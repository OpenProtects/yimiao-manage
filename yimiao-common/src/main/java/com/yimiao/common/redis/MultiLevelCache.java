package com.yimiao.common.redis;

import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Slf4j
@Component
public class MultiLevelCache {

    private static final long LOCAL_CACHE_EXPIRE = 60 * 1000L;
    private final Map<String, CacheEntry> localCache = new ConcurrentHashMap<>();
    private final StringRedisTemplate redisTemplate;

    public MultiLevelCache(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        Thread cleanupThread = new Thread(this::cleanupLocalCache);
        cleanupThread.setDaemon(true);
        cleanupThread.start();
    }

    public String get(String key) {
        CacheEntry entry = localCache.get(key);
        if (entry != null && !entry.isExpired()) {
            log.debug("本地缓存命中: key={}", key);
            return entry.getValue();
        }

        try {
            String value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                localCache.put(key, new CacheEntry(value, System.currentTimeMillis() + LOCAL_CACHE_EXPIRE));
                log.debug("Redis缓存命中: key={}", key);
            }
            return value;
        } catch (Exception e) {
            log.error("获取缓存失败: key={}", key, e);
            if (entry != null) {
                return entry.getValue();
            }
            return null;
        }
    }

    public <T> T get(String key, Class<T> clazz) {
        String value = get(key);
        if (value == null) {
            return null;
        }
        return JSON.parseObject(value, clazz);
    }

    public <T> T get(String key, Class<T> clazz, Supplier<T> loader) {
        return get(key, clazz, loader, 30, TimeUnit.MINUTES);
    }

    public <T> T get(String key, Class<T> clazz, Supplier<T> loader, long expire, TimeUnit unit) {
        T value = get(key, clazz);
        if (value != null) {
            return value;
        }

        value = loader.get();
        if (value != null) {
            set(key, value, expire, unit);
        }
        return value;
    }

    public void set(String key, String value) {
        set(key, value, 30, TimeUnit.MINUTES);
    }

    public void set(String key, String value, long expire, TimeUnit unit) {
        try {
            redisTemplate.opsForValue().set(key, value, expire, unit);
            localCache.put(key, new CacheEntry(value, System.currentTimeMillis() + LOCAL_CACHE_EXPIRE));
        } catch (Exception e) {
            log.error("设置缓存失败: key={}", key, e);
            localCache.put(key, new CacheEntry(value, System.currentTimeMillis() + LOCAL_CACHE_EXPIRE));
        }
    }

    public <T> void set(String key, T value) {
        set(key, value, 30, TimeUnit.MINUTES);
    }

    public <T> void set(String key, T value, long expire, TimeUnit unit) {
        set(key, JSON.toJSONString(value), expire, unit);
    }

    public void delete(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.error("删除缓存失败: key={}", key, e);
        }
        localCache.remove(key);
    }

    public void deleteLocal(String key) {
        localCache.remove(key);
    }

    public void clearLocal() {
        localCache.clear();
    }

    private void cleanupLocalCache() {
        while (true) {
            try {
                Thread.sleep(60 * 1000L);
                long now = System.currentTimeMillis();
                localCache.entrySet().removeIf(entry -> entry.getValue().isExpired());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private static class CacheEntry {
        private final String value;
        private final long expireTime;

        public CacheEntry(String value, long expireTime) {
            this.value = value;
            this.expireTime = expireTime;
        }

        public String getValue() {
            return value;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > expireTime;
        }
    }
}
