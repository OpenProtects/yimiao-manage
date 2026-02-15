package com.yimiao.user.controller;

import com.yimiao.api.dto.RealNameCertDTO;
import com.yimiao.common.core.Result;
import com.yimiao.user.entity.RealNameCert;
import com.yimiao.user.service.RealNameCertService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/cert")
@Api(tags = "实名认证")
public class RealNameCertController {

    private final RealNameCertService realNameCertService;

    public RealNameCertController(RealNameCertService realNameCertService) {
        this.realNameCertService = realNameCertService;
    }

    @PostMapping("/apply")
    @ApiOperation("申请实名认证")
    public Result<Void> certify(@Valid @RequestBody RealNameCertDTO dto) {
        realNameCertService.certify(dto);
        return Result.success();
    }

    @GetMapping("/status")
    @ApiOperation("获取认证状态")
    public Result<RealNameCert> getStatus(@RequestHeader("X-User-Id") Long userId) {
        return Result.success(realNameCertService.getByUserId(userId));
    }

    @GetMapping("/check")
    @ApiOperation("检查是否已认证")
    public Result<Boolean> checkCertified(@RequestHeader("X-User-Id") Long userId) {
        return Result.success(realNameCertService.isCertified(userId));
    }
}
