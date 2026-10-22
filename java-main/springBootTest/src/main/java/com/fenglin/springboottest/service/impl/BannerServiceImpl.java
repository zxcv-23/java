package com.fenglin.springboottest.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fenglin.springboottest.dto.BannerVO;
import com.fenglin.springboottest.entity.Banner;
import com.fenglin.springboottest.mapper.BannerMapper;
import com.fenglin.springboottest.service.BannerService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 轮播标语服务实现
 */
@Service
public class BannerServiceImpl extends ServiceImpl<BannerMapper, Banner> implements BannerService {

    @Override
    public List<BannerVO> listActive() {
        return baseMapper.listActive();
    }
}
