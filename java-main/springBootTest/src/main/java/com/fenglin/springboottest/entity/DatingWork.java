package com.fenglin.springboottest.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 姻缘阁 · 作品实体，对应数据库表 dating_work。
 *
 * <p>一条作品 = 标题 + 文案 + 最多 N 张照片 + 可选 1 段视频 + 可选 1 首 BGM。
 * 照片与视频是「一对多」，单独存在 dating_work_media 里，便于保留图片顺序。</p>
 *
 * <p>作品只能由本人发布（user_id 取自 JWT，前端传不进来），
 * 点赞数 / 浏览数冗余在本表，避免每次列表都 count 子表。</p>
 */
@Data
@TableName("dating_work")
public class DatingWork {

    /** 主键ID（自增） */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 作者用户ID（服务端写入，不可伪造） */
    private Long userId;

    /** 作品标题 */
    private String title;

    /** 作品文案 / 心情描述 */
    private String content;

    /** 封面图（取第一张照片，没有照片则取视频播放不了时的占位） */
    private String cover;

    /** BGM 地址（可直接播放的音频 URL，可为空） */
    private String bgmUrl;

    /** BGM 名称（展示用，可为空） */
    private String bgmName;

    /** 点赞数（冗余统计） */
    private Integer likeCount;

    /** 留言数（冗余统计） */
    private Integer commentCount;

    /** 浏览数（冗余统计） */
    private Integer viewCount;

    /** 是否公开：1-公开 0-仅自己可见 */
    private Integer visible;

    /** 创建时间（由数据库默认值填充） */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
