package com.fenglin.springboottest.controller;

import com.fenglin.springboottest.common.Result;
import com.fenglin.springboottest.dto.AdminInfo;
import com.fenglin.springboottest.dto.ItemManageVO;
import com.fenglin.springboottest.dto.LoginRequest;
import com.fenglin.springboottest.service.AdminService;
import com.fenglin.springboottest.service.ItemService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 后台管理接口
 *
 * <ul>
 *   <li>POST /api/admin/login   —— 后台登录</li>
 *   <li>GET  /api/manage/items  —— 条目管理列表（含已下线）</li>
 * </ul>
 */
@RestController
@RequestMapping("/api")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private ItemService itemService;

    /** 后台登录 */
    @PostMapping("/admin/login")
    public Result<AdminInfo> login(@Valid @RequestBody LoginRequest request) {
        return Result.success("判官归位", adminService.login(request));
    }

    /**
     * 条目管理列表：返回全部未删除条目（含已下线），可按关键词 / 分类筛选
     *
     * @param keyword    关键词（可选，匹配标题/标签）
     * @param categoryId 分类ID（可选，精确匹配直接归属分类）
     */
    @GetMapping("/manage/items")
    public Result<List<ItemManageVO>> manageItems(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId) {
        return Result.success(itemService.manageList(keyword, categoryId));
    }
}
