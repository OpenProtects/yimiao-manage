package com.yimiao.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yimiao.admin.entity.Admin;

public interface AdminService extends IService<Admin> {
    Admin getByUserId(Long userId);
    boolean hasPermission(Long userId, String permission);
}
