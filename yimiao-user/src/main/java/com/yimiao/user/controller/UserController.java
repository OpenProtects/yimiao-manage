package com.yimiao.user.controller;

import com.yimiao.api.dto.LoginDTO;
import com.yimiao.api.dto.RegisterDTO;
import com.yimiao.api.vo.LoginVO;
import com.yimiao.common.core.Result;
import com.yimiao.user.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/user")
@Api(tags = "用户管理")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    @ApiOperation("用户登录")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto, HttpServletRequest request) {
        LoginVO vo = userService.login(dto);
        userService.updateLastLogin(vo.getUserId(), getClientIp(request));
        return Result.success(vo);
    }

    @PostMapping("/register")
    @ApiOperation("用户注册")
    public Result<Void> register(@Valid @RequestBody RegisterDTO dto) {
        userService.register(dto);
        return Result.success();
    }

    @PostMapping("/logout")
    @ApiOperation("用户登出")
    public Result<Void> logout(@RequestHeader("Authorization") String token) {
        return Result.success();
    }

    @GetMapping("/info")
    @ApiOperation("获取用户信息")
    public Result<?> getUserInfo(@RequestHeader("X-User-Id") Long userId) {
        return Result.success(userService.getById(userId));
    }

    @PutMapping("/password")
    @ApiOperation("修改密码")
    public Result<Void> updatePassword(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {
        userService.updatePassword(userId, oldPassword, newPassword);
        return Result.success();
    }

    @PostMapping("/password/reset")
    @ApiOperation("重置密码")
    public Result<Void> resetPassword(
            @RequestParam String phone,
            @RequestParam String newPassword,
            @RequestParam String smsCode) {
        userService.resetPassword(phone, newPassword);
        return Result.success();
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
