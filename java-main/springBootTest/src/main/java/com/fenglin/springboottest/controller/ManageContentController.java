package com.fenglin.springboottest.controller;

import com.fenglin.springboottest.common.Result;
import com.fenglin.springboottest.dto.BannerRequest;
import com.fenglin.springboottest.dto.BannerVO;
import com.fenglin.springboottest.dto.CategoryRequest;
import com.fenglin.springboottest.dto.CategoryVO;
import com.fenglin.springboottest.service.BannerService;
import com.fenglin.springboottest.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 后台内容管理接口：分类 + 首页轮播标语。
 *
 * <p>路径都在 /api/manage/** 下，由 AdminJwtInterceptor 统一校验管理员 JWT；
 * 与条目管理接口互不干扰。此前分类与轮播只有「读」，首页内容改不了，
 * 这里补齐增删改与上下线。</p>
 *
 * <ul>
 *   <li>GET    /api/manage/categories            —— 分类树（带条目数）</li>
 *   <li>POST   /api/manage/categories            —— 新增分类</li>
 *   <li>PUT    /api/manage/categories/{id}       —— 局部修改分类</li>
 *   <li>DELETE /api/manage/categories/{id}       —— 删除分类（有子分类/藏品时拒绝）</li>
 *   <li>GET    /api/manage/banners               —— 轮播列表（含隐藏）</li>
 *   <li>POST   /api/manage/banners               —— 新增轮播</li>
 *   <li>PUT    /api/manage/banners/{id}          —— 局部修改轮播</li>
 *   <li>PUT    /api/manage/banners/{id}/status   —— 展示 / 隐藏</li>
 *   <li>DELETE /api/manage/banners/{id}          —— 删除轮播</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/manage")
public class ManageContentController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BannerService bannerService;

    // ---------------- 分类 ----------------

    @GetMapping("/categories")
    public Result<List<CategoryVO>> categories() {
        return Result.success(categoryService.manageTree());
    }

    @PostMapping("/categories")
    public Result<CategoryVO> createCategory(@Valid @RequestBody CategoryRequest request) {
        return Result.success("新分类已录入 ♥", categoryService.create(request));
    }

    /** 局部修改：不加 @Valid，只改传了的字段（Service 内兜底校验空白名称） */
    @PutMapping("/categories/{id}")
    public Result<CategoryVO> updateCategory(@PathVariable Long id,
                                             @RequestBody CategoryRequest request) {
        return Result.success("分类已修改", categoryService.update(id, request));
    }

    @DeleteMapping("/categories/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        categoryService.remove(id);
        return Result.success("分类已移出姻缘簿");
    }

    // ---------------- 轮播标语 ----------------

    @GetMapping("/banners")
    public Result<List<BannerVO>> banners() {
        return Result.success(bannerService.manageList());
    }

    @PostMapping("/banners")
    public Result<BannerVO> createBanner(@Valid @RequestBody BannerRequest request) {
        return Result.success("新标语已落笔 ♥", bannerService.create(request));
    }

    /** 局部修改：不加 @Valid，只改传了的字段 */
    @PutMapping("/banners/{id}")
    public Result<BannerVO> updateBanner(@PathVariable Long id,
                                         @RequestBody BannerRequest request) {
        return Result.success("标语已修改", bannerService.update(id, request));
    }

    @PutMapping("/banners/{id}/status")
    public Result<BannerVO> changeBannerStatus(@PathVariable Long id,
                                               @RequestParam Integer status) {
        BannerVO vo = bannerService.changeStatus(id, status);
        return Result.success(vo.getStatus() != null && vo.getStatus() == 1 ? "已展示" : "已隐藏", vo);
    }

    @DeleteMapping("/banners/{id}")
    public Result<Void> deleteBanner(@PathVariable Long id) {
        bannerService.remove(id);
        return Result.success("标语已移除");
    }
}
