package com.fenglin.springboottest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fenglin.springboottest.entity.DatingComment;
import org.apache.ibatis.annotations.Mapper;

/** 作品评论 Mapper */
@Mapper
public interface DatingCommentMapper extends BaseMapper<DatingComment> {
}
