package com.fenglin.springboottest.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fenglin.springboottest.dto.HomeVO;
import com.fenglin.springboottest.dto.ItemManageVO;
import com.fenglin.springboottest.dto.ItemSearchVO;
import com.fenglin.springboottest.dto.ItemUpdateRequest;
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

    /**
     * 后台修改条目：按 ID 更新标题/描述/图标/标签/正文/作者/分类/状态
     *
     * @param id      条目ID
     * @param request 修改内容（status 为空则保持原状态）
     * @return 修改后的最新条目（供前端回显）
     */
    ItemManageVO update(Long id, ItemUpdateRequest request);

    /**
     * 后台新增条目：标题必填，其余字段可选（缺省为未分类 / 上线 / 空串）
     *
     * @param request 新增内容
     * @return 新建的最新条目（含自增 ID，供前端直接插入列表）
     */
    ItemManageVO create(ItemUpdateRequest request);

    /**
     * 后台上下线：只改 status，不动其他字段
     *
     * @param id     条目ID
     * @param status 1-上线 0-下线
     * @return 修改后的最新条目
     */
    ItemManageVO changeStatus(Long id, Integer status);

    /**
     * 后台删除条目（逻辑删除：deleted 置 1，数据仍在库中，可人工恢复）
     *
     * @param id 条目ID
     */
    void remove(Long id);
}
