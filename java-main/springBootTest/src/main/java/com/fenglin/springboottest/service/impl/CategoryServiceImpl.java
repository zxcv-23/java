package com.fenglin.springboottest.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fenglin.springboottest.dto.CategoryRequest;
import com.fenglin.springboottest.dto.CategoryVO;
import com.fenglin.springboottest.entity.Category;
import com.fenglin.springboottest.entity.Item;
import com.fenglin.springboottest.exception.BusinessException;
import com.fenglin.springboottest.mapper.CategoryMapper;
import com.fenglin.springboottest.mapper.ItemMapper;
import com.fenglin.springboottest.service.CategoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 分类服务实现：首页分类树 + 后台分类维护
 */
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Autowired
    private ItemMapper itemMapper;

    @Override
    public List<CategoryVO> tree() {
        List<CategoryVO> all = new ArrayList<>();
        for (Category c : list()) {
            CategoryVO vo = new CategoryVO();
            BeanUtils.copyProperties(c, vo);
            vo.setChildren(new ArrayList<>());
            all.add(vo);
        }
        return buildTree(all);
    }

    @Override
    public List<CategoryVO> manageTree() {
        List<CategoryVO> all = baseMapper.manageFlat();
        for (CategoryVO vo : all) {
            if (vo.getChildren() == null) {
                vo.setChildren(new ArrayList<>());
            }
        }
        return buildTree(all);
    }

    /** 把平铺列表组装成树；父分类缺失的孤儿节点仍作为根节点展示，避免数据丢失 */
    private List<CategoryVO> buildTree(List<CategoryVO> all) {
        Map<Long, CategoryVO> voMap = new LinkedHashMap<>();
        for (CategoryVO vo : all) {
            voMap.put(vo.getId(), vo);
        }
        List<CategoryVO> roots = new ArrayList<>();
        for (CategoryVO vo : all) {
            Long pid = vo.getParentId();
            if (pid == null || pid == 0) {
                roots.add(vo);
            } else {
                CategoryVO parent = voMap.get(pid);
                if (parent != null) {
                    parent.getChildren().add(vo);
                } else {
                    roots.add(vo);
                }
            }
        }
        sortRec(roots);
        return roots;
    }

    private void sortRec(List<CategoryVO> list) {
        list.sort(Comparator.comparingInt(c -> c.getSort() == null ? 0 : c.getSort()));
        for (CategoryVO vo : list) {
            if (vo.getChildren() != null) {
                sortRec(vo.getChildren());
            }
        }
    }

    @Override
    public CategoryVO create(CategoryRequest request) {
        Long parentId = normalizeParent(request.getParentId());
        int level = 1;
        if (parentId != 0L) {
            Category parent = getById(parentId);
            if (parent == null) {
                throw new BusinessException("父分类不存在，请重新选择");
            }
            level = (parent.getLevel() == null ? 1 : parent.getLevel()) + 1;
        }
        if (request.getLevel() != null) {
            level = request.getLevel();
        }
        String name = request.getName().trim();
        Long dup = count(new LambdaQueryWrapper<Category>()
                .eq(Category::getParentId, parentId)
                .eq(Category::getName, name));
        if (dup != null && dup > 0) {
            throw new BusinessException("同一层级下已有同名分类");
        }

        Category category = new Category();
        category.setParentId(parentId);
        category.setName(name);
        category.setLevel(level);
        category.setIcon(request.getIcon() == null ? "" : request.getIcon().trim());
        category.setSort(request.getSort() == null ? 0 : request.getSort());
        category.setDescription(request.getDescription() == null ? "" : request.getDescription().trim());
        category.setDeleted(0);
        // createTime / updateTime 由 MyMetaObjectHandler 自动填充
        save(category);
        return toVO(category);
    }

    @Override
    public CategoryVO update(Long id, CategoryRequest request) {
        Category exist = getById(id);
        if (exist == null) {
            throw new BusinessException("分类不存在或已删除");
        }
        // update 接口不加 @Valid 以支持局部修改，这里手动兜住「传了但为空白」的情况
        if (request.getName() != null && request.getName().trim().isEmpty()) {
            throw new BusinessException("分类名称不能为空");
        }

        Long parentId = request.getParentId();
        if (parentId != null) {
            if (parentId.equals(id)) {
                throw new BusinessException("不能把分类挂在自己下面");
            }
            if (parentId != 0L) {
                Category parent = getById(parentId);
                if (parent == null) {
                    throw new BusinessException("父分类不存在，请重新选择");
                }
                // 防环：从新父节点一路往上找，若遇到自己，说明把分类挂到了自己的子孙下
                Long cursor = parent.getParentId();
                int guard = 0;
                while (cursor != null && cursor != 0L && guard++ < 20) {
                    if (cursor.equals(id)) {
                        throw new BusinessException("不能把分类挂到自己的子分类下");
                    }
                    Category up = getById(cursor);
                    cursor = up == null ? 0L : up.getParentId();
                }
            }
        }

        // 局部更新：null 保持原值，传空串才清空
        LambdaUpdateWrapper<Category> update = new LambdaUpdateWrapper<Category>()
                .eq(Category::getId, id);
        if (request.getName() != null) {
            update.set(Category::getName, request.getName().trim());
        }
        if (request.getIcon() != null) {
            update.set(Category::getIcon, request.getIcon().trim());
        }
        if (request.getSort() != null) {
            update.set(Category::getSort, request.getSort());
        }
        if (request.getDescription() != null) {
            update.set(Category::getDescription, request.getDescription().trim());
        }
        if (request.getLevel() != null) {
            update.set(Category::getLevel, request.getLevel());
        }
        if (parentId != null) {
            update.set(Category::getParentId, parentId);
        }
        update.set(Category::getUpdateTime, LocalDateTime.now());
        update(update);
        return toVO(getById(id));
    }

    @Override
    public void remove(Long id) {
        Category exist = getById(id);
        if (exist == null) {
            throw new BusinessException("分类不存在或已删除");
        }
        long children = count(new LambdaQueryWrapper<Category>().eq(Category::getParentId, id));
        if (children > 0) {
            throw new BusinessException("该分类下还有 " + children + " 个子分类，请先处理子分类");
        }
        Long items = itemMapper.selectCount(new LambdaQueryWrapper<Item>()
                .eq(Item::getCategoryId, id));
        if (items != null && items > 0) {
            throw new BusinessException("该分类下还有 " + items + " 件藏品，请先转移或删除");
        }
        // @TableLogic：逻辑删除，数据仍保留
        removeById(id);
    }

    private Long normalizeParent(Long parentId) {
        return parentId == null || parentId < 0 ? 0L : parentId;
    }

    private CategoryVO toVO(Category category) {
        CategoryVO vo = new CategoryVO();
        BeanUtils.copyProperties(category, vo);
        vo.setChildren(new ArrayList<>());
        vo.setItemCount(0L);
        return vo;
    }
}
