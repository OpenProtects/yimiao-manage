package com.yimiao.payment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yimiao.payment.entity.PaymentChannel;

import java.util.List;

public interface PaymentChannelService extends IService<PaymentChannel> {
    List<PaymentChannel> listEnabledChannels();
    PaymentChannel getByCode(String channelCode);
    boolean enableChannel(Long channelId);
    boolean disableChannel(Long channelId);
    boolean updateChannel(PaymentChannel channel);
}
