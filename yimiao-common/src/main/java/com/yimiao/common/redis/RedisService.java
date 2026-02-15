package com.yimiao.common.redis;

import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class RedisService {

    private final StringRedisTemplate redisTemplate;

    public RedisService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void set(String key, String value) {
        try {
            redisTemplate.opsForValue().set(key, value);
        } catch (Exception e) {
            log.error("Redis set操作失败: key={}", key, e);
        }
    }

    public void set(String key, String value, long timeout, TimeUnit unit) {
        try {
            redisTemplate.opsForValue().set(key, value, timeout, unit);
        } catch (Exception e) {
            log.error("Redis set操作失败: key={}", key, e);
        }
    }

    public String get(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("Redis get操作失败: key={}", key, e);
            return null;
        }
    }

    public <T> void setObject(String key, T value) {
        try {
            redisTemplate.opsForValue().set(key, JSON.toJSONString(value));
        } catch (Exception e) {
            log.error("Redis setObject操作失败: key={}", key, e);
        }
    }

    public <T> void setObject(String key, T value, long timeout, TimeUnit unit) {
        try {
            redisTemplate.opsForValue().set(key, JSON.toJSONString(value), timeout, unit);
        } catch (Exception e) {
            log.error("Redis setObject操作失败: key={}", key, e);
        }
    }

    public <T> T getObject(String key, Class<T> clazz) {
        try {
            String value = redisTemplate.opsForValue().get(key);
            if (value == null) {
                return null;
            }
            return JSON.parseObject(value, clazz);
        } catch (Exception e) {
            log.error("Redis getObject操作失败: key={}", key, e);
            return null;
        }
    }

    public Boolean delete(String key) {
        try {
            return redisTemplate.delete(key);
        } catch (Exception e) {
            log.error("Redis delete操作失败: key={}", key, e);
            return false;
        }
    }

    public Long delete(Collection<String> keys) {
        try {
            return redisTemplate.delete(keys);
        } catch (Exception e) {
            log.error("Redis delete操作失败: keys={}", keys, e);
            return 0L;
        }
    }

    public Boolean hasKey(String key) {
        try {
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            log.error("Redis hasKey操作失败: key={}", key, e);
            return false;
        }
    }

    public Boolean expire(String key, long timeout, TimeUnit unit) {
        try {
            return redisTemplate.expire(key, timeout, unit);
        } catch (Exception e) {
            log.error("Redis expire操作失败: key={}", key, e);
            return false;
        }
    }

    public Long getExpire(String key, TimeUnit unit) {
        try {
            return redisTemplate.getExpire(key, unit);
        } catch (Exception e) {
            log.error("Redis getExpire操作失败: key={}", key, e);
            return -2L;
        }
    }

    public Long increment(String key) {
        try {
            return redisTemplate.opsForValue().increment(key);
        } catch (Exception e) {
            log.error("Redis increment操作失败: key={}", key, e);
            return null;
        }
    }

    public Long increment(String key, long delta) {
        try {
            return redisTemplate.opsForValue().increment(key, delta);
        } catch (Exception e) {
            log.error("Redis increment操作失败: key={}", key, e);
            return null;
        }
    }

    public Long decrement(String key) {
        try {
            return redisTemplate.opsForValue().decrement(key);
        } catch (Exception e) {
            log.error("Redis decrement操作失败: key={}", key, e);
            return null;
        }
    }

    public Long decrement(String key, long delta) {
        try {
            return redisTemplate.opsForValue().decrement(key, delta);
        } catch (Exception e) {
            log.error("Redis decrement操作失败: key={}", key, e);
            return null;
        }
    }

    public Long size(String key) {
        try {
            return redisTemplate.opsForValue().size(key);
        } catch (Exception e) {
            log.error("Redis size操作失败: key={}", key, e);
            return 0L;
        }
    }

    public void setHash(String key, String field, String value) {
        try {
            redisTemplate.opsForHash().put(key, field, value);
        } catch (Exception e) {
            log.error("Redis setHash操作失败: key={}, field={}", key, field, e);
        }
    }

    public Object getHash(String key, String field) {
        try {
            return redisTemplate.opsForHash().get(key, field);
        } catch (Exception e) {
            log.error("Redis getHash操作失败: key={}, field={}", key, field, e);
            return null;
        }
    }

    public Long deleteHash(String key, String... fields) {
        try {
            return redisTemplate.opsForHash().delete(key, (Object[]) fields);
        } catch (Exception e) {
            log.error("Redis deleteHash操作失败: key={}", key, e);
            return 0L;
        }
    }

    public Long leftPush(String key, String value) {
        try {
            return redisTemplate.opsForList().leftPush(key, value);
        } catch (Exception e) {
            log.error("Redis leftPush操作失败: key={}", key, e);
            return 0L;
        }
    }

    public String rightPop(String key) {
        try {
            return redisTemplate.opsForList().rightPop(key);
        } catch (Exception e) {
            log.error("Redis rightPop操作失败: key={}", key, e);
            return null;
        }
    }

    public Long addSet(String key, String... values) {
        try {
            return redisTemplate.opsForSet().add(key, values);
        } catch (Exception e) {
            log.error("Redis addSet操作失败: key={}", key, e);
            return 0L;
        }
    }

    public Boolean isMember(String key, String value) {
        try {
            return redisTemplate.opsForSet().isMember(key, value);
        } catch (Exception e) {
            log.error("Redis isMember操作失败: key={}", key, e);
            return false;
        }
    }

    public Boolean addZSet(String key, String value, double score) {
        try {
            return redisTemplate.opsForZSet().add(key, value, score);
        } catch (Exception e) {
            log.error("Redis addZSet操作失败: key={}", key, e);
            return false;
        }
    }

    public Long rank(String key, String value) {
        try {
            return redisTemplate.opsForZSet().rank(key, value);
        } catch (Exception e) {
            log.error("Redis rank操作失败: key={}", key, e);
            return null;
        }
    }
}
