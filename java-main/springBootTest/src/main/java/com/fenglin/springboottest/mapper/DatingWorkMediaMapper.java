package com.fenglin.springboottest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fenglin.springboottest.entity.DatingWorkMedia;
import org.apache.ibatis.annotations.Mapper;

/** 作品媒体（照片 / 视频）Mapper */
@Mapper
public interface DatingWorkMediaMapper extends BaseMapper<DatingWorkMedia> {
}
