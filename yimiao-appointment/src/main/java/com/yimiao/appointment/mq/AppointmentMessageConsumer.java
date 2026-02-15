package com.yimiao.appointment.mq;

import com.alibaba.fastjson2.JSON;
import com.yimiao.api.vo.AppointmentVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RabbitListener(queues = "appointment.queue")
public class AppointmentMessageConsumer {

    @RabbitHandler
    public void handleAppointmentCreated(String message) {
        try {
            log.info("收到预约消息: {}", message);
            
            AppointmentVO appointment = JSON.parseObject(message, AppointmentVO.class);

            sendNotification(appointment);
            
            log.info("预约消息处理完成: orderNo={}", appointment.getOrderNo());
        } catch (Exception e) {
            log.error("处理预约消息失败: {}", message, e);
        }
    }

    private void sendNotification(AppointmentVO appointment) {
        log.info("发送预约成功通知: orderNo={}, userId={}", appointment.getOrderNo(), appointment.getUserId());

    }
}
