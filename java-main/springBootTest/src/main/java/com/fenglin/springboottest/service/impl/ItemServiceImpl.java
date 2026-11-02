package com.fenglin.springboottest.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fenglin.springboottest.dto.HomeVO;
import com.fenglin.springboottest.dto.ItemManageVO;
import com.fenglin.springboottest.dto.ItemSearchVO;
import com.fenglin.springboottest.dto.ItemUpdateRequest;
import com.fenglin.springboottest.entity.Item;
import com.fenglin.springboottest.exception.BusinessException;
import com.fenglin.springboottest.mapper.ItemMapper;
import com.fenglin.springboottest.service.BannerService;
import com.fenglin.springboottest.service.CategoryService;
import com.fenglin.springboottest.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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

    @Override
    public List<ItemManageVO> manageList(String keyword, Long categoryId) {
        return baseMapper.manageList(keyword, categoryId);
    }

    @Override
    public ItemManageVO update(Long id, ItemUpdateRequest request) {
        Item exist = getById(id);
        if (exist == null) {
            throw new BusinessException("此条目已不在生死簿中（可能已被删除）");
        }

        // 分类可空；给了值就必须真实存在，否则清成 0（未分类）
        Long categoryId = request.getCategoryId();
        if (categoryId != null && categoryService.getById(categoryId) == null) {
            throw new BusinessException("所选分类不存在，请重新选择");
        }

        // 局部更新：请求里没传（null）的字段保持原值，传了空串才清空。
        // 这样编辑时只改某个字段，不会把图标/描述等其它字段误清空。
        LambdaUpdateWrapper<Item> update = new LambdaUpdateWrapper<Item>()
                .eq(Item::getId, id);

        // 标题必填（@NotBlank 已保证非空白）
        if (request.getTitle() != null) {
            update.set(Item::getTitle, request.getTitle().trim());
        }
        // 其余字段：非 null 才 set；null 表示保持原值
        if (request.getDescription() != null) {
            update.set(Item::getDescription, request.getDescription().trim());
        }
        if (request.getEmoji() != null) {
            update.set(Item::getEmoji, request.getEmoji().trim());
        }
        if (request.getTag() != null) {
            update.set(Item::getTag, request.getTag().trim());
        }
        if (request.getContent() != null) {
            update.set(Item::getContent, request.getContent());
        }
        if (request.getAuthor() != null) {
            update.set(Item::getAuthor, request.getAuthor().trim());
        }
        // 分类：传了才改；传 0 或具体分类ID 都可；未传则保持原分类
        if (categoryId != null) {
            update.set(Item::getCategoryId, categoryId);
        }
        update.set(Item::getUpdateTime, LocalDateTime.now());

        if (request.getStatus() != null) {
            update.set(Item::getStatus, request.getStatus());
        }
        update(update);

        return baseMapper.manageItem(id);
    }

    @Override
    public ItemManageVO create(ItemUpdateRequest request) {
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new BusinessException("标题不能为空");
        }
        Long categoryId = request.getCategoryId();
        if (categoryId != null && categoryService.getById(categoryId) == null) {
            throw new BusinessException("所选分类不存在，请重新选择");
        }

        Item item = new Item();
        item.setTitle(request.getTitle().trim());
        // 未传的字段默认空串；分类默认 0（未分类）；状态默认上线
        item.setDescription(request.getDescription() == null ? "" : request.getDescription().trim());
        item.setEmoji(request.getEmoji() == null ? "" : request.getEmoji().trim());
        item.setTag(request.getTag() == null ? "" : request.getTag().trim());
        item.setContent(request.getContent() == null ? "" : request.getContent());
        item.setAuthor(request.getAuthor() == null ? "" : request.getAuthor().trim());
        item.setCategoryId(categoryId == null ? 0L : categoryId);
        item.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        item.setDeleted(0);
        // createTime / updateTime 由 MyMetaObjectHandler 自动填充
        save(item);
        return baseMapper.manageItem(item.getId());
    }

    @Override
    public ItemManageVO changeStatus(Long id, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException("状态只能是 0（下线）或 1（上线）");
        }
        Item exist = getById(id);
        if (exist == null) {
            throw new BusinessException("此条目已不在生死簿中（可能已被删除）");
        }
        update(new LambdaUpdateWrapper<Item>()
                .eq(Item::getId, id)
                .set(Item::getStatus, status)
                .set(Item::getUpdateTime, LocalDateTime.now()));
        return baseMapper.manageItem(id);
    }

    @Override
    public void remove(Long id) {
        Item exist = getById(id);
        if (exist == null) {
            throw new BusinessException("此条目已不在生死簿中（可能已被删除）");
        }
        // @TableLogic：走逻辑删除，deleted 置 1，数据仍保留在库中
        if (!removeById(id)) {
            throw new BusinessException("删除失败，请稍后再试");
        }
    }
}
