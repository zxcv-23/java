package com.fenglin.springboottest.service;

import com.fenglin.springboottest.dto.FavoriteStatusVO;
import com.fenglin.springboottest.dto.ItemSearchVO;

import java.util.List;

/**
 * 用户收藏服务
 */
public interface FavoriteService {

    /** 切换收藏（已收藏则取消，未收藏则收藏），返回切换后的状态与总数 */
    FavoriteStatusVO toggle(Long userId, Long itemId);

    /** 查询当前用户对某条目的收藏状态 */
    FavoriteStatusVO status(Long userId, Long itemId);

    /** 我的收藏列表（只含仍在线、未删除的条目） */
    List<ItemSearchVO> list(Long userId);
}
