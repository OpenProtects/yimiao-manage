package com.yimiao.appointment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yimiao.appointment.entity.Blacklist;
import com.yimiao.appointment.mapper.BlacklistMapper;
import com.yimiao.appointment.service.BlacklistService;
import com.yimiao.common.constant.RedisKeyConstant;
import com.yimiao.common.redis.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class BlacklistServiceImpl extends ServiceImpl<BlacklistMapper, Blacklist> implements BlacklistService {

    private final RedisService redisService;

    public BlacklistServiceImpl(RedisService redisService) {
        this.redisService = redisService;
    }

    @Override
    public boolean isInBlacklist(String idCard) {
        String cacheKey = RedisKeyConstant.BLACKLIST + idCard;
        
        String cached = redisService.get(cacheKey);
        if ("1".equals(cached)) {
            return true;
        }
        if ("0".equals(cached)) {
            return false;
        }
        
        Blacklist blacklist = getByIdCard(idCard);
        boolean inBlacklist = blacklist != null && blacklist.getStatus() == 0;
        
        if (inBlacklist && blacklist.getType() == 2 && blacklist.getExpireTime() != null) {
            if (blacklist.getExpireTime().isBefore(LocalDateTime.now())) {
                inBlacklist = false;
                blacklist.setStatus(1);
                updateById(blacklist);
            }
        }
        
        redisService.set(cacheKey, inBlacklist ? "1" : "0", 1, TimeUnit.HOURS);
        
        return inBlacklist;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addToBlacklist(String idCard, String realName, String reason, Integer type, Long operatorId) {
        Blacklist existing = getByIdCard(idCard);
        
        if (existing != null) {
            existing.setReason(reason);
            existing.setType(type);
            existing.setStatus(0);
            existing.setOperatorId(operatorId);
            updateById(existing);
        } else {
            Blacklist blacklist = new Blacklist();
            blacklist.setIdCard(idCard);
            blacklist.setRealName(realName);
            blacklist.setReason(reason);
            blacklist.setType(type);
            blacklist.setStatus(0);
            blacklist.setOperatorId(operatorId);
            save(blacklist);
        }
        
        String cacheKey = RedisKeyConstant.BLACKLIST + idCard;
        redisService.set(cacheKey, "1", 1, TimeUnit.HOURS);
        
        log.info("添加黑名单: idCard={}, reason={}", idCard, reason);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeFromBlacklist(String idCard) {
        Blacklist blacklist = getByIdCard(idCard);
        if (blacklist != null) {
            blacklist.setStatus(1);
            updateById(blacklist);
        }
        
        String cacheKey = RedisKeyConstant.BLACKLIST + idCard;
        redisService.set(cacheKey, "0", 1, TimeUnit.HOURS);
        
        log.info("移除黑名单: idCard={}", idCard);
    }

    @Override
    public Blacklist getByIdCard(String idCard) {
        return getOne(new LambdaQueryWrapper<Blacklist>()
                .eq(Blacklist::getIdCard, idCard)
                .eq(Blacklist::getDeleted, 0));
    }
}
