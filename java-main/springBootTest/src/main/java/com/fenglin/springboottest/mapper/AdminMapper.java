package com.fenglin.springboottest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fenglin.springboottest.entity.Admin;
import org.apache.ibatis.annotations.Mapper;

/**
 * 后台管理员 Mapper
 */
@Mapper
public interface AdminMapper extends BaseMapper<Admin> {
}
