package com.yimiao.common.redis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class StockCache {

    private static final String STOCK_KEY_PREFIX = "stock:";
    private static final String STOCK_LUA = 
            "local stock = redis.call('get', KEYS[1]) " +
            "if not stock then " +
            "    return -1 " +
            "end " +
            "if tonumber(stock) >= tonumber(ARGV[1]) then " +
            "    return redis.call('decrby', KEYS[1], ARGV[1]) " +
            "else " +
            "    return -2 " +
            "end";

    private static final String DEDUCT_STOCK_LUA =
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
            "    return redis.call('del', KEYS[1]) " +
            "else " +
            "    return 0 " +
            "end";

    private final StringRedisTemplate redisTemplate;

    public StockCache(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void initStock(String stockId, int quantity) {
        String key = STOCK_KEY_PREFIX + stockId;
        redisTemplate.opsForValue().set(key, String.valueOf(quantity));
        log.info("初始化库存: stockId={}, quantity={}", stockId, quantity);
    }

    public Integer getStock(String stockId) {
        String key = STOCK_KEY_PREFIX + stockId;
        String stock = redisTemplate.opsForValue().get(key);
        if (stock == null) {
            return null;
        }
        return Integer.parseInt(stock);
    }

    public StockResult deductStock(String stockId, int quantity) {
        String key = STOCK_KEY_PREFIX + stockId;
        try {
            DefaultRedisScript<Long> script = new DefaultRedisScript<>(STOCK_LUA, Long.class);
            Long result = redisTemplate.execute(script, 
                    Collections.singletonList(key), 
                    String.valueOf(quantity));
            
            if (result == null) {
                return StockResult.error("执行失败");
            }
            
            if (result == -1) {
                return StockResult.notFound();
            }
            if (result == -2) {
                return StockResult.insufficient();
            }
            
            return StockResult.success(result.intValue());
        } catch (Exception e) {
            log.error("扣减库存失败: stockId={}, quantity={}", stockId, quantity, e);
            return StockResult.error(e.getMessage());
        }
    }

    public boolean addStock(String stockId, int quantity) {
        String key = STOCK_KEY_PREFIX + stockId;
        try {
            redisTemplate.opsForValue().increment(key, quantity);
            return true;
        } catch (Exception e) {
            log.error("增加库存失败: stockId={}, quantity={}", stockId, quantity, e);
            return false;
        }
    }

    public boolean setStock(String stockId, int quantity, long expire, TimeUnit unit) {
        String key = STOCK_KEY_PREFIX + stockId;
        try {
            redisTemplate.opsForValue().set(key, String.valueOf(quantity), expire, unit);
            return true;
        } catch (Exception e) {
            log.error("设置库存失败: stockId={}, quantity={}", stockId, quantity, e);
            return false;
        }
    }

    public boolean deleteStock(String stockId) {
        String key = STOCK_KEY_PREFIX + stockId;
        try {
            Boolean result = redisTemplate.delete(key);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.error("删除库存失败: stockId={}", stockId, e);
            return false;
        }
    }

    public static class StockResult {
        private final boolean success;
        private final Integer remaining;
        private final String message;
        private final String code;

        private StockResult(boolean success, Integer remaining, String message, String code) {
            this.success = success;
            this.remaining = remaining;
            this.message = message;
            this.code = code;
        }

        public static StockResult success(int remaining) {
            return new StockResult(true, remaining, "扣减成功", "SUCCESS");
        }

        public static StockResult insufficient() {
            return new StockResult(false, null, "库存不足", "INSUFFICIENT");
        }

        public static StockResult notFound() {
            return new StockResult(false, null, "库存不存在", "NOT_FOUND");
        }

        public static StockResult error(String message) {
            return new StockResult(false, null, message, "ERROR");
        }

        public boolean isSuccess() {
            return success;
        }

        public Integer getRemaining() {
            return remaining;
        }

        public String getMessage() {
            return message;
        }

        public String getCode() {
            return code;
        }
    }
}
