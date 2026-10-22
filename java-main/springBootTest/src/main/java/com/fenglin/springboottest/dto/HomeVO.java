package com.fenglin.springboottest.dto;

import lombok.Data;

import java.util.List;

/**
 * 首页展示数据
 */
@Data
public class HomeVO {

    /** 分类树（分级分类，供前端筛选栏使用） */
    private List<CategoryVO> categories;

    /** 首页推荐条目（展示用） */
    private List<ItemSearchVO> featured;

    /** 热门标签 */
    private List<String> hotTags;

    /** 首页轮播标语 / 主题横幅（驱动前端高级动态主题） */
    private List<BannerVO> banners;
}
