package com.fenglin.springboottest.dto;

import lombok.Data;

import java.util.List;

/**
 * 分类视图对象（树节点）
 */
@Data
public class CategoryVO {

    private Long id;

    private Long parentId;

    private String name;

    private Integer level;

    private String icon;

    private Integer sort;

    private String description;

    /** 该分类下的条目数（后台管理列表用；首页分类树为 null） */
    private Long itemCount;

    /** 子分类 */
    private List<CategoryVO> children;
}
