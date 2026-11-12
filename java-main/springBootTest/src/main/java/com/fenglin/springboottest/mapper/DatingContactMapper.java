package com.fenglin.springboottest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fenglin.springboottest.entity.DatingContact;
import org.apache.ibatis.annotations.Mapper;

/** 姻缘阁联系方式 Mapper */
@Mapper
public interface DatingContactMapper extends BaseMapper<DatingContact> {
}
