package com.gig.collide.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gig.collide.domain.Ad;
import com.gig.collide.mapper.AdMapper;
import com.gig.collide.service.AdService;
import com.gig.collide.Apientry.api.common.response.Result;
import com.gig.collide.Apientry.api.common.response.PageResponse;
import com.gig.collide.Apientry.api.ads.response.AdResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 广告业务服务实现�?- 极简�?
 *
 * @author GIG Team
 * @version 3.0.0 (极简�?
 * @since 2024-01-16
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdServiceImpl implements AdService {

    private final AdMapper adMapper;

    @Override
    public Ad createAd(Ad ad) {
        ad.setCreateTime(LocalDateTime.now());
        ad.setUpdateTime(LocalDateTime.now());
        if (ad.getIsActive() == null) {
            ad.setIsActive(1);
        }
        if (ad.getSortOrder() == null) {
            ad.setSortOrder(0);
        }
        adMapper.insert(ad);
        return ad;
    }

    @Override
    public Ad getAdById(Long id) {
        return adMapper.selectById(id);
    }

    @Override
    public boolean updateAd(Ad ad) {
        ad.setUpdateTime(LocalDateTime.now());
        return adMapper.updateById(ad) > 0;
    }

    @Override
    public boolean deleteAd(Long id) {
        return adMapper.deleteById(id) > 0;
    }

    @Override
    public Page<Ad> queryAds(String adName, String adType, Integer isActive, Integer currentPage, Integer pageSize) {
        LambdaQueryWrapper<Ad> wrapper = Wrappers.lambdaQuery();

        if (StringUtils.hasText(adName)) {
            wrapper.like(Ad::getAdName, adName);
        }

        if (StringUtils.hasText(adType)) {
            wrapper.eq(Ad::getAdType, adType);
        }

        if (isActive != null) {
            wrapper.eq(Ad::getIsActive, isActive);
        }

        wrapper.orderByDesc(Ad::getSortOrder, Ad::getId);

        Page<Ad> page = new Page<>(currentPage, pageSize);
        return adMapper.selectPage(page, wrapper);
    }

    @Override
    public List<Ad> getAdsByType(String adType) {
        return adMapper.findByAdType(adType);
    }

    @Override
    public Ad getRandomAdByType(String adType) {
        List<Ad> ads = adMapper.findRandomByAdType(adType, 1);
        return ads.isEmpty() ? null : ads.get(0);
    }

    @Override
    public List<Ad> getRandomAdsByType(String adType, Integer limit) {
        return adMapper.findRandomByAdType(adType, limit);
    }

    @Override
    public List<String> getAllAdTypes() {
        LambdaQueryWrapper<Ad> wrapper = Wrappers.lambdaQuery();
        wrapper.select(Ad::getAdType)
                .eq(Ad::getIsActive, 1)
                .groupBy(Ad::getAdType);

        List<Ad> ads = adMapper.selectList(wrapper);
        return ads.stream()
                .map(Ad::getAdType)
                .collect(Collectors.toList());
    }

    @Override
    public List<Ad> getTopRecommendedAds(Integer limit) {
        LambdaQueryWrapper<Ad> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(Ad::getIsActive, 1)
                .orderByDesc(Ad::getSortOrder, Ad::getId)
                .last("LIMIT " + limit);

        return adMapper.selectList(wrapper);
    }

    @Override
    public List<Ad> getAllEnabledAds() {
        LambdaQueryWrapper<Ad> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(Ad::getIsActive, 1)
                .orderByDesc(Ad::getSortOrder, Ad::getId);

        return adMapper.selectList(wrapper);
    }

    @Override
    public boolean enableAd(Long id) {
        LambdaUpdateWrapper<Ad> wrapper = Wrappers.lambdaUpdate();
        wrapper.eq(Ad::getId, id)
                .set(Ad::getIsActive, 1)
                .set(Ad::getUpdateTime, LocalDateTime.now());

        return adMapper.update(null, wrapper) > 0;
    }

    @Override
    public boolean disableAd(Long id) {
        LambdaUpdateWrapper<Ad> wrapper = Wrappers.lambdaUpdate();
        wrapper.eq(Ad::getId, id)
                .set(Ad::getIsActive, 0)
                .set(Ad::getUpdateTime, LocalDateTime.now());

        return adMapper.update(null, wrapper) > 0;
    }

    @Override
    public boolean batchUpdateAdStatus(List<Long> ids, Integer status) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }

        LambdaUpdateWrapper<Ad> wrapper = Wrappers.lambdaUpdate();
        wrapper.in(Ad::getId, ids)
                .set(Ad::getIsActive, status)
                .set(Ad::getUpdateTime, LocalDateTime.now());

        return adMapper.update(null, wrapper) > 0;
    }

    @Override
    public boolean updateAdSortOrder(Long id, Integer sortOrder) {
        LambdaUpdateWrapper<Ad> wrapper = Wrappers.lambdaUpdate();
        wrapper.eq(Ad::getId, id)
                .set(Ad::getSortOrder, sortOrder)
                .set(Ad::getUpdateTime, LocalDateTime.now());

        return adMapper.update(null, wrapper) > 0;
    }

    @Override
    public boolean batchUpdateAdSortOrder(List<Long> ids, List<Integer> sortOrders) {
        if (ids == null || ids.isEmpty() || sortOrders == null ||
                ids.size() != sortOrders.size()) {
            return false;
        }

        try {
            for (int i = 0; i < ids.size(); i++) {
                updateAdSortOrder(ids.get(i), sortOrders.get(i));
            }
            return true;
        } catch (Exception e) {
            log.error("批量更新广告权重失败", e);
            return false;
        }
    }

    // =================== Controller专用方法 ===================

    @Override
    public Result<PageResponse<AdResponse>> listAdsForController(Long id, String adType, String status, String position, String keyword, Integer isValid,
                                                               String orderBy, String orderDirection, Integer currentPage, Integer pageSize) {
        try {
            log.info("Controller层 - 广告列表查询: id={}, adType={}, keyword={}, isValid={}, orderBy={}, orderDirection={}, page={}, size={}",
                    id, adType, keyword, isValid, orderBy, orderDirection, currentPage, pageSize);

            // 参数验证和默认值设置
            if (currentPage == null || currentPage < 1) {
                currentPage = 1;
            }
            if (pageSize == null || pageSize < 1 || pageSize > 100) {
                pageSize = 20;
            }
            if (orderBy == null || orderBy.trim().isEmpty()) {
                orderBy = "createTime";
            }
            if (orderDirection == null || orderDirection.trim().isEmpty()) {
                orderDirection = "DESC";
            }

            // 构建查询条件
            Page<Ad> page = new Page<>(currentPage, pageSize);
            
            // 调用Mapper查询
            IPage<Ad> adPage = adMapper.selectAdList(page, id, adType, keyword, isValid, orderBy, orderDirection);
            
            // 转换为Response对象
            List<AdResponse> responses = adPage.getRecords().stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
            
            // 构建分页响应 - 按照其他模块的样式手动设置分页信息
            PageResponse<AdResponse> pageResponse = new PageResponse<>();
            pageResponse.setRecords(responses);
            pageResponse.setTotal(adPage.getTotal());
            pageResponse.setCurrentPage(currentPage);
            pageResponse.setPageSize(pageSize);
            pageResponse.setTotalPages((int) Math.ceil((double) adPage.getTotal() / pageSize));
            
            log.info("广告列表查询成功: 总数={}, 当前页={}, 页面大小={}", 
                    adPage.getTotal(), currentPage, pageSize);
            return Result.success(pageResponse);
            
        } catch (Exception e) {
            log.error("Controller层 - 广告列表查询失败", e);
            return Result.error("广告列表查询失败: " + e.getMessage());
        }
    }

    /**
     * 将Ad实体转换为AdResponse
     */
    private AdResponse convertToResponse(Ad ad) {
        if (ad == null) {
            return null;
        }
        
        AdResponse response = new AdResponse();
        response.setId(ad.getId());
        response.setAdName(ad.getAdName());
        response.setAdTitle(ad.getAdTitle());
        response.setAdDescription(ad.getAdDescription());
        response.setAdType(ad.getAdType());
        response.setImageUrl(ad.getImageUrl());
        response.setClickUrl(ad.getClickUrl());
        response.setAltText(ad.getAltText());
        response.setTargetType(ad.getTargetType());
        response.setIsActive(ad.getIsActive());
        response.setSortOrder(ad.getSortOrder());
        response.setCreateTime(ad.getCreateTime());
        response.setUpdateTime(ad.getUpdateTime());
        
        return response;
    }
}
