package com.fenglin.springboottest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fenglin.springboottest.dto.ItemSearchVO;
import com.fenglin.springboottest.entity.Favorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 收藏 Mapper
 */
@Mapper
public interface FavoriteMapper extends BaseMapper<Favorite> {

    /**
     * 我的收藏列表：只返回仍在线、未删除的条目，附带分类名称与浏览量。
     */
    @Select("SELECT i.id, i.emoji, i.title, i.description AS `desc`, i.tag, " +
            "       i.category_id AS categoryId, c.name AS categoryName, " +
            "       i.view_count AS viewCount " +
            "FROM favorite f " +
            "JOIN item i ON i.id = f.item_id AND i.deleted = 0 AND i.status = 1 " +
            "LEFT JOIN category c ON c.id = i.category_id " +
            "WHERE f.user_id = #{userId} " +
            "ORDER BY f.id DESC")
    List<ItemSearchVO> listByUser(@Param("userId") Long userId);

    /** 某条目被多少人收藏 */
    @Select("SELECT COUNT(*) FROM favorite WHERE item_id = #{itemId}")
    long countByItem(@Param("itemId") Long itemId);
}
