package com.fenglin.springboottest.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fenglin.springboottest.dto.CategoryRequest;
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

    /** 后台分类树（每个节点附带条目数） */
    List<CategoryVO> manageTree();

    /** 新增分类 */
    CategoryVO create(CategoryRequest request);

    /** 局部修改分类（null 字段保持原值） */
    CategoryVO update(Long id, CategoryRequest request);

    /** 逻辑删除分类；有子分类或仍有条目时拒绝删除 */
    void remove(Long id);
}
