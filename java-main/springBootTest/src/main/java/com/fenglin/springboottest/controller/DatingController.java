package com.fenglin.springboottest.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fenglin.springboottest.common.Result;
import com.fenglin.springboottest.dto.DatingCandidateVO;
import com.fenglin.springboottest.dto.DatingContactInfo;
import com.fenglin.springboottest.dto.DatingConversationVO;
import com.fenglin.springboottest.dto.DatingMessageRequest;
import com.fenglin.springboottest.dto.DatingMessageVO;
import com.fenglin.springboottest.dto.DatingProfileVO;
import com.fenglin.springboottest.dto.DatingWorkRequest;
import com.fenglin.springboottest.dto.DatingWorkVO;
import com.fenglin.springboottest.entity.User;
import com.fenglin.springboottest.mapper.UserMapper;
import com.fenglin.springboottest.security.JwtInterceptor;
import com.fenglin.springboottest.security.OptionalJwtInterceptor;
import com.fenglin.springboottest.service.DatingService;
import com.fenglin.springboottest.service.FileUploadService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 姻缘阁（/dating.html）的全部接口：嘉宾列表、嘉宾主页、作品集、点赞、私信、联系方式。
 *
 * <p><strong>两组路径，鉴权方式不同：</strong></p>
 * <ul>
 *   <li>{@code /api/dating/**}（浏览类）：游客可访问，登录后通过
 *       {@link OptionalJwtInterceptor} 识别身份，用于「我点过赞没有」「联系方式能不能看」</li>
 *   <li>{@code /api/dating/me/**}（写操作）：由严格版 {@link JwtInterceptor} 保护，
 *       没有合法 token 直接 401，userId 一律从 token 取，前端传不进来</li>
 * </ul>
 *
 * <p>返回值全部是脱敏后的 VO：浏览接口绝不返回 User 实体本身，
 * 手机号 / 邮箱 / 登录用户名等注册信息不会出现在公开页面上。</p>
 */
@RestController
@RequestMapping("/api/dating")
public class DatingController {

    /** 单次返回上限，防止一次性把整张用户表拉走 */
    private static final int MAX_LIMIT = 100;
    private static final int DEFAULT_LIMIT = 60;

    /** 单个用户最多上传的作品数量，防止刷库 */
    private static final int MAX_WORKS_PER_USER = 60;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DatingService datingService;

    @Autowired
    private FileUploadService fileUploadService;

    // ==================== 浏览类（游客可访问） ====================

    /**
     * 嘉宾列表。
     *
     * @param keyword 昵称 / 简介模糊匹配，可为空
     * @param gender  1-男 2-女，其余值视为不筛选
     * @param limit   返回条数，clamping 到 1~100
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

    /**
     * 嘉宾主页：资料 + 作品集 + 联系方式可见性。
     * 未登录也能看（联系方式隐藏），登录后额外带上点赞状态。
     */
    @GetMapping("/users/{id}")
    public Result<DatingProfileVO> userProfile(
            @PathVariable Long id,
            @RequestAttribute(value = OptionalJwtInterceptor.ATTR_USER_ID, required = false) Long viewerId) {
        return Result.success(datingService.profile(id, viewerId));
    }

    /** 全站公开作品流（作品集总览） */
    @GetMapping("/works")
    public Result<List<DatingWorkVO>> works(
            @RequestParam(required = false, defaultValue = "30") Integer limit,
            @RequestAttribute(value = OptionalJwtInterceptor.ATTR_USER_ID, required = false) Long viewerId) {
        return Result.success(datingService.feed(limit == null ? 30 : limit, viewerId));
    }

    /** 作品详情（浏览 +1） */
    @GetMapping("/works/{id}")
    public Result<DatingWorkVO> workDetail(
            @PathVariable Long id,
            @RequestAttribute(value = OptionalJwtInterceptor.ATTR_USER_ID, required = false) Long viewerId) {
        return Result.success(datingService.workDetail(id, viewerId));
    }

    /** 与某人的对话记录（读消息需要登录，否则不知道「我」是谁） */
    @GetMapping("/messages/{peerId}")
    public Result<List<DatingMessageVO>> conversation(
            @PathVariable Long peerId,
            @RequestAttribute(value = OptionalJwtInterceptor.ATTR_USER_ID, required = false) Long viewerId) {
        if (viewerId == null) {
            return Result.of(401, "登录后才能查看私信", null);
        }
        return Result.success(datingService.conversation(viewerId, peerId));
    }

    // ==================== 我的写操作（必须登录） ====================

    /** 发布作品 */
    @PostMapping("/me/works")
    public Result<DatingWorkVO> createWork(
            @RequestAttribute(JwtInterceptor.ATTR_USER_ID) Long userId,
            @Valid @RequestBody DatingWorkRequest request) {
        // 简单限流：单人作品数上限，避免恶意刷库
        long count = datingService.feedCountOf(userId);
        if (count >= MAX_WORKS_PER_USER) {
            return Result.error("作品太多了，最多保存 " + MAX_WORKS_PER_USER + " 件，先删掉几件吧");
        }
        return Result.success("作品已收入阁中 ♥", datingService.create(userId, request));
    }

    /** 删除自己的作品 */
    @DeleteMapping("/me/works/{id}")
    public Result<Void> deleteWork(
            @RequestAttribute(JwtInterceptor.ATTR_USER_ID) Long userId,
            @PathVariable Long id) {
        datingService.delete(userId, id);
        return Result.success("作品已移除");
    }

    /** 点赞 / 取消点赞（切换） */
    @PostMapping("/me/likes/{workId}")
    public Result<Map<String, Object>> toggleLike(
            @RequestAttribute(JwtInterceptor.ATTR_USER_ID) Long userId,
            @PathVariable Long workId) {
        Map<String, Object> data = datingService.toggleLike(userId, workId);
        boolean liked = Boolean.TRUE.equals(data.get("liked"));
        return Result.success(liked ? "♥ 已点赞" : "已取消点赞", data);
    }

    /** 发私信 */
    @PostMapping("/me/messages/{toUserId}")
    public Result<DatingMessageVO> sendMessage(
            @RequestAttribute(JwtInterceptor.ATTR_USER_ID) Long userId,
            @PathVariable Long toUserId,
            @Valid @RequestBody DatingMessageRequest request) {
        return Result.success("已送出", datingService.send(userId, toUserId, request.getContent()));
    }

    /** 我的会话列表 */
    @GetMapping("/me/conversations")
    public Result<List<DatingConversationVO>> conversations(
            @RequestAttribute(JwtInterceptor.ATTR_USER_ID) Long userId) {
        return Result.success(datingService.conversations(userId));
    }

    /** 我的未读私信数（顶栏红点） */
    @GetMapping("/me/unread")
    public Result<Integer> unread(@RequestAttribute(JwtInterceptor.ATTR_USER_ID) Long userId) {
        return Result.success(datingService.unreadCount(userId));
    }

    /** 读取我自己的联系方式（编辑表单回填） */
    @GetMapping("/me/contact")
    public Result<DatingContactInfo> myContact(@RequestAttribute(JwtInterceptor.ATTR_USER_ID) Long userId) {
        return Result.success(datingService.myContact(userId));
    }

    /** 保存我的联系方式与可见性 */
    @PutMapping("/me/contact")
    public Result<DatingContactInfo> saveContact(
            @RequestAttribute(JwtInterceptor.ATTR_USER_ID) Long userId,
            @RequestBody DatingContactInfo request) {
        return Result.success("联系方式已保存", datingService.saveContact(userId, request));
    }

    /**
     * 上传作品照片（单张），返回 /uploads/xxx.jpg。
     * 前端先调本接口拿地址，发布作品时再把地址列表一起提交。
     */
    @PostMapping("/me/upload/image")
    public Result<String> uploadImage(
            @RequestAttribute(JwtInterceptor.ATTR_USER_ID) Long userId,
            @RequestParam("file") MultipartFile file) {
        String url = fileUploadService.uploadWorkImage(file, "work_" + userId);
        return Result.success("照片已上传", url);
    }

    /** 上传作品视频（单段，表单提交、不做转码） */
    @PostMapping("/me/upload/video")
    public Result<String> uploadVideo(
            @RequestAttribute(JwtInterceptor.ATTR_USER_ID) Long userId,
            @RequestParam("file") MultipartFile file) {
        String url = fileUploadService.uploadVideo(file);
        return Result.success("视频已上传", url);
    }

    // ==================== 内部方法 ====================

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
