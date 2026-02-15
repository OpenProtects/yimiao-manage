package com.yimiao.appointment.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yimiao.api.dto.AppointmentDTO;
import com.yimiao.api.vo.AppointmentVO;
import com.yimiao.appointment.service.AppointmentService;
import com.yimiao.common.core.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/appointment")
@Api(tags = "预约管理")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping
    @ApiOperation("创建预约")
    public Result<Long> create(@Valid @RequestBody AppointmentDTO dto) {
        return Result.success(appointmentService.createAppointment(dto));
    }

    @PostMapping("/cancel/{id}")
    @ApiOperation("取消预约")
    public Result<Void> cancel(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(required = false) String reason) {
        appointmentService.cancelAppointment(id, userId, reason);
        return Result.success();
    }

    @GetMapping("/{id}")
    @ApiOperation("获取预约详情")
    public Result<AppointmentVO> getDetail(@PathVariable Long id) {
        return Result.success(appointmentService.getDetail(id));
    }

    @GetMapping("/page")
    @ApiOperation("分页查询预约列表")
    public Result<Page<AppointmentVO>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer status) {
        return Result.success(appointmentService.pageList(pageNum, pageSize, userId, status));
    }

    @GetMapping("/my")
    @ApiOperation("获取我的预约列表")
    public Result<List<AppointmentVO>> myAppointments(@RequestHeader("X-User-Id") Long userId) {
        return Result.success(appointmentService.listByUserId(userId));
    }

    @PostMapping("/verify/{id}")
    @ApiOperation("核销预约")
    public Result<Void> verify(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long operatorId) {
        appointmentService.verifyAppointment(id, operatorId);
        return Result.success();
    }

    @GetMapping("/order-no/{orderNo}")
    @ApiOperation("根据订单号查询")
    public Result<AppointmentVO> getByOrderNo(@PathVariable String orderNo) {
        return Result.success(appointmentService.getDetail(
                appointmentService.getByOrderNo(orderNo).getId()));
    }
}
