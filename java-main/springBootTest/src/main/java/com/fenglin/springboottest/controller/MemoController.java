package com.fenglin.springboottest.controller;

import com.fenglin.springboottest.common.Result;
import com.fenglin.springboottest.dto.MemoRequest;
import com.fenglin.springboottest.entity.Memo;
import com.fenglin.springboottest.security.JwtInterceptor;
import com.fenglin.springboottest.service.MemoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 备忘录接口（REST）。
 *
 * <p>放在 /api/profile/** 下直接复用现有 JwtInterceptor：需要前台用户 JWT，
 * 用户ID由服务端从 token 解析注入，前端不传 userId，天然做到用户间数据隔离。</p>
 *
 * <ul>
 *   <li>GET    /api/profile/memos        —— 我的备忘列表</li>
 *   <li>POST   /api/profile/memos        —— 新增</li>
 *   <li>PUT    /api/profile/memos/{id}   —— 部分更新（标题/正文/配色/完成/置顶）</li>
 *   <li>DELETE /api/profile/memos/{id}   —— 删除</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/profile/memos")
public class MemoController {

    @Autowired
    private MemoService memoService;

    @GetMapping
    public Result<List<Memo>> list(
            @RequestAttribute(JwtInterceptor.ATTR_USER_ID) Long userId) {
        return Result.success("已为你取回全部备忘", memoService.list(userId));
    }

    @PostMapping
    public Result<Memo> create(
            @RequestAttribute(JwtInterceptor.ATTR_USER_ID) Long userId,
            @Valid @RequestBody MemoRequest request) {
        return Result.success("备忘已记下 ♥", memoService.create(userId, request));
    }

    @PutMapping("/{id}")
    public Result<Memo> update(
            @RequestAttribute(JwtInterceptor.ATTR_USER_ID) Long userId,
            @PathVariable Long id,
            @Valid @RequestBody MemoRequest request) {
        return Result.success("备忘已更新", memoService.update(userId, id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @RequestAttribute(JwtInterceptor.ATTR_USER_ID) Long userId,
            @PathVariable Long id) {
        memoService.delete(userId, id);
        return Result.success("备忘已撕下");
    }
}
