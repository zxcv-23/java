package com.fenglin.springboottest.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fenglin.springboottest.dto.RegisterRequest;
import com.fenglin.springboottest.entity.User;
import com.fenglin.springboottest.exception.BusinessException;
import com.fenglin.springboottest.mapper.UserMapper;
import com.fenglin.springboottest.service.UserService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 用户服务实现
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    /** BCrypt 密码加密器（带随机盐，每次加密结果不同，安全性高） */
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public void register(RegisterRequest request) {
        // 1. 校验用户名是否已被录入
        Long usernameCount = baseMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername()));
        if (usernameCount != null && usernameCount > 0) {
            throw new BusinessException("夫君名讳已被录入，换一个吧");
        }

        // 2. 校验邮箱是否已结契
        Long emailCount = baseMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getEmail, request.getEmail()));
        if (emailCount != null && emailCount > 0) {
            throw new BusinessException("此冥府信笺已结契，请直接登录");
        }

        // 3. 组装用户并加密密码
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getUsername());   // 默认昵称等于用户名
        user.setStatus(1);                          // 默认正常

        // createTime / updateTime 由 MyBatis-Plus 自动填充，deleted 默认 0
        baseMapper.insert(user);
    }
}
