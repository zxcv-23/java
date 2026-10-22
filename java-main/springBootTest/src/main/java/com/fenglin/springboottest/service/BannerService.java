package com.fenglin.springboottest.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fenglin.springboottest.dto.BannerVO;
import com.fenglin.springboottest.entity.Banner;

import java.util.List;

/**
 * 轮播标语服务接口
 */
public interface BannerService extends IService<Banner> {

    /**
     * 查询当前展示中的轮播标语
     */
    List<BannerVO> listActive();
}
