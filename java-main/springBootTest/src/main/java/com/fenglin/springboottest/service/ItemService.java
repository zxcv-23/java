package com.fenglin.springboottest.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fenglin.springboottest.dto.HomeVO;
import com.fenglin.springboottest.dto.ItemManageVO;
import com.fenglin.springboottest.dto.ItemSearchVO;
import com.fenglin.springboottest.entity.Item;

import java.util.List;

/**
 * 条目服务接口
 */
public interface ItemService extends IService<Item> {

    /**
     * 分级分类搜索
     *
     * @param keyword     关键词（可空）
     * @param categoryId  分类ID（可空；非空时自动包含其全部子孙分类）
     * @return 匹配条目列表
     */
    List<ItemSearchVO> search(String keyword, Long categoryId);

    /**
     * 首页推荐条目
     */
    List<ItemSearchVO> featured();

    /**
     * 热门标签
     */
    List<String> hotTags();

    /**
     * 首页展示数据（分类树 + 推荐 + 热门标签）
     */
    HomeVO home();

    /**
     * 后台管理列表：返回全部未删除条目（含已下线），附带分类名称
     *
     * @param keyword     关键词（可选，匹配标题/标签）
     * @param categoryId  分类ID（可选，精确匹配直接归属分类）
     * @return 管理条目列表
     */
    List<ItemManageVO> manageList(String keyword, Long categoryId);
}
