package com.fenglin.springboottest.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fenglin.springboottest.dto.CategoryVO;
import com.fenglin.springboottest.entity.Category;

import java.util.List;

/**
 * 分类服务接口
 */
public interface CategoryService extends IService<Category> {

    /**
     * 查询分类树（分级分类：父 -> 子）
     *
     * @return 一级分类列表，每个节点含 children
     */
    List<CategoryVO> tree();
}
