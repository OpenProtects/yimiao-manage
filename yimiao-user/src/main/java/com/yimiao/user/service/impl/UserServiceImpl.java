package com.yimiao.user.service.impl;

import cn.hutool.core.util.IdcardUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yimiao.api.dto.LoginDTO;
import com.yimiao.api.dto.RegisterDTO;
import com.yimiao.api.vo.LoginVO;
import com.yimiao.common.constant.RedisKeyConstant;
import com.yimiao.common.core.ResultCode;
import com.yimiao.common.exception.BusinessException;
import com.yimiao.common.redis.RedisService;
import com.yimiao.common.util.JwtUtil;
import com.yimiao.user.entity.User;
import com.yimiao.user.mapper.UserMapper;
import com.yimiao.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final JwtUtil jwtUtil;
    private final RedisService redisService;

    public UserServiceImpl(JwtUtil jwtUtil, RedisService redisService) {
        this.jwtUtil = jwtUtil;
        this.redisService = redisService;
    }

    @Override
    public LoginVO login(LoginDTO dto) {
        User user = getByUsername(dto.getUsername());
        if (user == null) {
            user = getByPhone(dto.getUsername());
        }
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        
        if (!BCrypt.checkpw(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.USER_PASSWORD_ERROR);
        }
        
        if (user.getStatus() == 1) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }
        
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getUserType());
        
        redisService.set(RedisKeyConstant.USER_TOKEN + user.getId(), token, 24, TimeUnit.HOURS);
        
        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setTokenType("Bearer");
        vo.setExpiresIn(86400000L);
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setUserType(user.getUserType());
        
        log.info("用户登录成功: userId={}, username={}", user.getId(), user.getUsername());
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(RegisterDTO dto) {
        if (getByUsername(dto.getUsername()) != null) {
            throw new BusinessException(ResultCode.USER_EXISTS);
        }
        if (getByPhone(dto.getPhone()) != null) {
            throw new BusinessException(ResultCode.PHONE_EXISTS);
        }
        
        String smsKey = RedisKeyConstant.USER_SMS + dto.getPhone();
        String cachedCode = redisService.get(smsKey);
        if (cachedCode == null || !cachedCode.equals(dto.getSmsCode())) {
            throw new BusinessException(ResultCode.VERIFICATION_CODE_ERROR);
        }
        
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(BCrypt.hashpw(dto.getPassword()));
        user.setPhone(dto.getPhone());
        user.setUserType(0);
        user.setStatus(0);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        
        save(user);
        
        redisService.delete(smsKey);
        
        log.info("用户注册成功: userId={}, username={}", user.getId(), user.getUsername());
    }

    @Override
    public User getByUsername(String username) {
        return getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)
                .eq(User::getDeleted, 0));
    }

    @Override
    public User getByPhone(String phone) {
        return getOne(new LambdaQueryWrapper<User>()
                .eq(User::getPhone, phone)
                .eq(User::getDeleted, 0));
    }

    @Override
    public void updateLastLogin(Long userId, String ip) {
        User user = new User();
        user.setId(userId);
        user.setLastLoginTime(LocalDateTime.now());
        user.setLastLoginIp(ip);
        updateById(user);
    }

    @Override
    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        if (!BCrypt.checkpw(oldPassword, user.getPassword())) {
            throw new BusinessException(ResultCode.USER_PASSWORD_ERROR);
        }
        
        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setPassword(BCrypt.hashpw(newPassword));
        updateById(updateUser);
    }

    @Override
    public void resetPassword(String phone, String newPassword) {
        User user = getByPhone(phone);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setPassword(BCrypt.hashpw(newPassword));
        updateById(updateUser);
    }
}
