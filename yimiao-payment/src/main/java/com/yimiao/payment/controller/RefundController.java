package com.yimiao.payment.controller;

import com.yimiao.common.core.Result;
import com.yimiao.payment.entity.RefundRecord;
import com.yimiao.payment.service.RefundService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Slf4j
@RestController
@RequestMapping("/refund")
@Api(tags = "退款管理")
public class RefundController {

    private final RefundService refundService;

    public RefundController(RefundService refundService) {
        this.refundService = refundService;
    }

    @PostMapping("/create")
    @ApiOperation("创建退款")
    public Result<String> create(
            @RequestParam Long orderId,
            @RequestParam String orderNo,
            @RequestParam Long userId,
            @RequestParam BigDecimal amount,
            @RequestParam String reason) {
        return Result.success(refundService.createRefund(orderId, orderNo, userId, amount, reason));
    }

    @PostMapping("/notify")
    @ApiOperation("退款回调")
    public Result<Void> notify(
            @RequestParam String refundNo,
            @RequestParam boolean success) {
        refundService.handleRefundNotify(refundNo, success);
        return Result.success();
    }

    @GetMapping("/record/{orderNo}")
    @ApiOperation("获取退款记录")
    public Result<RefundRecord> getRecord(@PathVariable String orderNo) {
        return Result.success(refundService.getByOrderNo(orderNo));
    }
}
