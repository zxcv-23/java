package com.fenglin.springboottest.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fenglin.springboottest.dto.DatingCandidateVO;
import com.fenglin.springboottest.dto.DatingContactInfo;
import com.fenglin.springboottest.dto.DatingConversationVO;
import com.fenglin.springboottest.dto.DatingMediaVO;
import com.fenglin.springboottest.dto.DatingMessageVO;
import com.fenglin.springboottest.dto.DatingProfileVO;
import com.fenglin.springboottest.dto.DatingWorkRequest;
import com.fenglin.springboottest.dto.DatingWorkVO;
import com.fenglin.springboottest.entity.DatingContact;
import com.fenglin.springboottest.entity.DatingLike;
import com.fenglin.springboottest.entity.DatingMessage;
import com.fenglin.springboottest.entity.DatingWork;
import com.fenglin.springboottest.entity.DatingWorkMedia;
import com.fenglin.springboottest.entity.User;
import com.fenglin.springboottest.exception.BusinessException;
import com.fenglin.springboottest.mapper.DatingContactMapper;
import com.fenglin.springboottest.mapper.DatingLikeMapper;
import com.fenglin.springboottest.mapper.DatingMessageMapper;
import com.fenglin.springboottest.mapper.DatingWorkMapper;
import com.fenglin.springboottest.mapper.DatingWorkMediaMapper;
import com.fenglin.springboottest.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 姻缘阁作品集：作品 / 点赞 / 私信 / 联系方式 的业务逻辑。
 *
 * <p>几条刻意遵守的规则：</p>
 * <ul>
 *   <li><strong>作者身份只从 JWT 取</strong>：所有写接口的 userId 都由服务端注入，
 *       请求体里即使带了 userId 也会被忽略，杜绝「替别人发作品 / 冒充别人发私信」。</li>
 *   <li><strong>可见性判断集中在这里</strong>：联系方式是否可见只由 {@link #canSeeContact} 决定，
 *       控制器不重复实现，避免两处口径不一致。</li>
 *   <li><strong>计数用原子 SQL</strong>：点赞数走 {@code like_count = like_count + 1}，
 *       不做「查出来 +1 再写回」，避免并发下丢计数。</li>
 * </ul>
 */
@Service
public class DatingService {

    /** 单条作品最多图片数，与 application-dev.properties 的 dating.work.max-images 呼应 */
    private static final int MAX_IMAGES = 9;

    /** 消息里允许出现的 URL 前缀，避免把站外站点当图片/视频播放（简易白名单） */
    private static final String UPLOAD_PREFIX = "/uploads/";

    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Autowired
    private DatingWorkMapper workMapper;

    @Autowired
    private DatingWorkMediaMapper mediaMapper;

    @Autowired
    private DatingLikeMapper likeMapper;

    @Autowired
    private DatingMessageMapper messageMapper;

    @Autowired
    private DatingContactMapper contactMapper;

    @Autowired
    private UserMapper userMapper;

    // ==================== 嘉宾主页 ====================

    /**
     * 嘉宾主页聚合：资料 + 联系方式可见性 + 作品集。
     *
     * @param targetUserId 主页主人
     * @param viewerId     当前登录用户，未登录为 null
     */
    public DatingProfileVO profile(Long targetUserId, Long viewerId) {
        User target = userMapper.selectById(targetUserId);
        if (target == null || target.getStatus() == null || target.getStatus() != 1) {
            throw new BusinessException("这位有缘人已不在姻缘簿上了");
        }

        DatingProfileVO vo = new DatingProfileVO();
        vo.setProfile(toCandidateVO(target));
        boolean mine = viewerId != null && viewerId.equals(targetUserId);
        vo.setMine(mine);
        vo.setLoggedIn(viewerId != null);

        // 作品：本人看全部，别人只看公开
        List<DatingWork> works = listWorksOf(targetUserId, mine);
        vo.setWorks(toWorkVOs(works, viewerId));

        DatingContact contact = findContact(targetUserId);
        boolean visible = canSeeContact(targetUserId, viewerId, contact);
        vo.setContactVisible(visible);
        if (visible && contact != null) {
            DatingContactInfo info = new DatingContactInfo();
            info.setWechat(contact.getWechat());
            info.setQq(contact.getQq());
            info.setPhone(contact.getPhone());
            info.setOther(contact.getOther());
            info.setVisible(contact.getVisible());
            vo.setContact(info);
        }
        return vo;
    }

    /** 某人的作品列表：mine=true 返回全部（含仅自己可见），否则只返回 visible=1 */
    private List<DatingWork> listWorksOf(Long userId, boolean mine) {
        LambdaQueryWrapper<DatingWork> q = new LambdaQueryWrapper<>();
        q.eq(DatingWork::getUserId, userId);
        if (!mine) {
            q.eq(DatingWork::getVisible, 1);
        }
        q.orderByDesc(DatingWork::getId);
        return workMapper.selectList(q);
    }

    /**
     * 联系方式可见性：
     * <ol>
     *   <li>本人永远可见</li>
     *   <li>未登录一律不可见</li>
     *   <li>对方把 visible 设成 1（公开给登录用户）可见</li>
     *   <li>双方之间有过私信往来可见</li>
     * </ol>
     */
    private boolean canSeeContact(Long targetUserId, Long viewerId, DatingContact contact) {
        if (viewerId == null) {
            return false;
        }
        if (viewerId.equals(targetUserId)) {
            return true;
        }
        if (contact == null) {
            return false;
        }
        if (contact.getVisible() != null && contact.getVisible() == 1) {
            return true;
        }
        return hasMessaged(targetUserId, viewerId);
    }

    /** 两个用户之间是否存在任意方向的消息 */
    private boolean hasMessaged(Long a, Long b) {
        LambdaQueryWrapper<DatingMessage> q = new LambdaQueryWrapper<>();
        q.and(w -> w.and(x -> x.eq(DatingMessage::getFromUserId, a).eq(DatingMessage::getToUserId, b))
                    .or(x -> x.eq(DatingMessage::getFromUserId, b).eq(DatingMessage::getToUserId, a)));
        return messageMapper.selectCount(q) > 0;
    }

    // ==================== 作品 ====================

    /** 作品列表（全站，供「作品集」总览用）：只返回公开作品 */
    public List<DatingWorkVO> feed(int limit, Long viewerId) {
        int size = Math.min(Math.max(limit, 1), 60);
        LambdaQueryWrapper<DatingWork> q = new LambdaQueryWrapper<>();
        q.eq(DatingWork::getVisible, 1).orderByDesc(DatingWork::getId).last("LIMIT " + size);
        return toWorkVOs(workMapper.selectList(q), viewerId);
    }

    /** 某人已发布的作品总数（含仅自己可见），用于发布前的数量上限校验 */
    public long feedCountOf(Long userId) {
        LambdaQueryWrapper<DatingWork> q = new LambdaQueryWrapper<>();
        q.eq(DatingWork::getUserId, userId);
        return workMapper.selectCount(q);
    }

    /** 作品详情（浏览 +1，作者本人不计） */    public DatingWorkVO workDetail(Long workId, Long viewerId) {
        DatingWork work = workMapper.selectById(workId);
        if (work == null) {
            throw new BusinessException("这件作品已被收起");
        }
        boolean mine = viewerId != null && viewerId.equals(work.getUserId());
        if (!mine && (work.getVisible() == null || work.getVisible() != 1)) {
            throw new BusinessException("这件作品仅作者本人可见");
        }
        if (!mine) {
            // 原子自增，避免并发覆盖；失败不影响详情返回
            try {
                workMapper.update(null, new LambdaUpdateWrapper<DatingWork>()
                        .eq(DatingWork::getId, workId)
                        .setSql("view_count = view_count + 1"));
                work.setViewCount((work.getViewCount() == null ? 0 : work.getViewCount()) + 1);
            } catch (Exception ignored) {
                // 浏览数只是展示指标，统计失败不应让详情页报错
            }
        }
        List<DatingWorkVO> vos = toWorkVOs(List.of(work), viewerId);
        return vos.get(0);
    }

    /**
     * 发布作品。
     *
     * <p>媒体 URL 只接受本服务上传产生的 /uploads/ 前缀，防止把站外地址写进库后
     * 页面被当成图床 / 外链跳板。</p>
     */
    @Transactional
    public DatingWorkVO create(Long userId, DatingWorkRequest req) {
        List<String> images = new ArrayList<>();
        if (req.getImages() != null) {
            for (String url : req.getImages()) {
                if (!StringUtils.hasText(url)) {
                    continue;
                }
                String clean = url.trim();
                if (!clean.startsWith(UPLOAD_PREFIX) || clean.contains("..")) {
                    throw new BusinessException("图片地址不合法，请通过页面上传");
                }
                images.add(clean);
            }
        }
        if (images.size() > MAX_IMAGES) {
            throw new BusinessException("一件作品最多 " + MAX_IMAGES + " 张照片");
        }

        String video = StringUtils.hasText(req.getVideoUrl()) ? req.getVideoUrl().trim() : null;
        if (video != null && (!video.startsWith(UPLOAD_PREFIX) || video.contains(".."))) {
            throw new BusinessException("视频地址不合法，请通过页面上传");
        }
        if (images.isEmpty() && video == null) {
            throw new BusinessException("至少上传一张照片或一段视频");
        }
        String bgm = StringUtils.hasText(req.getBgmUrl()) ? req.getBgmUrl().trim() : null;
        // BGM 允许 http(s) 直链（便于放现成的音乐 CDN），但要挡掉 javascript: 这类协议
        if (bgm != null && !(bgm.startsWith("http://") || bgm.startsWith("https://") || bgm.startsWith(UPLOAD_PREFIX))) {
            throw new BusinessException("BGM 地址需以 http(s):// 或上传地址开头");
        }

        DatingWork work = new DatingWork();
        work.setUserId(userId);
        work.setTitle(req.getTitle().trim());
        work.setContent(StringUtils.hasText(req.getContent()) ? req.getContent().trim() : null);
        work.setCover(images.isEmpty() ? null : images.get(0));
        work.setBgmUrl(bgm);
        work.setBgmName(StringUtils.hasText(req.getBgmName()) ? req.getBgmName().trim() : null);
        work.setLikeCount(0);
        work.setCommentCount(0);
        work.setViewCount(0);
        work.setVisible(1);
        work.setCreateTime(LocalDateTime.now());
        work.setUpdateTime(LocalDateTime.now());
        workMapper.insert(work);

        // 媒体按上传顺序落库：图片 sort 0..n-1，视频排在最后
        int sort = 0;
        for (String url : images) {
            insertMedia(work.getId(), "image", url, sort++);
        }
        if (video != null) {
            insertMedia(work.getId(), "video", video, sort);
        }
        return workDetail(work.getId(), userId);
    }

    private void insertMedia(Long workId, String type, String url, int sort) {
        DatingWorkMedia media = new DatingWorkMedia();
        media.setWorkId(workId);
        media.setMediaType(type);
        media.setUrl(url);
        media.setSortOrder(sort);
        mediaMapper.insert(media);
    }

    /** 删除作品：仅作者本人可删，连带清理媒体与点赞记录 */
    @Transactional
    public void delete(Long userId, Long workId) {
        DatingWork work = workMapper.selectById(workId);
        if (work == null) {
            throw new BusinessException("作品不存在或已被删除");
        }
        if (!userId.equals(work.getUserId())) {
            throw new BusinessException("只能删除自己的作品");
        }
        mediaMapper.delete(new LambdaQueryWrapper<DatingWorkMedia>().eq(DatingWorkMedia::getWorkId, workId));
        likeMapper.delete(new LambdaQueryWrapper<DatingLike>().eq(DatingLike::getWorkId, workId));
        workMapper.deleteById(workId);
    }

    // ==================== 点赞 ====================

    /**
     * 点赞 / 取消点赞（同一个接口切换），返回最新点赞数与状态。
     * 重复点赞因唯一索引抛 DuplicateKeyException，这里捕获后视为幂等成功。
     */
    @Transactional
    public Map<String, Object> toggleLike(Long userId, Long workId) {
        DatingWork work = workMapper.selectById(workId);
        if (work == null) {
            throw new BusinessException("作品不存在或已被删除");
        }
        LambdaQueryWrapper<DatingLike> exist = new LambdaQueryWrapper<>();
        exist.eq(DatingLike::getWorkId, workId).eq(DatingLike::getUserId, userId);
        DatingLike old = likeMapper.selectOne(exist);

        boolean liked;
        if (old != null) {
            likeMapper.deleteById(old.getId());
            adjustCount(workId, "like_count", -1);
            liked = false;
        } else {
            try {
                DatingLike like = new DatingLike();
                like.setWorkId(workId);
                like.setUserId(userId);
                like.setCreateTime(LocalDateTime.now());
                likeMapper.insert(like);
                adjustCount(workId, "like_count", 1);
            } catch (DuplicateKeyException e) {
                // 并发下另一个请求已插入：结果等价，不报错也不算二次加数
            }
            liked = true;
        }
        DatingWork fresh = workMapper.selectById(workId);
        int count = fresh == null || fresh.getLikeCount() == null ? 0 : Math.max(0, fresh.getLikeCount());

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("liked", liked);
        data.put("likeCount", count);
        return data;
    }

    /** 原子增减计数字段（列名由调用方写死，不接受外部输入） */
    private void adjustCount(Long workId, String column, int delta) {
        workMapper.update(null, new LambdaUpdateWrapper<DatingWork>()
                .eq(DatingWork::getId, workId)
                .setSql(column + " = GREATEST(COALESCE(" + column + ", 0) + (" + delta + "), 0)"));
    }

    // ==================== 私信 ====================

    /** 发私信：不能发给自己；收件人必须存在且正常 */
    public DatingMessageVO send(Long fromUserId, Long toUserId, String content) {
        if (fromUserId.equals(toUserId)) {
            throw new BusinessException("不能给自己发私信哦");
        }
        User to = userMapper.selectById(toUserId);
        if (to == null || to.getStatus() == null || to.getStatus() != 1) {
            throw new BusinessException("对方已不在姻缘簿上了");
        }
        if (!StringUtils.hasText(content)) {
            throw new BusinessException("消息不能为空");
        }
        DatingMessage msg = new DatingMessage();
        msg.setFromUserId(fromUserId);
        msg.setToUserId(toUserId);
        msg.setContent(content.trim());
        msg.setIsRead(0);
        msg.setCreateTime(LocalDateTime.now());
        messageMapper.insert(msg);
        return toMessageVO(msg, fromUserId);
    }

    /** 对话记录：按 id 升序，进来即把我方收到的未读置为已读 */
    @Transactional
    public List<DatingMessageVO> conversation(Long userId, Long peerId) {
        LambdaQueryWrapper<DatingMessage> q = new LambdaQueryWrapper<>();
        q.and(w -> w.and(x -> x.eq(DatingMessage::getFromUserId, userId).eq(DatingMessage::getToUserId, peerId))
                    .or(x -> x.eq(DatingMessage::getFromUserId, peerId).eq(DatingMessage::getToUserId, userId)))
         .orderByAsc(DatingMessage::getId)
         .last("LIMIT 200");

        List<DatingMessage> rows = messageMapper.selectList(q);
        List<DatingMessageVO> list = new ArrayList<>(rows.size());
        for (DatingMessage m : rows) {
            list.add(toMessageVO(m, userId));
        }

        // 标记已读：对方发给我的未读消息
        messageMapper.update(null, new LambdaUpdateWrapper<DatingMessage>()
                .eq(DatingMessage::getFromUserId, peerId)
                .eq(DatingMessage::getToUserId, userId)
                .eq(DatingMessage::getIsRead, 0)
                .set(DatingMessage::getIsRead, 1));
        return list;
    }

    /**
     * 会话列表：把我参与的所有消息按「对方」归组，取每个对方最后一条 + 未读数。
     *
     * <p>先取最近 500 条（个人站数据量足够），在内存里归组，避免写复杂的
     * MySQL 8 窗口函数 SQL；会话多的场景以后再换成专门的会话表。</p>
     */
    public List<DatingConversationVO> conversations(Long userId) {
        LambdaQueryWrapper<DatingMessage> q = new LambdaQueryWrapper<>();
        q.and(w -> w.eq(DatingMessage::getFromUserId, userId).or().eq(DatingMessage::getToUserId, userId))
         .orderByDesc(DatingMessage::getId)
         .last("LIMIT 500");
        List<DatingMessage> rows = messageMapper.selectList(q);

        Map<Long, DatingConversationVO> byPeer = new LinkedHashMap<>();
        Map<Long, Integer> unread = new HashMap<>();
        for (DatingMessage m : rows) {
            Long peer = m.getFromUserId().equals(userId) ? m.getToUserId() : m.getFromUserId();
            DatingConversationVO vo = byPeer.get(peer);
            if (vo == null) {
                // 因为按 id 倒序，第一次遇到的就是最后一条
                vo = new DatingConversationVO();
                vo.setPeerId(peer);
                vo.setLastMessage(m.getContent());
                vo.setLastTime(fmt(m.getCreateTime()));
                vo.setLastFromMe(m.getFromUserId().equals(userId));
                byPeer.put(peer, vo);
            }
            if (!m.getFromUserId().equals(userId)
                    && (m.getIsRead() == null || m.getIsRead() == 0)) {
                unread.merge(peer, 1, Integer::sum);
            }
        }
        if (byPeer.isEmpty()) {
            return new ArrayList<>();
        }

        List<User> users = userMapper.selectBatchIds(byPeer.keySet());
        Map<Long, User> userMap = new HashMap<>();
        for (User u : users) {
            userMap.put(u.getId(), u);
        }
        List<DatingConversationVO> list = new ArrayList<>(byPeer.values());
        for (DatingConversationVO vo : list) {
            User peer = userMap.get(vo.getPeerId());
            vo.setPeerName(peer == null ? "已注销用户"
                    : (StringUtils.hasText(peer.getNickname()) ? peer.getNickname() : "匿名有缘人"));
            vo.setPeerAvatar(peer == null ? "" : (peer.getAvatar() == null ? "" : peer.getAvatar()));
            vo.setPeerGender(peer == null || peer.getGender() == null ? 0 : peer.getGender());
            vo.setUnread(unread.getOrDefault(vo.getPeerId(), 0));
        }
        // 未读优先，其次按最后一条消息 id 倒序（byPeer 已是有序的 LinkedHashMap）
        list.sort(Comparator.comparing((DatingConversationVO v) -> v.getUnread() == null ? 0 : v.getUnread()).reversed());
        return list;
    }

    /** 未读总数（顶栏红点） */
    public int unreadCount(Long userId) {
        LambdaQueryWrapper<DatingMessage> q = new LambdaQueryWrapper<>();
        q.eq(DatingMessage::getToUserId, userId).eq(DatingMessage::getIsRead, 0);
        return Math.toIntExact(messageMapper.selectCount(q));
    }

    // ==================== 联系方式 ====================

    /** 读取自己的联系方式（编辑用，永远返回） */
    public DatingContactInfo myContact(Long userId) {
        DatingContact c = findContact(userId);
        DatingContactInfo info = new DatingContactInfo();
        if (c == null) {
            info.setVisible(0);
            return info;
        }
        info.setWechat(c.getWechat());
        info.setQq(c.getQq());
        info.setPhone(c.getPhone());
        info.setOther(c.getOther());
        info.setVisible(c.getVisible() == null ? 0 : c.getVisible());
        return info;
    }

    /** 保存自己的联系方式：有则更新、无则新增（一人一条，靠唯一索引保证） */
    @Transactional
    public DatingContactInfo saveContact(Long userId, DatingContactInfo req) {
        DatingContact c = findContact(userId);
        boolean create = c == null;
        if (create) {
            c = new DatingContact();
            c.setUserId(userId);
        }
        c.setWechat(trimOrNull(req.getWechat(), 60, "微信号"));
        c.setQq(trimOrNull(req.getQq(), 30, "QQ号"));
        c.setPhone(trimOrNull(req.getPhone(), 30, "手机号"));
        c.setOther(trimOrNull(req.getOther(), 120, "其他联系方式"));
        c.setVisible(req.getVisible() != null && req.getVisible() == 1 ? 1 : 0);
        c.setUpdateTime(LocalDateTime.now());
        if (create) {
            contactMapper.insert(c);
        } else {
            contactMapper.updateById(c);
        }
        return myContact(userId);
    }

    private String trimOrNull(String v, int max, String label) {
        if (!StringUtils.hasText(v)) {
            return null;
        }
        String t = v.trim();
        if (t.length() > max) {
            throw new BusinessException(label + "最多 " + max + " 个字符");
        }
        return t;
    }

    private DatingContact findContact(Long userId) {
        LambdaQueryWrapper<DatingContact> q = new LambdaQueryWrapper<>();
        q.eq(DatingContact::getUserId, userId);
        return contactMapper.selectOne(q);
    }

    // ==================== 视图转换 ====================

    private DatingCandidateVO toCandidateVO(User user) {
        DatingCandidateVO vo = new DatingCandidateVO();
        vo.setId(user.getId());
        vo.setNickname(StringUtils.hasText(user.getNickname()) ? user.getNickname() : "匿名有缘人");
        vo.setAvatar(StringUtils.hasText(user.getAvatar()) ? user.getAvatar() : "");
        int gender = user.getGender() == null ? 0 : user.getGender();
        vo.setGender(gender);
        vo.setGenderLabel(gender == 1 ? "公子" : gender == 2 ? "姑娘" : "未留");
        vo.setBio(StringUtils.hasText(user.getBio()) ? user.getBio() : "这位有缘人尚未留下只言片语，等你来问～");
        if (user.getCreateTime() != null) {
            LocalDateTime created = user.getCreateTime();
            vo.setJoinedAt(created.toLocalDate().toString());
            vo.setTag(java.time.temporal.ChronoUnit.DAYS.between(created.toLocalDate(), java.time.LocalDate.now()) <= 7
                    ? "新入阁" : "姻缘簿在册");
        }
        vo.setDemo(false);
        return vo;
    }

    /**
     * 批量转换作品视图：一次性把媒体、作者、点赞状态查出来，
     * 避免「每条作品查一次媒体 + 一次用户 + 一次点赞」的 N+1 查询。
     */
    private List<DatingWorkVO> toWorkVOs(List<DatingWork> works, Long viewerId) {
        List<DatingWorkVO> list = new ArrayList<>();
        if (works == null || works.isEmpty()) {
            return list;
        }
        List<Long> workIds = new ArrayList<>(works.size());
        Set<Long> userIds = new HashSet<>();
        for (DatingWork w : works) {
            workIds.add(w.getId());
            userIds.add(w.getUserId());
        }

        // 媒体
        LambdaQueryWrapper<DatingWorkMedia> mq = new LambdaQueryWrapper<>();
        mq.in(DatingWorkMedia::getWorkId, workIds).orderByAsc(DatingWorkMedia::getSortOrder);
        Map<Long, List<DatingMediaVO>> mediaMap = new HashMap<>();
        for (DatingWorkMedia m : mediaMapper.selectList(mq)) {
            DatingMediaVO vo = new DatingMediaVO();
            vo.setMediaType(m.getMediaType());
            vo.setUrl(m.getUrl());
            mediaMap.computeIfAbsent(m.getWorkId(), k -> new ArrayList<>()).add(vo);
        }

        // 作者
        Map<Long, User> userMap = new HashMap<>();
        for (User u : userMapper.selectBatchIds(userIds)) {
            userMap.put(u.getId(), u);
        }

        // 当前用户点过赞的作品
        Set<Long> likedIds = new HashSet<>();
        if (viewerId != null) {
            LambdaQueryWrapper<DatingLike> lq = new LambdaQueryWrapper<>();
            lq.in(DatingLike::getWorkId, workIds).eq(DatingLike::getUserId, viewerId);
            for (DatingLike l : likeMapper.selectList(lq)) {
                likedIds.add(l.getWorkId());
            }
        }

        for (DatingWork w : works) {
            DatingWorkVO vo = new DatingWorkVO();
            vo.setId(w.getId());
            vo.setUserId(w.getUserId());
            User author = userMap.get(w.getUserId());
            vo.setAuthorName(author == null ? "已注销用户"
                    : (StringUtils.hasText(author.getNickname()) ? author.getNickname() : "匿名有缘人"));
            vo.setAuthorAvatar(author == null || author.getAvatar() == null ? "" : author.getAvatar());
            vo.setAuthorGender(author == null || author.getGender() == null ? 0 : author.getGender());
            vo.setTitle(w.getTitle());
            vo.setContent(w.getContent());
            List<DatingMediaVO> media = mediaMap.getOrDefault(w.getId(), new ArrayList<>());
            vo.setMedia(media);
            String cover = w.getCover();
            if (!StringUtils.hasText(cover)) {
                for (DatingMediaVO m : media) {
                    if ("image".equals(m.getMediaType())) {
                        cover = m.getUrl();
                        break;
                    }
                }
            }
            vo.setCover(cover);
            vo.setBgmUrl(w.getBgmUrl());
            vo.setBgmName(w.getBgmName());
            vo.setLikeCount(w.getLikeCount() == null ? 0 : w.getLikeCount());
            vo.setCommentCount(w.getCommentCount() == null ? 0 : w.getCommentCount());
            vo.setViewCount(w.getViewCount() == null ? 0 : w.getViewCount());
            vo.setLiked(likedIds.contains(w.getId()));
            vo.setMine(viewerId != null && viewerId.equals(w.getUserId()));
            vo.setCreatedAt(fmt(w.getCreateTime()));
            list.add(vo);
        }
        return list;
    }

    private DatingMessageVO toMessageVO(DatingMessage m, Long viewerId) {
        DatingMessageVO vo = new DatingMessageVO();
        vo.setId(m.getId());
        vo.setFromUserId(m.getFromUserId());
        vo.setToUserId(m.getToUserId());
        vo.setContent(m.getContent());
        vo.setMine(m.getFromUserId().equals(viewerId));
        vo.setRead(m.getIsRead() != null && m.getIsRead() == 1);
        vo.setCreatedAt(fmt(m.getCreateTime()));
        return vo;
    }

    private String fmt(LocalDateTime t) {
        return t == null ? "" : t.format(DT);
    }
}
