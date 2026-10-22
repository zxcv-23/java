package com.fenglin.springboottest.dto;

import lombok.Data;

/**
 * 搜索/展示结果视图对象
 * <p>字段命名与前端首页卡片对齐：id / emoji / title / desc / tag，
 * 额外附带 categoryId、categoryName 供前端展示分类来源。</p>
 */
@Data
public class ItemSearchVO {

    private Long id;

    /** 图标 emoji */
    private String emoji;

    /** 标题 */
    private String title;

    /** 描述（由 item.description 映射） */
    private String desc;

    /** 标签 */
    private String tag;

    /** 所属分类ID */
    private Long categoryId;

    /** 所属分类名称 */
    private String categoryName;
}
