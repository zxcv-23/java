package com.fenglin.springboottest.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 作品集返回给前端的视图对象。
 *
 * <p>把「作者昵称 / 头像」一起带上，前端列表页就不用为每条作品再查一次用户。</p>
 */
@Data
public class DatingWorkVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    /** 作者用户ID */
    private Long userId;

    /** 作者昵称 */
    private String authorName;

    /** 作者头像 */
    private String authorAvatar;

    /** 作者性别：1-男 2-女 0-未知 */
    private Integer authorGender;

    private String title;

    private String content;

    /** 封面地址（可能为空，前端用首张图片兜底） */
    private String cover;

    /** BGM 地址，为空表示没有配乐 */
    private String bgmUrl;

    /** BGM 名称 */
    private String bgmName;

    /** 照片与视频（按上传顺序） */
    private List<DatingMediaVO> media;

    private Integer likeCount;

    private Integer commentCount;

    private Integer viewCount;

    /** 当前请求者是否已点赞 */
    private Boolean liked;

    /** 是否是自己发的（前端据此显示删除按钮） */
    private Boolean mine;

    /** 创建时间 yyyy-MM-dd HH:mm */
    private String createdAt;
}
