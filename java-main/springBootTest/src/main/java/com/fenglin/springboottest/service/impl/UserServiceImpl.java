package com.fenglin.springboottest.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fenglin.springboottest.dto.LoginRequest;
import com.fenglin.springboottest.dto.LoginResponse;
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
        String username = request.getUsername().trim();

        // 1. 校验用户名是否已被录入
        Long usernameCount = baseMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (usernameCount != null && usernameCount > 0) {
            throw new BusinessException("夫君名讳已被录入，换一个吧");
        }

        // 2. 组装用户并加密密码（不收集邮箱、手机号等阳间信息）
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(username);                 // 默认昵称等于用户名
        user.setStatus(1);                          // 默认正常

        // createTime / updateTime 由 MyBatis-Plus 自动填充，deleted 默认 0
        baseMapper.insert(user);
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        String username = request.getAccount().trim();
        String password = request.getPassword();

        if (username.isEmpty()) {
            throw new BusinessException("夫君还不肯报名吗？");
        }

        // 1. 按夫君名讳查找用户（仅支持用户名登录）
        User user = baseMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));

        // 2. 校验账号状态（在密码比对之前，禁用账号不泄露密码对错）
        if (user != null && (user.getStatus() == null || user.getStatus() != 1)) {
            throw new BusinessException("此身已被封印，无法结契");
        }

        // 3. 校验用户与密码（BCrypt 比对；账号不存在与密码错误提示一致，避免名讳被试探）
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException("名讳或暗语不匹配，花娘不认这道婚约");
        }

        return LoginResponse.from(user);
    }
}
