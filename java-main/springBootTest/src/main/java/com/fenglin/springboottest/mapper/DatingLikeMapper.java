package com.fenglin.springboottest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fenglin.springboottest.entity.DatingLike;
import org.apache.ibatis.annotations.Mapper;

/** 作品点赞 Mapper */
@Mapper
public interface DatingLikeMapper extends BaseMapper<DatingLike> {
}
