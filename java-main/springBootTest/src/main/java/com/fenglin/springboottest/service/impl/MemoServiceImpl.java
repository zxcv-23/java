package com.fenglin.springboottest.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fenglin.springboottest.dto.MemoRequest;
import com.fenglin.springboottest.entity.Memo;
import com.fenglin.springboottest.exception.BusinessException;
import com.fenglin.springboottest.mapper.MemoMapper;
import com.fenglin.springboottest.service.MemoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 备忘录服务实现
 */
@Service
public class MemoServiceImpl implements MemoService {

    private static final String DEFAULT_COLOR = "violet";

    @Autowired
    private MemoMapper memoMapper;

    @Override
    public List<Memo> list(Long userId) {
        return memoMapper.selectList(new LambdaQueryWrapper<Memo>()
                .eq(Memo::getUserId, userId)
                .orderByDesc(Memo::getPinned)
                .orderByAsc(Memo::getDone)
                .orderByDesc(Memo::getUpdateTime)
                .orderByDesc(Memo::getId));
    }

    @Override
    public Memo create(Long userId, MemoRequest request) {
        String title = request.getTitle() == null ? "" : request.getTitle().trim();
        if (title.isEmpty()) {
            throw new BusinessException("给这条备忘起个标题吧");
        }

        Memo memo = new Memo();
        memo.setUserId(userId);
        memo.setTitle(title);
        memo.setContent(request.getContent());
        memo.setDone(normalizeFlag(request.getDone()));
        memo.setPinned(normalizeFlag(request.getPinned()));
        memo.setColor(request.getColor() == null ? DEFAULT_COLOR : request.getColor());
        memoMapper.insert(memo);
        return memo;
    }

    @Override
    public Memo update(Long userId, Long id, MemoRequest request) {
        Memo memo = owned(userId, id);

        if (request.getTitle() != null) {
            String title = request.getTitle().trim();
            if (title.isEmpty()) {
                throw new BusinessException("标题不能为空");
            }
            memo.setTitle(title);
        }
        if (request.getContent() != null) {
            memo.setContent(request.getContent());
        }
        if (request.getColor() != null) {
            memo.setColor(request.getColor());
        }
        if (request.getDone() != null) {
            memo.setDone(normalizeFlag(request.getDone()));
        }
        if (request.getPinned() != null) {
            memo.setPinned(normalizeFlag(request.getPinned()));
        }

        memoMapper.updateById(memo);
        return memoMapper.selectById(id);
    }

    @Override
    public void delete(Long userId, Long id) {
        owned(userId, id);
        memoMapper.deleteById(id);
    }

    /** 只取「属于当前用户」的那一行，越权与不存在统一给可读提示 */
    private Memo owned(Long userId, Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException("请指定要操作的备忘");
        }
        Memo memo = memoMapper.selectOne(new LambdaQueryWrapper<Memo>()
                .eq(Memo::getId, id)
                .eq(Memo::getUserId, userId));
        if (memo == null) {
            throw new BusinessException("这条备忘不存在，或不属于你");
        }
        return memo;
    }

    /** 只认 0/1，其余一律按 0 处理，避免脏值 */
    private int normalizeFlag(Integer value) {
        return value != null && value == 1 ? 1 : 0;
    }
}
