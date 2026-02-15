package com.yimiao.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yimiao.api.dto.LoginDTO;
import com.yimiao.api.dto.RegisterDTO;
import com.yimiao.api.vo.LoginVO;
import com.yimiao.user.entity.User;

public interface UserService extends IService<User> {
    LoginVO login(LoginDTO dto);
    void register(RegisterDTO dto);
    User getByUsername(String username);
    User getByPhone(String phone);
    void updateLastLogin(Long userId, String ip);
    void updatePassword(Long userId, String oldPassword, String newPassword);
    void resetPassword(String phone, String newPassword);
}
