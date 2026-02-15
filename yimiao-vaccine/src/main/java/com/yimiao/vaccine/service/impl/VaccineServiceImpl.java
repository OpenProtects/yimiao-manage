package com.yimiao.vaccine.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yimiao.api.dto.VaccineDTO;
import com.yimiao.api.vo.VaccineVO;
import com.yimiao.common.core.ResultCode;
import com.yimiao.common.exception.BusinessException;
import com.yimiao.vaccine.entity.Vaccine;
import com.yimiao.vaccine.entity.VaccineStock;
import com.yimiao.vaccine.mapper.VaccineMapper;
import com.yimiao.vaccine.service.VaccineService;
import com.yimiao.vaccine.service.VaccineStockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class VaccineServiceImpl extends ServiceImpl<VaccineMapper, Vaccine> implements VaccineService {

    private final VaccineStockService vaccineStockService;

    public VaccineServiceImpl(VaccineStockService vaccineStockService) {
        this.vaccineStockService = vaccineStockService;
    }

    @Override
    public Page<VaccineVO> pageList(int pageNum, int pageSize, String name, String type, Integer status) {
        Page<Vaccine> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Vaccine> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(name)) {
            wrapper.like(Vaccine::getName, name);
        }
        if (StringUtils.hasText(type)) {
            wrapper.eq(Vaccine::getType, type);
        }
        if (status != null) {
            wrapper.eq(Vaccine::getStatus, status);
        }
        wrapper.eq(Vaccine::getDeleted, 0);
        wrapper.orderByDesc(Vaccine::getCreateTime);
        
        Page<Vaccine> result = page(page, wrapper);
        
        Page<VaccineVO> voPage = new Page<>();
        voPage.setCurrent(result.getCurrent());
        voPage.setSize(result.getSize());
        voPage.setTotal(result.getTotal());
        voPage.setRecords(result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList()));
        
        return voPage;
    }

    @Override
    public VaccineVO getDetail(Long id) {
        Vaccine vaccine = getById(id);
        if (vaccine == null || vaccine.getDeleted() == 1) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "疫苗不存在");
        }
        return convertToVO(vaccine);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addVaccine(VaccineDTO dto) {
        Vaccine existing = getOne(new LambdaQueryWrapper<Vaccine>()
                .eq(Vaccine::getCode, dto.getCode())
                .eq(Vaccine::getDeleted, 0));
        if (existing != null) {
            throw new BusinessException("疫苗编码已存在");
        }
        
        Vaccine vaccine = new Vaccine();
        BeanUtil.copyProperties(dto, vaccine);
        save(vaccine);
        
        log.info("添加疫苗成功: id={}, name={}", vaccine.getId(), vaccine.getName());
        return vaccine.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateVaccine(VaccineDTO dto) {
        Vaccine vaccine = getById(dto.getId());
        if (vaccine == null || vaccine.getDeleted() == 1) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "疫苗不存在");
        }
        
        BeanUtil.copyProperties(dto, vaccine);
        updateById(vaccine);
        
        log.info("更新疫苗成功: id={}", dto.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteVaccine(Long id) {
        Vaccine vaccine = getById(id);
        if (vaccine == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "疫苗不存在");
        }
        
        LambdaUpdateWrapper<Vaccine> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Vaccine::getId, id)
                     .set(Vaccine::getDeleted, 1);
        update(updateWrapper);
        
        log.info("删除疫苗成功: id={}", id);
    }

    @Override
    public List<VaccineVO> listAvailable() {
        List<Vaccine> list = list(new LambdaQueryWrapper<Vaccine>()
                .eq(Vaccine::getStatus, 0)
                .eq(Vaccine::getDeleted, 0)
                .orderByAsc(Vaccine::getName));
        
        return list.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public boolean checkAgeRange(Long vaccineId, Integer age) {
        Vaccine vaccine = getById(vaccineId);
        if (vaccine == null) {
            return false;
        }
        return age >= vaccine.getMinAge() && age <= vaccine.getMaxAge();
    }

    private VaccineVO convertToVO(Vaccine vaccine) {
        VaccineVO vo = new VaccineVO();
        BeanUtil.copyProperties(vaccine, vo);
        return vo;
    }
}
