package com.yimiao.admin.controller;

import com.yimiao.admin.entity.Admin;
import com.yimiao.admin.service.AdminService;
import com.yimiao.common.core.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/admin")
@Api(tags = "管理员管理")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/{userId}")
    @ApiOperation("获取管理员信息")
    public Result<Admin> getByUserId(@PathVariable Long userId) {
        return Result.success(adminService.getByUserId(userId));
    }

    @GetMapping("/permission/check")
    @ApiOperation("检查权限")
    public Result<Boolean> checkPermission(
            @RequestParam Long userId,
            @RequestParam String permission) {
        return Result.success(adminService.hasPermission(userId, permission));
    }
}
