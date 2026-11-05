package com.fenglin.springboottest.controller;

import com.fenglin.springboottest.common.PageResult;
import com.fenglin.springboottest.common.Result;
import com.fenglin.springboottest.dto.ItemDetailVO;
import com.fenglin.springboottest.dto.ItemSearchVO;
import com.fenglin.springboottest.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 前台条目浏览接口（公开，无需登录）。
 *
 * <ul>
 *   <li>GET /api/items             —— 分页浏览（关键词 / 分类 / 最新|最热）</li>
 *   <li>GET /api/items/hot         —— 热门榜（按浏览量）</li>
 *   <li>GET /api/items/{id}        —— 条目详情（自动累加浏览量）</li>
 * </ul>
 *
 * <p>旧的 GET /api/search 保持不变，老前端继续可用。</p>
 */
@RestController
@RequestMapping("/api/items")
public class ItemController {

    @Autowired
    private ItemService itemService;

    /** 分页浏览：page 从 1 开始，size 上限 50，sort = new(默认) | hot */
    @GetMapping
    public Result<PageResult<ItemSearchVO>> browse(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false, defaultValue = "new") String sort,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "12") Integer size) {
        return Result.success(itemService.browse(keyword, categoryId, sort, page, size));
    }

    /** 热门榜：按浏览量倒序 */
    @GetMapping("/hot")
    public Result<List<ItemSearchVO>> hot(
            @RequestParam(required = false, defaultValue = "8") Integer limit) {
        return Result.success("月老为你选出最热闹的姻缘 ♥", itemService.hot(limit));
    }

    /** 条目详情：下线 / 已删除 / 不存在都会返回可读的业务提示 */
    @GetMapping("/{id}")
    public Result<ItemDetailVO> detail(@PathVariable Long id) {
        return Result.success("已寻得此段姻缘 ♥", itemService.detail(id));
    }
}
