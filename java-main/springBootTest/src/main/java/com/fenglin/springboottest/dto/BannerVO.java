package com.fenglin.springboottest.dto;

import lombok.Data;

/**
 * 首页轮播标语视图对象
 * <p>字段与前端轮播卡片对齐：id / title / subtitle / tag / accent。</p>
 */
@Data
public class BannerVO {

    /** 主键ID */
    private Long id;

    /** 主标题 */
    private String title;

    /** 副标题/描述 */
    private String subtitle;

    /** 小标签 */
    private String tag;

    /** 主题强调色（HEX） */
    private String accent;
}
