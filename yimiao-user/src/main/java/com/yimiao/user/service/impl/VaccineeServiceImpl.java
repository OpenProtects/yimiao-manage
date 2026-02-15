package com.yimiao.user.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdcardUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yimiao.api.dto.VaccineeDTO;
import com.yimiao.api.vo.VaccineeVO;
import com.yimiao.common.core.ResultCode;
import com.yimiao.common.exception.BusinessException;
import com.yimiao.user.entity.Vaccinee;
import com.yimiao.user.mapper.VaccineeMapper;
import com.yimiao.user.service.VaccineeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class VaccineeServiceImpl extends ServiceImpl<VaccineeMapper, Vaccinee> implements VaccineeService {

    @Override
    public List<VaccineeVO> listByUserId(Long userId) {
        List<Vaccinee> list = list(new LambdaQueryWrapper<Vaccinee>()
                .eq(Vaccinee::getUserId, userId)
                .eq(Vaccinee::getDeleted, 0)
                .orderByDesc(Vaccinee::getIsDefault)
                .orderByDesc(Vaccinee::getCreateTime));
        
        return list.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public VaccineeVO getDetail(Long id) {
        Vaccinee vaccinee = getById(id);
        if (vaccinee == null || vaccinee.getDeleted() == 1) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "接种人不存在");
        }
        return convertToVO(vaccinee);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addVaccinee(VaccineeDTO dto) {
        if (!IdcardUtil.isValidCard(dto.getIdCard())) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "身份证号格式不正确");
        }
        
        Vaccinee existing = getByIdCard(dto.getIdCard());
        if (existing != null) {
            throw new BusinessException(ResultCode.ID_CARD_EXISTS);
        }
        
        Vaccinee vaccinee = new Vaccinee();
        BeanUtil.copyProperties(dto, vaccinee);
        vaccinee.setCertStatus(1);
        
        if (Boolean.TRUE.equals(dto.getIsDefault())) {
            clearDefaultFlag(dto.getUserId());
        }
        
        save(vaccinee);
        
        log.info("添加接种人成功: id={}, userId={}", vaccinee.getId(), dto.getUserId());
        return vaccinee.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateVaccinee(VaccineeDTO dto) {
        Vaccinee vaccinee = getById(dto.getId());
        if (vaccinee == null || vaccinee.getDeleted() == 1) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "接种人不存在");
        }
        
        if (!vaccinee.getUserId().equals(dto.getUserId())) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权限修改");
        }
        
        if (Boolean.TRUE.equals(dto.getIsDefault())) {
            clearDefaultFlag(dto.getUserId());
        }
        
        BeanUtil.copyProperties(dto, vaccinee);
        updateById(vaccinee);
        
        log.info("更新接种人成功: id={}", dto.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteVaccinee(Long id, Long userId) {
        Vaccinee vaccinee = getById(id);
        if (vaccinee == null || vaccinee.getDeleted() == 1) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "接种人不存在");
        }
        
        if (!vaccinee.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权限删除");
        }
        
        vaccinee.setDeleted(1);
        updateById(vaccinee);
        
        log.info("删除接种人成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setDefault(Long id, Long userId) {
        Vaccinee vaccinee = getById(id);
        if (vaccinee == null || vaccinee.getDeleted() == 1) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "接种人不存在");
        }
        
        if (!vaccinee.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权限操作");
        }
        
        clearDefaultFlag(userId);
        
        vaccinee.setIsDefault(true);
        updateById(vaccinee);
        
        log.info("设置默认接种人成功: id={}", id);
    }

    @Override
    public Vaccinee getByIdCard(String idCard) {
        return getOne(new LambdaQueryWrapper<Vaccinee>()
                .eq(Vaccinee::getIdCard, idCard)
                .eq(Vaccinee::getDeleted, 0));
    }

    private void clearDefaultFlag(Long userId) {
        update(new LambdaUpdateWrapper<Vaccinee>()
                .eq(Vaccinee::getUserId, userId)
                .eq(Vaccinee::getDeleted, 0)
                .set(Vaccinee::getIsDefault, false));
    }

    private VaccineeVO convertToVO(Vaccinee vaccinee) {
        VaccineeVO vo = new VaccineeVO();
        BeanUtil.copyProperties(vaccinee, vo);
        
        String maskedIdCard = vaccinee.getIdCard().replaceAll("(\\d{4})\\d{10}(\\d{4})", "$1**********$2");
        vo.setIdCard(maskedIdCard);
        
        if (vaccinee.getBirthDate() != null) {
            int age = Period.between(vaccinee.getBirthDate(), LocalDate.now()).getYears();
            vo.setAge(age);
        }
        
        return vo;
    }
}
