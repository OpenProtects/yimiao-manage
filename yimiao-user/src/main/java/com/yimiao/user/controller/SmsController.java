package com.yimiao.user.controller;

import com.yimiao.common.core.Result;
import com.yimiao.common.redis.RedisService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/sms")
@Api(tags = "短信服务")
public class SmsController {

    private final RedisService redisService;
    private static final String SMS_CODE_PREFIX = "yimiao:user:sms:";
    private static final Map<String, SmsRecord> smsRecords = new ConcurrentHashMap<>();

    public SmsController(RedisService redisService) {
        this.redisService = redisService;
    }

    @PostMapping("/send")
    @ApiOperation("发送验证码")
    public Result<Map<String, Object>> sendCode(@RequestParam String phone) {
        String code = generateCode();
        String key = SMS_CODE_PREFIX + phone;
        
        redisService.set(key, code, 5, TimeUnit.MINUTES);
        
        SmsRecord record = new SmsRecord();
        record.setPhone(phone);
        record.setCode(code);
        record.setCreateTime(System.currentTimeMillis());
        record.setExpireTime(System.currentTimeMillis() + 5 * 60 * 1000);
        smsRecords.put(phone, record);
        
        log.info("========================================");
        log.info("【模拟短信】手机号: {}", phone);
        log.info("【模拟短信】验证码: {}", code);
        log.info("【模拟短信】有效期: 5分钟");
        log.info("========================================");
        
        Map<String, Object> data = new HashMap<>();
        data.put("phone", phone);
        data.put("code", code);
        data.put("expireIn", 300);
        data.put("message", "验证码已发送，请查看控制台日志或调用 /sms/code/{phone} 接口获取");
        
        return Result.success(data);
    }

    @GetMapping("/code/{phone}")
    @ApiOperation("获取验证码(测试用)")
    public Result<Map<String, Object>> getCode(@PathVariable String phone) {
        String key = SMS_CODE_PREFIX + phone;
        String cachedCode = redisService.get(key);
        
        if (cachedCode == null) {
            return Result.error("验证码已过期或不存在");
        }
        
        Map<String, Object> data = new HashMap<>();
        data.put("phone", phone);
        data.put("code", cachedCode);
        data.put("expireIn", redisService.getExpire(key, TimeUnit.SECONDS));
        
        return Result.success(data);
    }

    @GetMapping("/records")
    @ApiOperation("获取所有验证码记录(测试用)")
    public Result<Map<String, SmsRecord>> getAllRecords() {
        return Result.success(smsRecords);
    }

    @PostMapping("/verify")
    @ApiOperation("验证验证码")
    public Result<Boolean> verifyCode(@RequestParam String phone, @RequestParam String code) {
        String key = SMS_CODE_PREFIX + phone;
        String cachedCode = redisService.get(key);
        
        if (cachedCode == null) {
            return Result.success(false);
        }
        
        boolean valid = cachedCode.equals(code);
        if (valid) {
            redisService.delete(key);
            smsRecords.remove(phone);
        }
        
        return Result.success(valid);
    }

    private String generateCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    @lombok.Data
    public static class SmsRecord {
        private String phone;
        private String code;
        private Long createTime;
        private Long expireTime;
    }
}
