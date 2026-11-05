package com.fenglin.springboottest.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fenglin.springboottest.dto.FavoriteStatusVO;
import com.fenglin.springboottest.dto.ItemSearchVO;
import com.fenglin.springboottest.entity.Favorite;
import com.fenglin.springboottest.entity.Item;
import com.fenglin.springboottest.exception.BusinessException;
import com.fenglin.springboottest.mapper.FavoriteMapper;
import com.fenglin.springboottest.mapper.ItemMapper;
import com.fenglin.springboottest.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户收藏服务实现
 */
@Service
public class FavoriteServiceImpl implements FavoriteService {

    @Autowired
    private FavoriteMapper favoriteMapper;

    @Autowired
    private ItemMapper itemMapper;

    @Override
    public FavoriteStatusVO toggle(Long userId, Long itemId) {
        ensureItemVisible(itemId);

        LambdaQueryWrapper<Favorite> query = new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getItemId, itemId);

        boolean favorited;
        if (favoriteMapper.selectOne(query) != null) {
            favoriteMapper.delete(query);
            favorited = false;
        } else {
            Favorite favorite = new Favorite();
            favorite.setUserId(userId);
            favorite.setItemId(itemId);
            try {
                favoriteMapper.insert(favorite);
                favorited = true;
            } catch (DuplicateKeyException e) {
                // 并发双击：唯一索引已挡住重复插入，视为「已收藏」即可
                favorited = true;
            }
        }
        return wrap(itemId, favorited);
    }

    @Override
    public FavoriteStatusVO status(Long userId, Long itemId) {
        ensureItemVisible(itemId);
        Long count = favoriteMapper.selectCount(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getItemId, itemId));
        return wrap(itemId, count != null && count > 0);
    }

    @Override
    public List<ItemSearchVO> list(Long userId) {
        return favoriteMapper.listByUser(userId);
    }

    /** 只允许收藏仍在线、未删除的条目（@TableLogic 会自动附加 deleted = 0） */
    private void ensureItemVisible(Long itemId) {
        if (itemId == null || itemId <= 0) {
            throw new BusinessException("请指定要收藏的条目");
        }
        Long count = itemMapper.selectCount(new LambdaQueryWrapper<Item>()
                .eq(Item::getId, itemId)
                .eq(Item::getStatus, 1));
        if (count == null || count == 0) {
            throw new BusinessException("此物已不在生死簿中，无法收藏");
        }
    }

    private FavoriteStatusVO wrap(Long itemId, boolean favorited) {
        FavoriteStatusVO vo = new FavoriteStatusVO();
        vo.setItemId(itemId);
        vo.setFavorited(favorited);
        vo.setFavoriteCount(favoriteMapper.countByItem(itemId));
        return vo;
    }
}
