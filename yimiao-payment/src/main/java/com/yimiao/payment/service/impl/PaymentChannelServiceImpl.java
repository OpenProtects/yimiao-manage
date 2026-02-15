package com.yimiao.payment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yimiao.payment.entity.PaymentChannel;
import com.yimiao.payment.mapper.PaymentChannelMapper;
import com.yimiao.payment.service.PaymentChannelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class PaymentChannelServiceImpl extends ServiceImpl<PaymentChannelMapper, PaymentChannel> implements PaymentChannelService {

    @Override
    public List<PaymentChannel> listEnabledChannels() {
        return list(new LambdaQueryWrapper<PaymentChannel>()
                .eq(PaymentChannel::getStatus, 1)
                .eq(PaymentChannel::getDeleted, 0)
                .orderByAsc(PaymentChannel::getSort));
    }

    @Override
    public PaymentChannel getByCode(String channelCode) {
        return getOne(new LambdaQueryWrapper<PaymentChannel>()
                .eq(PaymentChannel::getChannelCode, channelCode)
                .eq(PaymentChannel::getDeleted, 0));
    }

    @Override
    public boolean enableChannel(Long channelId) {
        PaymentChannel channel = getById(channelId);
        if (channel == null) {
            return false;
        }
        channel.setStatus(1);
        updateById(channel);
        log.info("启用支付渠道: channelId={}, channelCode={}", channelId, channel.getChannelCode());
        return true;
    }

    @Override
    public boolean disableChannel(Long channelId) {
        PaymentChannel channel = getById(channelId);
        if (channel == null) {
            return false;
        }
        channel.setStatus(0);
        updateById(channel);
        log.info("禁用支付渠道: channelId={}, channelCode={}", channelId, channel.getChannelCode());
        return true;
    }

    @Override
    public boolean updateChannel(PaymentChannel channel) {
        PaymentChannel existing = getById(channel.getId());
        if (existing == null) {
            return false;
        }
        existing.setChannelName(channel.getChannelName());
        existing.setChannelIcon(channel.getChannelIcon());
        existing.setApiUrl(channel.getApiUrl());
        existing.setAppId(channel.getAppId());
        existing.setAppSecret(channel.getAppSecret());
        existing.setMerchantId(channel.getMerchantId());
        existing.setMerchantPrivateKey(channel.getMerchantPrivateKey());
        existing.setPlatformPublicKey(channel.getPlatformPublicKey());
        existing.setNotifyUrl(channel.getNotifyUrl());
        existing.setReturnUrl(channel.getReturnUrl());
        existing.setSort(channel.getSort());
        existing.setRemark(channel.getRemark());
        updateById(existing);
        log.info("更新支付渠道: channelId={}", channel.getId());
        return true;
    }
}
