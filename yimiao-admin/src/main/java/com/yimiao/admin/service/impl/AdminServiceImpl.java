package com.yimiao.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yimiao.admin.entity.Admin;
import com.yimiao.admin.mapper.AdminMapper;
import com.yimiao.admin.service.AdminService;
import org.springframework.stereotype.Service;

@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements AdminService {

    @Override
    public Admin getByUserId(Long userId) {
        return getOne(new LambdaQueryWrapper<Admin>()
                .eq(Admin::getUserId, userId)
                .eq(Admin::getDeleted, 0));
    }

    @Override
    public boolean hasPermission(Long userId, String permission) {
        Admin admin = getByUserId(userId);
        if (admin == null) {
            return false;
        }
        if ("super_admin".equals(admin.getRole())) {
            return true;
        }
        String permissions = admin.getPermissions();
        if (permissions == null || permissions.isEmpty()) {
            return false;
        }
        return permissions.contains(permission);
    }
}
