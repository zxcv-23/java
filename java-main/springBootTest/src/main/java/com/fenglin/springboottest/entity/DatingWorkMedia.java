package com.fenglin.springboottest.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 作品附带媒体（照片 / 视频），对应数据库表 dating_work_media。
 *
 * <p>用 media_type 区分 image / video，用 sort_order 保留用户上传时的顺序
 * （封面取 sort_order 最小的图片）。</p>
 */
@Data
@TableName("dating_work_media")
public class DatingWorkMedia {

    /** 主键ID（自增） */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属作品ID */
    private Long workId;

    /** 媒体类型：image-图片 video-视频 */
    private String mediaType;

    /** 访问地址（形如 /uploads/work_12_xx.jpg） */
    private String url;

    /** 顺序（从 0 开始，越小越靠前） */
    private Integer sortOrder;
}
