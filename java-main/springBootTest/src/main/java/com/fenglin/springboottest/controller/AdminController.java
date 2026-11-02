package com.fenglin.springboottest.controller;

import com.fenglin.springboottest.common.Result;
import com.fenglin.springboottest.dto.AdminInfo;
import com.fenglin.springboottest.dto.ItemManageVO;
import com.fenglin.springboottest.dto.ItemUpdateRequest;
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
 *   <li>POST   /api/admin/login                 —— 后台登录</li>
 *   <li>GET    /api/manage/items                —— 条目管理列表（含已下线）</li>
 *   <li>POST   /api/manage/items                —— 新增条目</li>
 *   <li>PUT    /api/manage/items/{id}           —— 修改条目</li>
 *   <li>PUT    /api/manage/items/{id}/status    —— 上/下线</li>
 *   <li>DELETE /api/manage/items/{id}           —— 删除条目（逻辑删除）</li>
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

    /**
     * 修改条目（标题/描述/图标/标签/正文/作者/分类/状态）
     *
     * @param id      条目ID
     * @param request 修改内容，status 不传则保持原状态
     * @return 修改后的条目，前端可直接用它刷新那一行
     */
    @PutMapping("/manage/items/{id}")
    public Result<ItemManageVO> updateItem(@PathVariable Long id,
                                           @Valid @RequestBody ItemUpdateRequest request) {
        return Result.success("条目已修改", itemService.update(id, request));
    }

    /**
     * 新增条目（标题必填，其余可选）
     *
     * @param request 新增内容
     * @return 新建的条目，前端可直接插入列表首行
     */
    @PostMapping("/manage/items")
    public Result<ItemManageVO> createItem(@Valid @RequestBody ItemUpdateRequest request) {
        return Result.success("新条目已落笔", itemService.create(request));
    }

    /**
     * 上/下线切换（只改 status）
     *
     * @param status 1-上线 0-下线
     */
    @PutMapping("/manage/items/{id}/status")
    public Result<ItemManageVO> changeItemStatus(@PathVariable Long id,
                                                 @RequestParam Integer status) {
        ItemManageVO vo = itemService.changeStatus(id, status);
        return Result.success(vo.getStatus() != null && vo.getStatus() == 1 ? "已上线" : "已下线", vo);
    }

    /**
     * 删除条目（逻辑删除：deleted 置 1，数据仍留在库中，可人工恢复）
     */
    @DeleteMapping("/manage/items/{id}")
    public Result<Void> deleteItem(@PathVariable Long id) {
        itemService.remove(id);
        return Result.success("条目已从生死簿移除");
    }
}
