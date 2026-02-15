package com.yimiao.payment.service.impl;

import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yimiao.api.dto.PaymentDTO;
import com.yimiao.common.constant.RedisKeyConstant;
import com.yimiao.common.core.ResultCode;
import com.yimiao.common.exception.BusinessException;
import com.yimiao.common.redis.RedisLock;
import com.yimiao.common.redis.RedisService;
import com.yimiao.payment.entity.PaymentChannel;
import com.yimiao.payment.entity.PaymentRecord;
import com.yimiao.payment.mapper.PaymentRecordMapper;
import com.yimiao.payment.service.PaymentChannelService;
import com.yimiao.payment.service.PaymentService;
import com.yimiao.payment.strategy.PaymentStrategy;
import com.yimiao.payment.strategy.PaymentStrategyFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class PaymentServiceImpl extends ServiceImpl<PaymentRecordMapper, PaymentRecord> implements PaymentService {

    private final RedisService redisService;
    private final RedisLock redisLock;
    private final RabbitTemplate rabbitTemplate;
    private final PaymentChannelService channelService;
    private final PaymentStrategyFactory strategyFactory;

    public PaymentServiceImpl(RedisService redisService, RedisLock redisLock, 
                              RabbitTemplate rabbitTemplate, PaymentChannelService channelService,
                              PaymentStrategyFactory strategyFactory) {
        this.redisService = redisService;
        this.redisLock = redisLock;
        this.rabbitTemplate = rabbitTemplate;
        this.channelService = channelService;
        this.strategyFactory = strategyFactory;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createPayment(PaymentDTO dto) {
        String lockKey = RedisKeyConstant.PAYMENT_ORDER + dto.getOrderId();
        String lockValue = IdUtil.fastSimpleUUID();
        
        if (!redisLock.tryLock(lockKey, lockValue, 30, TimeUnit.SECONDS)) {
            throw new BusinessException("操作过于频繁，请稍后再试");
        }
        
        try {
            PaymentRecord existing = getByOrderNo(dto.getOrderId().toString());
            if (existing != null && existing.getStatus() == 1) {
                throw new BusinessException("订单已支付");
            }
            
            PaymentChannel channel = channelService.getByCode(dto.getChannelCode());
            if (channel == null || channel.getStatus() != 1) {
                throw new BusinessException("支付渠道不可用");
            }

            String tradeNo = generateTradeNo();
            
            PaymentRecord record = new PaymentRecord();
            record.setOrderId(dto.getOrderId());
            record.setOrderNo(dto.getOrderId().toString());
            record.setUserId(dto.getUserId());
            record.setAmount(dto.getAmount());
            record.setPayType(dto.getPayType());
            record.setChannelCode(dto.getChannelCode());
            record.setTradeNo(tradeNo);
            record.setStatus(0);
            
            save(record);
            
            String cacheKey = RedisKeyConstant.PAYMENT_ORDER + dto.getOrderId();
            redisService.setObject(cacheKey, record, 30, TimeUnit.MINUTES);
            
            log.info("创建支付记录: orderId={}, tradeNo={}, channelCode={}, amount={}", 
                    dto.getOrderId(), tradeNo, dto.getChannelCode(), dto.getAmount());
            return tradeNo;
        } finally {
            redisLock.unlock(lockKey, lockValue);
        }
    }

    public String getPayUrl(String tradeNo, String subject) {
        PaymentRecord record = getOne(new LambdaQueryWrapper<PaymentRecord>()
                .eq(PaymentRecord::getTradeNo, tradeNo)
                .eq(PaymentRecord::getDeleted, 0));
        
        if (record == null) {
            throw new BusinessException("支付记录不存在");
        }
        
        if (record.getStatus() != 0) {
            throw new BusinessException("订单状态异常");
        }

        PaymentChannel channel = channelService.getByCode(record.getChannelCode());
        if (channel == null || channel.getStatus() != 1) {
            throw new BusinessException("支付渠道不可用");
        }

        PaymentStrategy strategy = strategyFactory.getStrategy(record.getChannelCode());
        return strategy.createPayment(channel, record, subject);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handlePayNotify(String tradeNo, String orderNo, boolean success) {
        PaymentRecord record = getOne(new LambdaQueryWrapper<PaymentRecord>()
                .eq(PaymentRecord::getTradeNo, tradeNo)
                .eq(PaymentRecord::getDeleted, 0));
        
        if (record == null) {
            log.warn("支付通知未找到记录: tradeNo={}", tradeNo);
            return;
        }
        
        if (success) {
            record.setStatus(1);
            record.setPayTime(LocalDateTime.now());
        } else {
            record.setStatus(2);
        }
        record.setNotifyTime(LocalDateTime.now());
        
        updateById(record);
        
        if (success) {
            sendPaymentMessage(record);
        }
        
        log.info("处理支付通知: tradeNo={}, success={}", tradeNo, success);
    }

    public void handleChannelNotify(String channelCode, java.util.Map<String, String> params) {
        PaymentChannel channel = channelService.getByCode(channelCode);
        if (channel == null) {
            log.error("支付渠道不存在: {}", channelCode);
            return;
        }

        PaymentStrategy strategy = strategyFactory.getStrategy(channelCode);
        if (!strategy.verifyNotify(channel, params)) {
            log.error("支付回调验签失败: channelCode={}", channelCode);
            return;
        }

        String tradeNo = params.get("out_trade_no");
        String tradeStatus = params.get("trade_status");
        boolean success = "TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus);
        
        handlePayNotify(tradeNo, null, success);
    }

    @Override
    public boolean queryPayStatus(String orderNo) {
        PaymentRecord record = getByOrderNo(orderNo);
        if (record == null) {
            return false;
        }
        return record.getStatus() == 1;
    }

    @Override
    public void closePayment(String orderNo) {
        PaymentRecord record = getByOrderNo(orderNo);
        if (record != null && record.getStatus() == 0) {
            record.setStatus(2);
            record.setFailReason("订单超时关闭");
            updateById(record);
            log.info("关闭支付订单: orderNo={}", orderNo);
        }
    }

    @Override
    public PaymentRecord getByOrderNo(String orderNo) {
        return getOne(new LambdaQueryWrapper<PaymentRecord>()
                .eq(PaymentRecord::getOrderNo, orderNo)
                .eq(PaymentRecord::getDeleted, 0));
    }

    public List<PaymentChannel> getEnabledChannels() {
        return channelService.listEnabledChannels();
    }

    private String generateTradeNo() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String randomStr = IdUtil.fastSimpleUUID().substring(0, 8).toUpperCase();
        return "PAY" + dateStr + randomStr;
    }

    private void sendPaymentMessage(PaymentRecord record) {
        try {
            rabbitTemplate.convertAndSend("notification.exchange", "notification.send",
                    JSON.toJSONString(record));
        } catch (Exception e) {
            log.error("发送支付消息失败: orderId={}", record.getOrderId(), e);
        }
    }
}
