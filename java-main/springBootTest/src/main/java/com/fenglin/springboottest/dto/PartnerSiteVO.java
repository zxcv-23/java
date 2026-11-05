package com.fenglin.springboottest.dto;

import lombok.Data;

/**
 * 外部联动站点信息（后台管理页「前往相亲阁」跳转目标）。
 *
 * <p>由 application-*.properties 的 {@code partner.site.*} 配置驱动，
 * 前端不写死地址；以后换站点只改配置即可。</p>
 */
@Data
public class PartnerSiteVO {

    /** 站点展示名（按钮文案） */
    private String name;

    /** 跳转地址 */
    private String url;

    /** 是否启用该跳转入口 */
    private Boolean enabled;
}
