package com.yimiao.vaccine.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yimiao.api.dto.SiteDTO;
import com.yimiao.api.vo.SiteVO;
import com.yimiao.common.core.ResultCode;
import com.yimiao.common.exception.BusinessException;
import com.yimiao.vaccine.entity.Site;
import com.yimiao.vaccine.mapper.SiteMapper;
import com.yimiao.vaccine.service.SiteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SiteServiceImpl extends ServiceImpl<SiteMapper, Site> implements SiteService {

    @Override
    public Page<SiteVO> pageList(int pageNum, int pageSize, String name, String region, Integer status) {
        Page<Site> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Site> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(name)) {
            wrapper.like(Site::getName, name);
        }
        if (StringUtils.hasText(region)) {
            wrapper.eq(Site::getRegion, region);
        }
        if (status != null) {
            wrapper.eq(Site::getStatus, status);
        }
        wrapper.eq(Site::getDeleted, 0);
        wrapper.orderByDesc(Site::getCreateTime);
        
        Page<Site> result = page(page, wrapper);
        
        Page<SiteVO> voPage = new Page<>();
        voPage.setCurrent(result.getCurrent());
        voPage.setSize(result.getSize());
        voPage.setTotal(result.getTotal());
        voPage.setRecords(result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList()));
        
        return voPage;
    }

    @Override
    public SiteVO getDetail(Long id) {
        Site site = getById(id);
        if (site == null || site.getDeleted() == 1) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "接种点不存在");
        }
        return convertToVO(site);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addSite(SiteDTO dto) {
        Site existing = getOne(new LambdaQueryWrapper<Site>()
                .eq(Site::getCode, dto.getCode())
                .eq(Site::getDeleted, 0));
        if (existing != null) {
            throw new BusinessException("接种点编码已存在");
        }
        
        Site site = new Site();
        BeanUtil.copyProperties(dto, site);
        save(site);
        
        log.info("添加接种点成功: id={}, name={}", site.getId(), site.getName());
        return site.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSite(SiteDTO dto) {
        Site site = getById(dto.getId());
        if (site == null || site.getDeleted() == 1) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "接种点不存在");
        }
        
        BeanUtil.copyProperties(dto, site);
        updateById(site);
        
        log.info("更新接种点成功: id={}", dto.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSite(Long id) {
        Site site = getById(id);
        if (site == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "接种点不存在");
        }
        
        LambdaUpdateWrapper<Site> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Site::getId, id)
                     .set(Site::getDeleted, 1);
        update(updateWrapper);
        
        log.info("删除接种点成功: id={}", id);
    }

    @Override
    public List<SiteVO> listAll() {
        List<Site> list = list(new LambdaQueryWrapper<Site>()
                .eq(Site::getStatus, 0)
                .eq(Site::getDeleted, 0)
                .orderByAsc(Site::getRegion, Site::getName));
        
        return list.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public List<SiteVO> listByRegion(String region) {
        List<Site> list = list(new LambdaQueryWrapper<Site>()
                .eq(Site::getRegion, region)
                .eq(Site::getStatus, 0)
                .eq(Site::getDeleted, 0)
                .orderByAsc(Site::getName));
        
        return list.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    private SiteVO convertToVO(Site site) {
        SiteVO vo = new SiteVO();
        BeanUtil.copyProperties(site, vo);
        return vo;
    }
}
