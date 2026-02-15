package com.yimiao.appointment.controller;

import com.yimiao.appointment.entity.Blacklist;
import com.yimiao.appointment.service.BlacklistService;
import com.yimiao.common.core.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/blacklist")
@Api(tags = "黑名单管理")
public class BlacklistController {

    private final BlacklistService blacklistService;

    public BlacklistController(BlacklistService blacklistService) {
        this.blacklistService = blacklistService;
    }

    @GetMapping("/check/{idCard}")
    @ApiOperation("检查是否在黑名单")
    public Result<Boolean> check(@PathVariable String idCard) {
        return Result.success(blacklistService.isInBlacklist(idCard));
    }

    @PostMapping("/add")
    @ApiOperation("添加黑名单")
    public Result<Void> add(
            @RequestParam String idCard,
            @RequestParam String realName,
            @RequestParam String reason,
            @RequestParam(defaultValue = "1") Integer type,
            @RequestHeader("X-User-Id") Long operatorId) {
        blacklistService.addToBlacklist(idCard, realName, reason, type, operatorId);
        return Result.success();
    }

    @PostMapping("/remove/{idCard}")
    @ApiOperation("移除黑名单")
    public Result<Void> remove(@PathVariable String idCard) {
        blacklistService.removeFromBlacklist(idCard);
        return Result.success();
    }

    @GetMapping("/{idCard}")
    @ApiOperation("获取黑名单详情")
    public Result<Blacklist> getDetail(@PathVariable String idCard) {
        return Result.success(blacklistService.getByIdCard(idCard));
    }
}
