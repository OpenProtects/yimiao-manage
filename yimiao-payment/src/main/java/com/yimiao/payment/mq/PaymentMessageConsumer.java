package com.yimiao.payment.mq;

import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RabbitListener(queues = "payment.queue")
public class PaymentMessageConsumer {

    @RabbitHandler
    public void handlePaymentNotify(String message) {
        try {
            log.info("收到支付消息: {}", message);
            
            Map<String, Object> paymentInfo = JSON.parseObject(message, Map.class);
            String orderNo = (String) paymentInfo.get("orderNo");
            String tradeNo = (String) paymentInfo.get("tradeNo");
            Boolean success = (Boolean) paymentInfo.get("success");

            if (Boolean.TRUE.equals(success)) {
                sendPaymentSuccessNotification(orderNo, tradeNo);
            } else {
                sendPaymentFailNotification(orderNo);
            }
            
            log.info("支付消息处理完成: orderNo={}", orderNo);
        } catch (Exception e) {
            log.error("处理支付消息失败: {}", message, e);
        }
    }

    private void sendPaymentSuccessNotification(String orderNo, String tradeNo) {
        log.info("发送支付成功通知: orderNo={}, tradeNo={}", orderNo, tradeNo);

    }

    private void sendPaymentFailNotification(String orderNo) {
        log.info("发送支付失败通知: orderNo={}", orderNo);

    }
}
