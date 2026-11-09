package com.fenglin.springboottest.service;

import com.fenglin.springboottest.dto.MemoRequest;
import com.fenglin.springboottest.entity.Memo;

import java.util.List;

/**
 * 备忘录服务：所有方法都以 userId 作为第一道过滤条件，保证用户数据隔离。
 */
public interface MemoService {

    /** 我的备忘列表：置顶优先 → 未完成优先 → 最近更新时间倒序 */
    List<Memo> list(Long userId);

    /** 新增一条备忘，返回落库后的完整对象（含 id 与时间） */
    Memo create(Long userId, MemoRequest request);

    /** 部分更新（可改标题/正文/配色，也可勾选完成、置顶） */
    Memo update(Long userId, Long id, MemoRequest request);

    /** 删除（仅能删自己的） */
    void delete(Long userId, Long id);
}
