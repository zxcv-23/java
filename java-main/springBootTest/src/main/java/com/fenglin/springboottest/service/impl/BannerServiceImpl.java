package com.fenglin.springboottest.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fenglin.springboottest.dto.BannerRequest;
import com.fenglin.springboottest.dto.BannerVO;
import com.fenglin.springboottest.entity.Banner;
import com.fenglin.springboottest.exception.BusinessException;
import com.fenglin.springboottest.mapper.BannerMapper;
import com.fenglin.springboottest.service.BannerService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 轮播标语服务实现：前台展示 + 后台维护
 */
@Service
public class BannerServiceImpl extends ServiceImpl<BannerMapper, Banner> implements BannerService {

    @Override
    public List<BannerVO> listActive() {
        return baseMapper.listActive();
    }

    @Override
    public List<BannerVO> manageList() {
        return baseMapper.manageList();
    }

    @Override
    public BannerVO create(BannerRequest request) {
        Banner banner = new Banner();
        banner.setTitle(request.getTitle().trim());
        banner.setSubtitle(request.getSubtitle() == null ? "" : request.getSubtitle().trim());
        banner.setTag(request.getTag() == null ? "" : request.getTag().trim());
        banner.setAccent(request.getAccent() == null ? "" : request.getAccent().trim());
        banner.setSort(request.getSort() == null ? 0 : request.getSort());
        banner.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        banner.setDeleted(0);
        save(banner);
        return toVO(banner);
    }

    @Override
    public BannerVO update(Long id, BannerRequest request) {
        Banner exist = getById(id);
        if (exist == null) {
            throw new BusinessException("轮播标语不存在或已删除");
        }
        // update 接口不加 @Valid 以支持局部修改，这里手动兜住「传了但为空白」的情况
        if (request.getTitle() != null && request.getTitle().trim().isEmpty()) {
            throw new BusinessException("主标题不能为空");
        }
        // 局部更新：null 保持原值
        LambdaUpdateWrapper<Banner> update = new LambdaUpdateWrapper<Banner>()
                .eq(Banner::getId, id);
        if (request.getTitle() != null) {
            update.set(Banner::getTitle, request.getTitle().trim());
        }
        if (request.getSubtitle() != null) {
            update.set(Banner::getSubtitle, request.getSubtitle().trim());
        }
        if (request.getTag() != null) {
            update.set(Banner::getTag, request.getTag().trim());
        }
        if (request.getAccent() != null) {
            update.set(Banner::getAccent, request.getAccent().trim());
        }
        if (request.getSort() != null) {
            update.set(Banner::getSort, request.getSort());
        }
        if (request.getStatus() != null) {
            update.set(Banner::getStatus, request.getStatus());
        }
        update.set(Banner::getUpdateTime, LocalDateTime.now());
        update(update);
        return toVO(getById(id));
    }

    @Override
    public BannerVO changeStatus(Long id, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException("状态只能是 0（隐藏）或 1（展示）");
        }
        Banner exist = getById(id);
        if (exist == null) {
            throw new BusinessException("轮播标语不存在或已删除");
        }
        update(new LambdaUpdateWrapper<Banner>()
                .eq(Banner::getId, id)
                .set(Banner::getStatus, status)
                .set(Banner::getUpdateTime, LocalDateTime.now()));
        return toVO(getById(id));
    }

    @Override
    public void remove(Long id) {
        Banner exist = getById(id);
        if (exist == null) {
            throw new BusinessException("轮播标语不存在或已删除");
        }
        removeById(id);
    }

    private BannerVO toVO(Banner banner) {
        BannerVO vo = new BannerVO();
        BeanUtils.copyProperties(banner, vo);
        return vo;
    }
}
