package com.yimiao.appointment.controller;

import com.yimiao.appointment.service.RiskControlService;
import com.yimiao.common.core.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/risk")
@Api(tags = "风控管理")
public class RiskControlController {

    private final RiskControlService riskControlService;

    public RiskControlController(RiskControlService riskControlService) {
        this.riskControlService = riskControlService;
    }

    @GetMapping("/check")
    @ApiOperation("检查预约资格")
    public Result<Boolean> checkQualification(
            @RequestParam Long userId,
            @RequestParam Long vaccineeId,
            @RequestParam Long vaccineId,
            @RequestParam Integer doseNo) {
        return Result.success(riskControlService.checkAppointmentQualification(userId, vaccineeId, vaccineId, doseNo));
    }

    @GetMapping("/queue/{userId}")
    @ApiOperation("获取排队位置")
    public Result<String> getQueuePosition(@PathVariable Long userId) {
        return Result.success(riskControlService.getQueuePosition(userId));
    }

    @PostMapping("/rate-limit/try")
    @ApiOperation("尝试获取限流许可")
    public Result<Boolean> tryRateLimit(
            @RequestParam Long userId,
            @RequestParam String operation) {
        return Result.success(riskControlService.tryAcquireRateLimit(userId, operation));
    }
}
