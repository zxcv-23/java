package com.fenglin.springboottest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fenglin.springboottest.entity.Memo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 备忘录 Mapper：只用 MyBatis-Plus 的 BaseMapper，条件由 Service 层用
 * LambdaQueryWrapper 拼装（始终带 user_id），不写裸 SQL。
 */
@Mapper
public interface MemoMapper extends BaseMapper<Memo> {
}
