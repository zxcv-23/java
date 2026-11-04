package com.fenglin.springboottest.controller;

import com.fenglin.springboottest.common.PageResult;
import com.fenglin.springboottest.common.Result;
import com.fenglin.springboottest.dto.AdminInfo;
import com.fenglin.springboottest.dto.AdminPasswordChangeRequest;
import com.fenglin.springboottest.dto.ItemManageVO;
import com.fenglin.springboottest.dto.ItemUpdateRequest;
import com.fenglin.springboottest.dto.LoginRequest;
import com.fenglin.springboottest.dto.ManageStatsVO;
import com.fenglin.springboottest.security.AdminJwtInterceptor;
import com.fenglin.springboottest.service.AdminService;
import com.fenglin.springboottest.service.ItemService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 后台管理接口
 *
 * <p>除登录外，本类中的接口均由 AdminJwtInterceptor 保护，
 * 请求头必须带 {@code Authorization: Bearer <后台JWT>}。</p>
 *
 * <ul>
 *   <li>POST   /api/admin/login                 —— 后台登录（公开）</li>
 *   <li>GET    /api/admin/me                    —— 当前管理员身份自检</li>
 *   <li>POST   /api/admin/logout                —— 退出（作废旧 token）</li>
 *   <li>PUT    /api/admin/password              —— 修改自己的口令</li>
 *   <li>GET    /api/manage/stats                —— 概览统计</li>
 *   <li>GET    /api/manage/items                —— 条目管理列表（分页/筛选）</li>
 *   <li>POST   /api/manage/items                —— 新增条目</li>
 *   <li>PUT    /api/manage/items/{id}           —— 修改条目</li>
 *   <li>PUT    /api/manage/items/{id}/status    —— 上/下线</li>
 *   <li>DELETE /api/manage/items/{id}           —— 删除条目（逻辑删除）</li>
 *   <li>POST   /api/manage/items/{id}/restore   —— 从回收站恢复</li>
 * </ul>
 */
@RestController
@RequestMapping("/api")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private ItemService itemService;

    /** 后台登录（唯一放行的后台接口） */
    @PostMapping("/admin/login")
    public Result<AdminInfo> login(@Valid @RequestBody LoginRequest request) {
        return Result.success("判官归位", adminService.login(request));
    }

    /** 身份自检：前端进入后台时先调用，token 失效会由拦截器返回 401 */
    @GetMapping("/admin/me")
    public Result<AdminInfo> me(@RequestAttribute(AdminJwtInterceptor.ATTR_ADMIN_ID) Long adminId) {
        return Result.success(adminService.current(adminId));
    }

    /** 退出登录：自增令牌版本，当前 token 立即作废 */
    @PostMapping("/admin/logout")
    public Result<Void> logout(@RequestAttribute(AdminJwtInterceptor.ATTR_ADMIN_ID) Long adminId) {
        adminService.logout(adminId);
        return Result.success("已退出判官台");
    }

    /** 修改当前管理员口令：返回新 token，前端需替换本地会话 */
    @PutMapping("/admin/password")
    public Result<AdminInfo> changePassword(
            @RequestAttribute(AdminJwtInterceptor.ATTR_ADMIN_ID) Long adminId,
            @Valid @RequestBody AdminPasswordChangeRequest request) {
        return Result.success("口令已更换", adminService.changePassword(adminId, request));
    }

    /**
     * 条目管理列表：分页返回条目，可按关键词 / 分类 / 状态筛选
     *
     * @param keyword    关键词（可选，匹配标题/标签/描述）
     * @param categoryId 分类ID（可选）
     * @param status     状态（可选：1-上线 0-下线；不传为全部）
     * @param deleted    true-回收站（已删除条目） false-正常条目
     * @param page       页码，从 1 开始
     * @param size       每页条数
     */
    @GetMapping("/manage/items")
    public Result<PageResult<ItemManageVO>> manageItems(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false, defaultValue = "false") Boolean deleted,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size) {
        return Result.success(itemService.manageList(keyword, categoryId, status,
                Boolean.TRUE.equals(deleted), page, size));
    }

    /** 后台概览统计（真实库内计数） */
    @GetMapping("/manage/stats")
    public Result<ManageStatsVO> manageStats() {
        return Result.success(itemService.stats());
    }

    /** 从回收站恢复逻辑删除的条目 */
    @PostMapping("/manage/items/{id}/restore")
    public Result<ItemManageVO> restoreItem(@PathVariable Long id) {
        return Result.success("条目已重回生死簿", itemService.restore(id));
    }

    /**
     * 修改条目（标题/描述/图标/标签/正文/作者/分类/状态）
     */
    @PutMapping("/manage/items/{id}")
    public Result<ItemManageVO> updateItem(@PathVariable Long id,
                                           @Valid @RequestBody ItemUpdateRequest request) {
        return Result.success("条目已修改", itemService.update(id, request));
    }

    /**
     * 新增条目（标题必填，其余可选）
     */
    @PostMapping("/manage/items")
    public Result<ItemManageVO> createItem(@Valid @RequestBody ItemUpdateRequest request) {
        return Result.success("新条目已落笔", itemService.create(request));
    }

    /**
     * 上/下线切换（只改 status）
     */
    @PutMapping("/manage/items/{id}/status")
    public Result<ItemManageVO> changeItemStatus(@PathVariable Long id,
                                                 @RequestParam Integer status) {
        ItemManageVO vo = itemService.changeStatus(id, status);
        return Result.success(vo.getStatus() != null && vo.getStatus() == 1 ? "已上线" : "已下线", vo);
    }

    /**
     * 删除条目（逻辑删除：deleted 置 1，数据仍留在库中，可在回收站恢复）
     */
    @DeleteMapping("/manage/items/{id}")
    public Result<Void> deleteItem(@PathVariable Long id) {
        itemService.remove(id);
        return Result.success("条目已从生死簿移除");
    }
}
