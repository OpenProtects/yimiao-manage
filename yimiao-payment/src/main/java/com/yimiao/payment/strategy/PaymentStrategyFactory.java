package com.yimiao.payment.strategy;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class PaymentStrategyFactory {
    private final List<PaymentStrategy> strategies;
    private final Map<String, PaymentStrategy> strategyMap = new HashMap<>();

    public PaymentStrategyFactory(List<PaymentStrategy> strategies) {
        this.strategies = strategies;
    }

    @PostConstruct
    public void init() {
        for (PaymentStrategy strategy : strategies) {
            strategyMap.put(strategy.getChannelCode(), strategy);
            log.info("注册支付策略: {}", strategy.getChannelCode());
        }
    }

    public PaymentStrategy getStrategy(String channelCode) {
        PaymentStrategy strategy = strategyMap.get(channelCode);
        if (strategy == null) {
            throw new IllegalArgumentException("不支持的支付渠道: " + channelCode);
        }
        return strategy;
    }

    public boolean hasStrategy(String channelCode) {
        return strategyMap.containsKey(channelCode);
    }
}
