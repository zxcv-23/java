package com.fenglin.springboottest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fenglin.springboottest.dto.CategoryVO;
import com.fenglin.springboottest.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 分类 Mapper
 */
@Mapper
public interface CategoryMapper extends BaseMapper<Category> {

    /**
     * 后台管理用平铺列表：附带每个分类下的条目数（只统计未删除条目）。
     */
    @Select("SELECT c.id, c.parent_id AS parentId, c.name, c.level, c.icon, c.sort, c.description, " +
            "       (SELECT COUNT(*) FROM item i WHERE i.category_id = c.id AND i.deleted = 0) AS itemCount " +
            "FROM category c " +
            "WHERE c.deleted = 0 " +
            "ORDER BY c.sort ASC, c.id ASC")
    List<CategoryVO> manageFlat();
}
