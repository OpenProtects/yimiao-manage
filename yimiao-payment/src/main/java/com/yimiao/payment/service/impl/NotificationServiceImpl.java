package com.yimiao.payment.service.impl;

import com.yimiao.payment.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class NotificationServiceImpl implements NotificationService {

    private final RabbitTemplate rabbitTemplate;

    public NotificationServiceImpl(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void sendAppointmentSuccess(Long userId, String orderNo, String vaccineName, String siteName, String date) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("type", "APPOINTMENT_SUCCESS");
        data.put("title", "预约成功通知");
        data.put("content", String.format("您已成功预约%s，接种点：%s，预约日期：%s", vaccineName, siteName, date));
        data.put("orderNo", orderNo);
        
        sendNotification(data);
        log.info("发送预约成功通知: userId={}, orderNo={}", userId, orderNo);
    }

    @Override
    public void sendAppointmentCancel(Long userId, String orderNo, String reason) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("type", "APPOINTMENT_CANCEL");
        data.put("title", "预约取消通知");
        data.put("content", String.format("您的预约已取消，原因：%s", reason));
        data.put("orderNo", orderNo);
        
        sendNotification(data);
        log.info("发送预约取消通知: userId={}, orderNo={}", userId, orderNo);
    }

    @Override
    public void sendVaccinationReminder(Long userId, String orderNo, String vaccineName, String siteName, String date) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("type", "VACCINATION_REMINDER");
        data.put("title", "接种提醒");
        data.put("content", String.format("您预约的%s接种将于明天进行，接种点：%s，请按时前往", vaccineName, siteName));
        data.put("orderNo", orderNo);
        
        sendNotification(data);
        log.info("发送接种提醒通知: userId={}, orderNo={}", userId, orderNo);
    }

    @Override
    public void sendPaymentSuccess(Long userId, String orderNo, String amount) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("type", "PAYMENT_SUCCESS");
        data.put("title", "支付成功通知");
        data.put("content", String.format("您的订单已支付成功，金额：%s元", amount));
        data.put("orderNo", orderNo);
        
        sendNotification(data);
        log.info("发送支付成功通知: userId={}, orderNo={}", userId, orderNo);
    }

    @Override
    public void sendRefundSuccess(Long userId, String orderNo, String amount) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("type", "REFUND_SUCCESS");
        data.put("title", "退款成功通知");
        data.put("content", String.format("您的订单已退款成功，金额：%s元", amount));
        data.put("orderNo", orderNo);
        
        sendNotification(data);
        log.info("发送退款成功通知: userId={}, orderNo={}", userId, orderNo);
    }

    private void sendNotification(Map<String, Object> data) {
        try {
            rabbitTemplate.convertAndSend("notification.exchange", "notification.send", data);
        } catch (Exception e) {
            log.error("发送通知失败: data={}", data, e);
        }
    }
}
