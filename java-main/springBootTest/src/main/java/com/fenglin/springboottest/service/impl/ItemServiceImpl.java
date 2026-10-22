package com.fenglin.springboottest.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fenglin.springboottest.dto.HomeVO;
import com.fenglin.springboottest.dto.ItemSearchVO;
import com.fenglin.springboottest.entity.Item;
import com.fenglin.springboottest.mapper.ItemMapper;
import com.fenglin.springboottest.service.BannerService;
import com.fenglin.springboottest.service.CategoryService;
import com.fenglin.springboottest.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 条目服务实现
 */
@Service
public class ItemServiceImpl extends ServiceImpl<ItemMapper, Item> implements ItemService {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BannerService bannerService;

    @Override
    public List<ItemSearchVO> search(String keyword, Long categoryId) {
        return baseMapper.search(keyword, categoryId);
    }

    @Override
    public List<ItemSearchVO> featured() {
        return baseMapper.featured();
    }

    @Override
    public List<String> hotTags() {
        return baseMapper.hotTags();
    }

    @Override
    public HomeVO home() {
        HomeVO vo = new HomeVO();
        vo.setCategories(categoryService.tree());
        vo.setFeatured(baseMapper.featured());
        vo.setHotTags(baseMapper.hotTags());
        vo.setBanners(bannerService.listActive());
        return vo;
    }
}
