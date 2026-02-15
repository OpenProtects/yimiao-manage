package com.yimiao.payment.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yimiao.common.constant.RedisKeyConstant;
import com.yimiao.common.redis.RedisService;
import com.yimiao.payment.entity.RefundRecord;
import com.yimiao.payment.mapper.RefundRecordMapper;
import com.yimiao.payment.service.RefundService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
public class RefundServiceImpl extends ServiceImpl<RefundRecordMapper, RefundRecord> implements RefundService {

    private final RedisService redisService;

    public RefundServiceImpl(RedisService redisService) {
        this.redisService = redisService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createRefund(Long orderId, String orderNo, Long userId, BigDecimal amount, String reason) {
        RefundRecord existing = getByOrderNo(orderNo);
        if (existing != null && existing.getStatus() == 1) {
            log.warn("退款已存在: orderNo={}", orderNo);
            return existing.getRefundNo();
        }
        
        String refundNo = generateRefundNo();
        
        RefundRecord record = new RefundRecord();
        record.setOrderId(orderId);
        record.setOrderNo(orderNo);
        record.setUserId(userId);
        record.setAmount(amount);
        record.setReason(reason);
        record.setRefundNo(refundNo);
        record.setStatus(0);
        
        save(record);
        
        log.info("创建退款记录: orderId={}, refundNo={}, amount={}", orderId, refundNo, amount);
        return refundNo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleRefundNotify(String refundNo, boolean success) {
        RefundRecord record = getOne(new LambdaQueryWrapper<RefundRecord>()
                .eq(RefundRecord::getRefundNo, refundNo)
                .eq(RefundRecord::getDeleted, 0));
        
        if (record == null) {
            log.warn("退款通知未找到记录: refundNo={}", refundNo);
            return;
        }
        
        if (success) {
            record.setStatus(1);
            record.setRefundTime(LocalDateTime.now());
        } else {
            record.setStatus(2);
        }
        
        updateById(record);
        
        log.info("处理退款通知: refundNo={}, success={}", refundNo, success);
    }

    @Override
    public RefundRecord getByOrderNo(String orderNo) {
        return getOne(new LambdaQueryWrapper<RefundRecord>()
                .eq(RefundRecord::getOrderNo, orderNo)
                .eq(RefundRecord::getDeleted, 0));
    }

    private String generateRefundNo() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String randomStr = IdUtil.fastSimpleUUID().substring(0, 8).toUpperCase();
        return "REF" + dateStr + randomStr;
    }
}
