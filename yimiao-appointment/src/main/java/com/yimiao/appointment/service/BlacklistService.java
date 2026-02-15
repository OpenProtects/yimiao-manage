package com.yimiao.appointment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yimiao.appointment.entity.Blacklist;

public interface BlacklistService extends IService<Blacklist> {
    boolean isInBlacklist(String idCard);
    void addToBlacklist(String idCard, String realName, String reason, Integer type, Long operatorId);
    void removeFromBlacklist(String idCard);
    Blacklist getByIdCard(String idCard);
}
