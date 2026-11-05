package com.fenglin.springboottest.dto;

import lombok.Data;

/**
 * 收藏状态视图对象（切换收藏 / 查询收藏状态都返回它）。
 */
@Data
public class FavoriteStatusVO {

    /** 条目ID */
    private Long itemId;

    /** 当前用户是否已收藏 */
    private Boolean favorited;

    /** 该条目被收藏的总次数 */
    private Long favoriteCount;
}
