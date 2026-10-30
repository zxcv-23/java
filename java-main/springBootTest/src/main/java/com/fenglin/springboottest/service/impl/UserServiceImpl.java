package com.fenglin.springboottest.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fenglin.springboottest.dto.LoginRequest;
import com.fenglin.springboottest.dto.LoginResponse;
import com.fenglin.springboottest.dto.PasswordChangeRequest;
import com.fenglin.springboottest.dto.ProfileUpdateRequest;
import com.fenglin.springboottest.dto.ProfileVO;
import com.fenglin.springboottest.dto.RegisterRequest;
import com.fenglin.springboottest.entity.User;
import com.fenglin.springboottest.exception.BusinessException;
import com.fenglin.springboottest.mapper.UserMapper;
import com.fenglin.springboottest.security.JwtService;
import com.fenglin.springboottest.service.FileUploadService;
import com.fenglin.springboottest.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 用户服务实现
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    /** BCrypt 密码加密器（带随机盐，每次加密结果不同，安全性高） */
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    private JwtService jwtService;

    @Autowired
    private FileUploadService fileUploadService;

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

        LoginResponse resp = LoginResponse.from(user);
        // 签发 JWT，前端后续请求通过 Authorization 头携带；写入令牌版本号以支持主动失效
        resp.setToken(jwtService.generateToken(user.getId(), user.getUsername(), user.getTokenVersion()));
        return resp;
    }

    @Override
    public ProfileVO getProfile(Long userId) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException("未找到该用户");
        }
        return ProfileVO.from(user);
    }

    @Override
    public void updateProfile(Long userId, ProfileUpdateRequest request) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException("未找到该用户");
        }

        // ---------- 唯一性校验（排除自己） ----------
        if (StringUtils.hasText(request.getEmail())) {
            String email = request.getEmail().trim();
            long cnt = baseMapper.selectCount(new LambdaQueryWrapper<User>()
                    .eq(User::getEmail, email)
                    .ne(User::getId, userId));
            if (cnt > 0) {
                throw new BusinessException("该邮箱已被其他夫君占用");
            }
            user.setEmail(email);
        }
        if (StringUtils.hasText(request.getPhone())) {
            String phone = request.getPhone().trim();
            long cnt = baseMapper.selectCount(new LambdaQueryWrapper<User>()
                    .eq(User::getPhone, phone)
                    .ne(User::getId, userId));
            if (cnt > 0) {
                throw new BusinessException("该手机号已被其他夫君占用");
            }
            user.setPhone(phone);
        }

        // ---------- 其余字段 ----------
        if (StringUtils.hasText(request.getNickname())) {
            user.setNickname(request.getNickname().trim());
        }
        if (request.getGender() != null) {
            if (request.getGender() < 0 || request.getGender() > 2) {
                throw new BusinessException("性别取值不合法（0-未知 1-男 2-女）");
            }
            user.setGender(request.getGender());
        }
        if (StringUtils.hasText(request.getBio())) {
            user.setBio(request.getBio().trim());
        }

        updateById(user);
    }

    @Override
    public void updateAvatar(Long userId, String avatarUrl) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException("未找到该用户");
        }
        String oldAvatar = user.getAvatar();
        user.setAvatar(avatarUrl);
        updateById(user);

        // 清理旧头像文件，避免磁盘残留（更换头像时才有旧文件）
        if (oldAvatar != null && !oldAvatar.equals(avatarUrl)) {
            fileUploadService.deleteImageByUrl(oldAvatar);
        }
    }

    @Override
    public String changePassword(Long userId, PasswordChangeRequest request) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException("未找到该用户");
        }

        // 校验原密码（账号不存在与密码错误提示一致，避免名讳被试探）
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException("原密码不正确");
        }
        // 新密码不能与旧密码相同
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new BusinessException("新密码不能与旧密码相同");
        }
        if (request.getNewPassword().length() < 6) {
            throw new BusinessException("新密码至少 6 位");
        }

        // 自增令牌版本号：旧 token 立即失效
        Long newVer = (user.getTokenVersion() == null ? 0L : user.getTokenVersion()) + 1;
        user.setTokenVersion(newVer);
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        updateById(user);

        // 返回携带新版本号的新 token，前端无缝续期登录态
        return jwtService.generateToken(user.getId(), user.getUsername(), newVer);
    }

    @Override
    public void logout(Long userId) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException("未找到该用户");
        }
        // 自增令牌版本号：当前 token 立即失效（多端会一并失效，符合演示预期）
        Long newVer = (user.getTokenVersion() == null ? 0L : user.getTokenVersion()) + 1;
        user.setTokenVersion(newVer);
        updateById(user);
    }
}
