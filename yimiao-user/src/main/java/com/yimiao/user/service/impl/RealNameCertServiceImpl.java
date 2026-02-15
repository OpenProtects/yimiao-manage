package com.yimiao.user.service.impl;

import cn.hutool.core.util.IdcardUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yimiao.api.dto.RealNameCertDTO;
import com.yimiao.common.core.ResultCode;
import com.yimiao.common.exception.BusinessException;
import com.yimiao.user.entity.RealNameCert;
import com.yimiao.user.mapper.RealNameCertMapper;
import com.yimiao.user.service.RealNameCertService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
public class RealNameCertServiceImpl extends ServiceImpl<RealNameCertMapper, RealNameCert> implements RealNameCertService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void certify(RealNameCertDTO dto) {
        if (!IdcardUtil.isValidCard(dto.getIdCard())) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "身份证号格式不正确");
        }
        
        RealNameCert existing = getOne(new LambdaQueryWrapper<RealNameCert>()
                .eq(RealNameCert::getIdCard, dto.getIdCard())
                .eq(RealNameCert::getDeleted, 0));
        if (existing != null) {
            throw new BusinessException(ResultCode.ID_CARD_EXISTS);
        }
        
        RealNameCert userCert = getByUserId(dto.getUserId());
        if (userCert != null && userCert.getStatus() == 1) {
            throw new BusinessException("已完成实名认证");
        }
        
        RealNameCert cert = new RealNameCert();
        cert.setUserId(dto.getUserId());
        cert.setRealName(dto.getRealName());
        cert.setIdCard(dto.getIdCard());
        cert.setStatus(1);
        cert.setCertTime(LocalDateTime.now());
        
        if (userCert != null) {
            cert.setId(userCert.getId());
            updateById(cert);
        } else {
            save(cert);
        }
        
        log.info("实名认证成功: userId={}, realName={}", dto.getUserId(), dto.getRealName());
    }

    @Override
    public RealNameCert getByUserId(Long userId) {
        return getOne(new LambdaQueryWrapper<RealNameCert>()
                .eq(RealNameCert::getUserId, userId)
                .eq(RealNameCert::getDeleted, 0));
    }

    @Override
    public boolean isCertified(Long userId) {
        RealNameCert cert = getByUserId(userId);
        return cert != null && cert.getStatus() == 1;
    }
}
