package com.fenglin.springboottest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fenglin.springboottest.entity.DatingWork;
import org.apache.ibatis.annotations.Mapper;

/**
 * 姻缘阁作品 Mapper：条件全部由 Service 用 LambdaQueryWrapper 拼装，
 * 涉及计数的更新（点赞数 +1/-1）用 update wrapper 走原子 SQL，避免并发覆盖。
 */
@Mapper
public interface DatingWorkMapper extends BaseMapper<DatingWork> {
}
