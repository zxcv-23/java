package com.fenglin.springboottest.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fenglin.springboottest.dto.BannerRequest;
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

    /** 后台列表（含隐藏的标语） */
    List<BannerVO> manageList();

    /** 新增轮播标语 */
    BannerVO create(BannerRequest request);

    /** 局部修改（null 字段保持原值） */
    BannerVO update(Long id, BannerRequest request);

    /** 展示 / 隐藏 */
    BannerVO changeStatus(Long id, Integer status);

    /** 逻辑删除 */
    void remove(Long id);
}
