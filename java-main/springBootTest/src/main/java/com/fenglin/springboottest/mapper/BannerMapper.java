package com.fenglin.springboottest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fenglin.springboottest.dto.BannerVO;
import com.fenglin.springboottest.entity.Banner;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 轮播标语 Mapper
 */
@Mapper
public interface BannerMapper extends BaseMapper<Banner> {

    /**
     * 查询当前展示中的轮播标语（按排序、ID 升序，最多 8 条）。
     */
    @Select("SELECT id, title, subtitle, tag, accent " +
            "FROM banner " +
            "WHERE deleted = 0 AND status = 1 " +
            "ORDER BY sort ASC, id ASC LIMIT 8")
    List<BannerVO> listActive();
}
