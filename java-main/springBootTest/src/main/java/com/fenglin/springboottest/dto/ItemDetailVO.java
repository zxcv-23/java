package com.fenglin.springboottest.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 前台条目详情视图对象。
 * <p>与列表卡片（ItemSearchVO）的区别：额外带正文 content、作者、创建时间、
 * 浏览量和收藏数，供详情页完整展示。</p>
 */
@Data
public class ItemDetailVO {

    private Long id;

    /** 图标 emoji */
    private String emoji;

    private String title;

    /** 描述（由 item.description 映射） */
    private String desc;

    private String tag;

    /** 正文/详情 */
    private String content;

    /** 作者/来源 */
    private String author;

    private Long categoryId;

    private String categoryName;

    /** 浏览次数（返回的是本次打开 +1 之后的值） */
    private Long viewCount;

    /** 被收藏次数 */
    private Long favoriteCount;

    private LocalDateTime createTime;
}
