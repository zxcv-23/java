package com.fenglin.springboottest.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fenglin.springboottest.common.Result;
import com.fenglin.springboottest.dto.DatingCandidateVO;
import com.fenglin.springboottest.entity.User;
import com.fenglin.springboottest.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * 相亲阁（/dating.html）的数据来源：公开、只读、脱敏的注册用户列表。
 *
 * <p>只查 status=1 的正常账号，只投影展示必需字段；MyBatis-Plus 的
 * {@code @TableLogic} 会自动过滤已删除账号。接口不受 JwtInterceptor 保护
 * （浏览嘉宾无需登录），因此这里绝不能返回 User 实体本身。</p>
 */
@RestController
@RequestMapping("/api/dating")
public class DatingController {

    /** 单次返回上限，防止一次性把整张用户表拉走 */
    private static final int MAX_LIMIT = 100;
    private static final int DEFAULT_LIMIT = 60;

    @Autowired
    private UserMapper userMapper;

    /**
     * 嘉宾列表。
     *
     * @param keyword 昵称 / 简介模糊匹配，可为空
     * @param gender  1-男 2-女，其余值视为不筛选
     * @param limit   返回条数， clamping 到 1~100
     */
    @GetMapping("/candidates")
    public Result<List<DatingCandidateVO>> candidates(
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(required = false) Integer gender,
            @RequestParam(required = false, defaultValue = "60") Integer limit) {

        int size = limit == null ? DEFAULT_LIMIT : Math.min(Math.max(limit, 1), MAX_LIMIT);
        String raw = keyword == null ? "" : keyword.trim();
        // lambda 捕获要求 effectively final，所以截断后一次成型
        final String kw = raw.length() > 50 ? raw.substring(0, 50) : raw;

        LambdaQueryWrapper<User> query = new LambdaQueryWrapper<>();
        query.select(User::getId,
                     User::getNickname,
                     User::getAvatar,
                     User::getGender,
                     User::getBio,
                     User::getCreateTime)
             .eq(User::getStatus, 1);

        if (StringUtils.hasText(kw)) {
            // 预编译参数，不存在 SQL 注入；% / _ 仅作为通配语义，无安全风险
            query.and(w -> w.like(User::getNickname, kw).or().like(User::getBio, kw));
        }
        if (gender != null && (gender == 1 || gender == 2)) {
            query.eq(User::getGender, gender);
        }
        query.orderByDesc(User::getId).last("LIMIT " + size);

        List<User> users = userMapper.selectList(query);
        List<DatingCandidateVO> list = new ArrayList<>(users.size());
        for (User user : users) {
            list.add(toVO(user));
        }
        return Result.success("姻缘簿已展开 ♥", list);
    }

    private DatingCandidateVO toVO(User user) {
        DatingCandidateVO vo = new DatingCandidateVO();
        vo.setId(user.getId());
        vo.setNickname(StringUtils.hasText(user.getNickname()) ? user.getNickname() : "匿名有缘人");
        vo.setAvatar(StringUtils.hasText(user.getAvatar()) ? user.getAvatar() : "");

        int gender = user.getGender() == null ? 0 : user.getGender();
        vo.setGender(gender);
        vo.setGenderLabel(gender == 1 ? "公子" : gender == 2 ? "姑娘" : "未留");
        vo.setBio(StringUtils.hasText(user.getBio()) ? user.getBio() : "这位有缘人尚未留下只言片语，等你来问～");

        String joinedAt = "";
        boolean fresh = false;
        if (user.getCreateTime() != null) {
            LocalDate day = user.getCreateTime().toLocalDate();
            joinedAt = day.toString();
            fresh = ChronoUnit.DAYS.between(day, LocalDate.now()) <= 7;
        }
        vo.setJoinedAt(joinedAt);
        vo.setTag(fresh ? "新入阁" : "姻缘簿在册");
        vo.setDemo(false);
        return vo;
    }
}
