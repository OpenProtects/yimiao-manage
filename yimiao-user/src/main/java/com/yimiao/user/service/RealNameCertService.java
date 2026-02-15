package com.yimiao.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yimiao.api.dto.RealNameCertDTO;
import com.yimiao.user.entity.RealNameCert;

public interface RealNameCertService extends IService<RealNameCert> {
    void certify(RealNameCertDTO dto);
    RealNameCert getByUserId(Long userId);
    boolean isCertified(Long userId);
}
