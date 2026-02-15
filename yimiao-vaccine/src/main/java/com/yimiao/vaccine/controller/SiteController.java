package com.yimiao.vaccine.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yimiao.api.dto.SiteDTO;
import com.yimiao.api.vo.SiteVO;
import com.yimiao.common.core.Result;
import com.yimiao.vaccine.service.SiteService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/site")
@Api(tags = "接种点管理")
public class SiteController {

    private final SiteService siteService;

    public SiteController(SiteService siteService) {
        this.siteService = siteService;
    }

    @GetMapping("/page")
    @ApiOperation("分页查询接种点列表")
    public Result<Page<SiteVO>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) Integer status) {
        return Result.success(siteService.pageList(pageNum, pageSize, name, region, status));
    }

    @GetMapping("/{id}")
    @ApiOperation("获取接种点详情")
    public Result<SiteVO> getDetail(@PathVariable Long id) {
        return Result.success(siteService.getDetail(id));
    }

    @PostMapping
    @ApiOperation("添加接种点")
    public Result<Long> add(@Valid @RequestBody SiteDTO dto) {
        return Result.success(siteService.addSite(dto));
    }

    @PutMapping
    @ApiOperation("更新接种点")
    public Result<Void> update(@Valid @RequestBody SiteDTO dto) {
        siteService.updateSite(dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除接种点")
    public Result<Void> delete(@PathVariable Long id) {
        siteService.deleteSite(id);
        return Result.success();
    }

    @GetMapping("/all")
    @ApiOperation("获取所有接种点列表")
    public Result<List<SiteVO>> listAll() {
        return Result.success(siteService.listAll());
    }

    @GetMapping("/region/{region}")
    @ApiOperation("根据区域获取接种点列表")
    public Result<List<SiteVO>> listByRegion(@PathVariable String region) {
        return Result.success(siteService.listByRegion(region));
    }
}
