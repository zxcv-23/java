package com.fenglin.springboottest.controller;

import com.fenglin.springboottest.common.Result;
import com.fenglin.springboottest.dto.HomeVO;
import com.fenglin.springboottest.dto.ItemSearchVO;
import com.fenglin.springboottest.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 首页 / 搜索接口（REST）
 * <p>前端 Vue 项目通过 GET /api/search、/api/home 调用。</p>
 */
@RestController
@RequestMapping("/api")
public class HomeController {

    @Autowired
    private ItemService itemService;

    /**
     * 分级分类搜索
     *
     * @param keyword     关键词（可选）
     * @param categoryId  分类ID（可选；限定到该分类及其子孙分类）
     */
    @GetMapping("/search")
    public Result<List<ItemSearchVO>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId) {
        List<ItemSearchVO> list = itemService.search(keyword, categoryId);
        String message = list.isEmpty()
                ? "冥河寂寂，未见与此相关的姻缘。"
                : "月老已为你牵起丝线 ♥";
        return Result.success(message, list);
    }

    /**
     * 首页展示数据：分类树 + 推荐条目 + 热门标签
     */
    @GetMapping("/home")
    public Result<HomeVO> home() {
        return Result.success("首页已就绪 ♥", itemService.home());
    }
}
