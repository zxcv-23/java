package com.fenglin.springboottest.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fenglin.springboottest.dto.CategoryVO;
import com.fenglin.springboottest.entity.Category;
import com.fenglin.springboottest.mapper.CategoryMapper;
import com.fenglin.springboottest.service.CategoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 分类服务实现
 */
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Override
    public List<CategoryVO> tree() {
        List<Category> all = list();
        Map<Long, CategoryVO> voMap = new LinkedHashMap<>();
        for (Category c : all) {
            CategoryVO vo = new CategoryVO();
            BeanUtils.copyProperties(c, vo);
            vo.setChildren(new ArrayList<>());
            voMap.put(c.getId(), vo);
        }

        List<CategoryVO> roots = new ArrayList<>();
        for (CategoryVO vo : voMap.values()) {
            Long pid = vo.getParentId();
            if (pid == null || pid == 0) {
                roots.add(vo);
            } else {
                CategoryVO parent = voMap.get(pid);
                if (parent != null) {
                    parent.getChildren().add(vo);
                } else {
                    // 父分类不存在则归为根，避免孤儿节点丢失
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
}
