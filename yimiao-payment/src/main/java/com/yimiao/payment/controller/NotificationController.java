package com.yimiao.payment.controller;

import com.yimiao.common.core.Result;
import com.yimiao.payment.service.NotificationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/notification")
@Api(tags = "通知管理")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/appointment-success")
    @ApiOperation("发送预约成功通知")
    public Result<Void> sendAppointmentSuccess(
            @RequestParam Long userId,
            @RequestParam String orderNo,
            @RequestParam String vaccineName,
            @RequestParam String siteName,
            @RequestParam String date) {
        notificationService.sendAppointmentSuccess(userId, orderNo, vaccineName, siteName, date);
        return Result.success();
    }

    @PostMapping("/appointment-cancel")
    @ApiOperation("发送预约取消通知")
    public Result<Void> sendAppointmentCancel(
            @RequestParam Long userId,
            @RequestParam String orderNo,
            @RequestParam String reason) {
        notificationService.sendAppointmentCancel(userId, orderNo, reason);
        return Result.success();
    }

    @PostMapping("/vaccination-reminder")
    @ApiOperation("发送接种提醒")
    public Result<Void> sendVaccinationReminder(
            @RequestParam Long userId,
            @RequestParam String orderNo,
            @RequestParam String vaccineName,
            @RequestParam String siteName,
            @RequestParam String date) {
        notificationService.sendVaccinationReminder(userId, orderNo, vaccineName, siteName, date);
        return Result.success();
    }

    @PostMapping("/payment-success")
    @ApiOperation("发送支付成功通知")
    public Result<Void> sendPaymentSuccess(
            @RequestParam Long userId,
            @RequestParam String orderNo,
            @RequestParam String amount) {
        notificationService.sendPaymentSuccess(userId, orderNo, amount);
        return Result.success();
    }

    @PostMapping("/refund-success")
    @ApiOperation("发送退款成功通知")
    public Result<Void> sendRefundSuccess(
            @RequestParam Long userId,
            @RequestParam String orderNo,
            @RequestParam String amount) {
        notificationService.sendRefundSuccess(userId, orderNo, amount);
        return Result.success();
    }
}
