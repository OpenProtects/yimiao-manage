package com.yimiao.payment.controller;

import com.yimiao.api.dto.PaymentDTO;
import com.yimiao.common.core.Result;
import com.yimiao.payment.entity.PaymentChannel;
import com.yimiao.payment.entity.PaymentRecord;
import com.yimiao.payment.service.PaymentChannelService;
import com.yimiao.payment.service.PaymentService;
import com.yimiao.payment.service.impl.PaymentServiceImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/payment")
@Api(tags = "支付管理")
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentServiceImpl paymentServiceImpl;
    private final PaymentChannelService channelService;

    public PaymentController(PaymentService paymentService, 
                             PaymentServiceImpl paymentServiceImpl,
                             PaymentChannelService channelService) {
        this.paymentService = paymentService;
        this.paymentServiceImpl = paymentServiceImpl;
        this.channelService = channelService;
    }

    @PostMapping("/create")
    @ApiOperation("创建支付")
    public Result<String> create(@RequestBody PaymentDTO dto) {
        return Result.success(paymentService.createPayment(dto));
    }

    @GetMapping("/pay-url/{tradeNo}")
    @ApiOperation("获取支付链接")
    public Result<String> getPayUrl(@PathVariable String tradeNo, 
                                    @RequestParam(defaultValue = "疫苗预约") String subject) {
        return Result.success(paymentServiceImpl.getPayUrl(tradeNo, subject));
    }

    @PostMapping("/notify/{channelCode}")
    @ApiOperation("支付渠道回调")
    public String channelNotify(@PathVariable String channelCode, 
                                @RequestParam Map<String, String> params) {
        try {
            paymentServiceImpl.handleChannelNotify(channelCode, params);
            return "success";
        } catch (Exception e) {
            log.error("支付回调处理失败: channelCode={}", channelCode, e);
            return "fail";
        }
    }

    @PostMapping("/notify")
    @ApiOperation("支付回调(内部)")
    public Result<Void> notify(
            @RequestParam String tradeNo,
            @RequestParam String orderNo,
            @RequestParam boolean success) {
        paymentService.handlePayNotify(tradeNo, orderNo, success);
        return Result.success();
    }

    @GetMapping("/status/{orderNo}")
    @ApiOperation("查询支付状态")
    public Result<Boolean> queryStatus(@PathVariable String orderNo) {
        return Result.success(paymentService.queryPayStatus(orderNo));
    }

    @PostMapping("/close/{orderNo}")
    @ApiOperation("关闭支付")
    public Result<Void> close(@PathVariable String orderNo) {
        paymentService.closePayment(orderNo);
        return Result.success();
    }

    @GetMapping("/record/{orderNo}")
    @ApiOperation("获取支付记录")
    public Result<PaymentRecord> getRecord(@PathVariable String orderNo) {
        return Result.success(paymentService.getByOrderNo(orderNo));
    }

    @GetMapping("/channels")
    @ApiOperation("获取可用支付渠道列表")
    public Result<List<PaymentChannel>> getEnabledChannels() {
        return Result.success(paymentServiceImpl.getEnabledChannels());
    }

    @GetMapping("/channel/list")
    @ApiOperation("获取所有支付渠道(管理)")
    public Result<List<PaymentChannel>> listAllChannels() {
        return Result.success(channelService.list());
    }

    @PostMapping("/channel/enable/{id}")
    @    @ApiOperation("启用支付渠道")
    public Result<Boolean> enableChannel(@PathVariable Long id) {
        return Result.success(channelService.enableChannel(id));
    }

    @PostMapping("/channel/disable/{id}")
    @ApiOperation("禁用支付渠道")
    public Result<Boolean> disableChannel(@PathVariable Long id) {
        return Result.success(channelService.disableChannel(id));
    }

    @PostMapping("/channel/update")
    @ApiOperation("更新支付渠道配置")
    public Result<Boolean> updateChannel(@RequestBody PaymentChannel channel) {
        return Result.success(channelService.updateChannel(channel));
    }

    @GetMapping("/channel/{id}")
    @ApiOperation("获取支付渠道详情")
    public Result<PaymentChannel> getChannel(@PathVariable Long id) {
        return Result.success(channelService.getById(id));
    }
    
    @PostMapping("/test/create")
    @ApiOperation("创建测试支付(用于本地测试)")
    public Result<Map<String, Object>> createTestPayment(@RequestBody TestPaymentDTO dto) {
        log.info("创建测试支付: {}", dto);
        
        String tradeNo = "TEST" + System.currentTimeMillis();
        String payUrl = "http://localhost:3000/test-pay?tradeNo=" + tradeNo + "&amount=" + dto.getAmount();
        
        Map<String, Object> result = new HashMap<>();
        result.put("tradeNo", tradeNo);
        result.put("payUrl", payUrl);
        result.put("amount", dto.getAmount());
        result.put("channelCode", dto.getChannelCode());
        result.put("subject", dto.getSubject());
        result.put("status", "pending");
        
        return Result.success(result);
    }
    
    @PostMapping("/test/complete")
    @ApiOperation("完成测试支付(用于本地测试)")
    public Result<Boolean> completeTestPayment(
            @RequestParam String tradeNo,
            @RequestParam String orderNo,
            @RequestParam(defaultValue = "true") boolean success) {
        log.info("完成测试支付: tradeNo={}, orderNo={}, success={}", tradeNo, orderNo, success);
        
        try {
            paymentService.handlePayNotify(tradeNo, orderNo, success);
            return Result.success(true);
        } catch (Exception e) {
            log.error("测试支付完成失败", e);
            return Result.success(false);
        }
    }
    
    @GetMapping("/test/channels")
    @ApiOperation("获取测试支付渠道列表")
    public Result<List<Map<String, Object>>> getTestChannels() {
        List<PaymentChannel> channels = channelService.list();
        List<Map<String, Object>> result = new java.util.ArrayList<>();
        
        for (PaymentChannel channel : channels) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", channel.getId());
            item.put("code", channel.getChannelCode());
            item.put("name", channel.getChannelName());
            item.put("type", channel.getChannelType());
            item.put("status", channel.getStatus());
            item.put("isTest", true);
            result.add(item);
        }
        
        return Result.success(result);
    }

    public static class TestPaymentDTO {
        private Double amount;
        private String channelCode;
        private String subject;
        private String orderNo;

        public Double getAmount() { return amount; }
        public void setAmount(Double amount) { this.amount = amount; }
        public String getChannelCode() { return channelCode; }
        public void setChannelCode(String channelCode) { this.channelCode = channelCode; }
        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }
        public String getOrderNo() { return orderNo; }
        public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    }
}
