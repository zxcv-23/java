package com.fenglin.springboottest.dto;

import lombok.Data;

/**
 * 后台管理列表视图对象
 * <p>与 ItemMapper#manageList 的查询列一一对应：
 * 在 ItemSearchVO 基础上额外携带 content / author / status，
 * 供后台查看与上下线管理。</p>
 */
@Data
public class ItemManageVO {

    private Long id;

    /** 图标 emoji */
    private String emoji;

    /** 标题 */
    private String title;

    /** 描述（由 item.description 映射） */
    private String desc;

    /** 标签 */
    private String tag;

    /** 正文/详情 */
    private String content;

    /** 作者/来源 */
    private String author;

    /** 状态：1-上线 0-下线 */
    private Integer status;

    /** 所属分类ID */
    private Long categoryId;

    /** 所属分类名称 */
    private String categoryName;
}
