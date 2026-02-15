package com.yimiao.vaccine.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yimiao.api.dto.VaccineDTO;
import com.yimiao.api.vo.VaccineVO;
import com.yimiao.common.core.Result;
import com.yimiao.vaccine.service.VaccineService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/vaccine")
@Api(tags = "疫苗管理")
public class VaccineController {

    private final VaccineService vaccineService;

    public VaccineController(VaccineService vaccineService) {
        this.vaccineService = vaccineService;
    }

    @GetMapping("/page")
    @ApiOperation("分页查询疫苗列表")
    public Result<Page<VaccineVO>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer status) {
        return Result.success(vaccineService.pageList(pageNum, pageSize, name, type, status));
    }

    @GetMapping("/{id}")
    @ApiOperation("获取疫苗详情")
    public Result<VaccineVO> getDetail(@PathVariable Long id) {
        return Result.success(vaccineService.getDetail(id));
    }

    @PostMapping
    @ApiOperation("添加疫苗")
    public Result<Long> add(@Valid @RequestBody VaccineDTO dto) {
        return Result.success(vaccineService.addVaccine(dto));
    }

    @PutMapping
    @ApiOperation("更新疫苗")
    public Result<Void> update(@Valid @RequestBody VaccineDTO dto) {
        vaccineService.updateVaccine(dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除疫苗")
    public Result<Void> delete(@PathVariable Long id) {
        vaccineService.deleteVaccine(id);
        return Result.success();
    }

    @GetMapping("/available")
    @ApiOperation("获取可用疫苗列表")
    public Result<List<VaccineVO>> listAvailable() {
        return Result.success(vaccineService.listAvailable());
    }

    @GetMapping("/check-age")
    @ApiOperation("检查年龄是否符合接种要求")
    public Result<Boolean> checkAge(@RequestParam Long vaccineId, @RequestParam Integer age) {
        return Result.success(vaccineService.checkAgeRange(vaccineId, age));
    }
}
