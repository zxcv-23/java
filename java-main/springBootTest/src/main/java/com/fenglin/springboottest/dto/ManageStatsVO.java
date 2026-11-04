package com.fenglin.springboottest.dto;

import lombok.Data;

/**
 * 后台概览统计（顶部卡片用真实库内计数，不再由前端截断后的列表推算）
 */
@Data
public class ManageStatsVO {

    /** 未删除条目总数 */
    private Long total;

    /** 上线中 */
    private Long online;

    /** 已下线 */
    private Long offline;

    /** 回收站（逻辑删除）条目数 */
    private Long deleted;

    /** 分类数 */
    private Long categories;
}
