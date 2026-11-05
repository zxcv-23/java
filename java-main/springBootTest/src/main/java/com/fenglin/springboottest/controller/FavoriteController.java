package com.fenglin.springboottest.controller;

import com.fenglin.springboottest.common.Result;
import com.fenglin.springboottest.dto.FavoriteStatusVO;
import com.fenglin.springboottest.dto.ItemSearchVO;
import com.fenglin.springboottest.security.JwtInterceptor;
import com.fenglin.springboottest.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 我的收藏接口（全部受 JwtInterceptor 保护，需前台用户 JWT）。
 *
 * <ul>
 *   <li>GET    /api/profile/favorites             —— 我的收藏列表</li>
 *   <li>GET    /api/profile/favorites/{itemId}    —— 查询是否已收藏</li>
 *   <li>POST   /api/profile/favorites/{itemId}/toggle —— 收藏 / 取消收藏</li>
 * </ul>
 *
 * <p>放在 /api/profile/** 下可直接复用现有的用户态拦截器，用户ID由服务端从
 * token 解析，前端不必（也不应该）传 userId。</p>
 */
@RestController
@RequestMapping("/api/profile/favorites")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    @GetMapping
    public Result<List<ItemSearchVO>> list(
            @RequestAttribute(JwtInterceptor.ATTR_USER_ID) Long userId) {
        return Result.success("已取回你的珍藏 ♥", favoriteService.list(userId));
    }

    @GetMapping("/{itemId}")
    public Result<FavoriteStatusVO> status(
            @RequestAttribute(JwtInterceptor.ATTR_USER_ID) Long userId,
            @PathVariable Long itemId) {
        return Result.success(favoriteService.status(userId, itemId));
    }

    @PostMapping("/{itemId}/toggle")
    public Result<FavoriteStatusVO> toggle(
            @RequestAttribute(JwtInterceptor.ATTR_USER_ID) Long userId,
            @PathVariable Long itemId) {
        FavoriteStatusVO vo = favoriteService.toggle(userId, itemId);
        return Result.success(Boolean.TRUE.equals(vo.getFavorited())
                ? "已收入你的珍藏 ♥" : "已从珍藏中取下", vo);
    }
}
