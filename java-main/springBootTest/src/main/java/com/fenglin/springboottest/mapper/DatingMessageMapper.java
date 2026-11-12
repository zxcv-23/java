package com.fenglin.springboottest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fenglin.springboottest.entity.DatingMessage;
import org.apache.ibatis.annotations.Mapper;

/** 站内私信 Mapper */
@Mapper
public interface DatingMessageMapper extends BaseMapper<DatingMessage> {
}
