package com.yimiao.user.controller;

import com.yimiao.api.dto.VaccineeDTO;
import com.yimiao.api.vo.VaccineeVO;
import com.yimiao.common.core.Result;
import com.yimiao.user.service.VaccineeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/vaccinee")
@Api(tags = "接种人管理")
public class VaccineeController {

    private final VaccineeService vaccineeService;

    public VaccineeController(VaccineeService vaccineeService) {
        this.vaccineeService = vaccineeService;
    }

    @GetMapping("/list")
    @ApiOperation("获取接种人列表")
    public Result<List<VaccineeVO>> list(@RequestHeader("X-User-Id") Long userId) {
        return Result.success(vaccineeService.listByUserId(userId));
    }

    @GetMapping("/{id}")
    @ApiOperation("获取接种人详情")
    public Result<VaccineeVO> getDetail(@PathVariable Long id) {
        return Result.success(vaccineeService.getDetail(id));
    }

    @PostMapping
    @ApiOperation("添加接种人")
    public Result<Long> add(@Valid @RequestBody VaccineeDTO dto) {
        return Result.success(vaccineeService.addVaccinee(dto));
    }

    @PutMapping
    @ApiOperation("更新接种人")
    public Result<Void> update(@Valid @RequestBody VaccineeDTO dto) {
        vaccineeService.updateVaccinee(dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除接种人")
    public Result<Void> delete(@PathVariable Long id, @RequestHeader("X-User-Id") Long userId) {
        vaccineeService.deleteVaccinee(id, userId);
        return Result.success();
    }

    @PutMapping("/{id}/default")
    @ApiOperation("设置默认接种人")
    public Result<Void> setDefault(@PathVariable Long id, @RequestHeader("X-User-Id") Long userId) {
        vaccineeService.setDefault(id, userId);
        return Result.success();
    }
}
