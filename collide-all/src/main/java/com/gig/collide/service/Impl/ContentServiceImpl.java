package com.gig.collide.service.Impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gig.collide.Apientry.api.common.response.PageResponse;
import com.gig.collide.Apientry.api.common.response.Result;
import com.gig.collide.Apientry.api.content.response.ContentResponse;
import com.gig.collide.domain.Content;
import com.gig.collide.domain.ContentPayment;
import com.gig.collide.mapper.ContentMapper;
import com.gig.collide.service.ContentService;
import com.gig.collide.service.ContentPaymentService;
import com.gig.collide.service.FollowService;
import com.gig.collide.service.LikeService;
import com.gig.collide.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 内容服务实现
 * 极简化 - 12个核心方法，使用通用查询
 *
 * @author GIG Team
 * @version 2.0.0 (内容付费版)
 * @since 2024-01-31
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class ContentServiceImpl implements ContentService {

    private final ContentMapper contentMapper;
    private final ContentPaymentService contentPaymentService;
    private final FollowService followService;
    private final LikeService likeService;
    private final FavoriteService favoriteService;

    // =================== 核心CRUD功能（4个方法）===================

    @Override
    public Content createContent(Content content) {
        log.info("创建内容: title={}, authorId={}", content.getTitle(), content.getAuthorId());

        // 基础验证
        if (!StringUtils.hasText(content.getTitle()) || content.getAuthorId() == null) {
            throw new IllegalArgumentException("标题和作者ID不能为空");
        }

        // 设置默认值
        if (content.getCreateTime() == null) {
            content.setCreateTime(LocalDateTime.now());
        }
        if (content.getUpdateTime() == null) {
            content.setUpdateTime(LocalDateTime.now());
        }
        if (!StringUtils.hasText(content.getStatus())) {
            content.setStatus("DRAFT");
        }
        if (!StringUtils.hasText(content.getReviewStatus())) {
            content.setReviewStatus("PENDING");
        }

        // 初始化统计数据
        if (content.getViewCount() == null) {
            content.setViewCount(0L);
        }
        if (content.getLikeCount() == null) {
            content.setLikeCount(0L);
        }
        if (content.getCommentCount() == null) {
            content.setCommentCount(0L);
        }
        if (content.getFavoriteCount() == null) {
            content.setFavoriteCount(0L);
        }

        contentMapper.insert(content);
        log.info("内容创建成功: id={}", content.getId());
        return content;
    }

    /**
     * 创建内容并同步创建付费配置
     */
    public Content createContentWithPayment(Content content, String paymentType, Long coinPrice, 
                                          Long originalPrice, Boolean vipFree, Boolean vipOnly, 
                                          Boolean trialEnabled, Integer trialWordCount, 
                                          Boolean isPermanent, Integer validDays) {
        log.info("创建内容并同步付费配置: title={}, paymentType={}, coinPrice={}", 
                content.getTitle(), paymentType, coinPrice);

        // 先创建内容
        Content createdContent = createContent(content);

        // 创建付费配置
        ContentPayment paymentConfig = new ContentPayment();
        paymentConfig.setContentId(createdContent.getId());
        paymentConfig.setPaymentType(paymentType != null ? paymentType : "FREE");
        paymentConfig.setCoinPrice(coinPrice != null ? coinPrice : 0L);
        paymentConfig.setOriginalPrice(originalPrice);
        paymentConfig.setVipFree(vipFree != null ? vipFree : false);
        paymentConfig.setVipOnly(vipOnly != null ? vipOnly : false);
        paymentConfig.setTrialEnabled(trialEnabled != null ? trialEnabled : false);
        paymentConfig.setTrialWordCount(trialWordCount != null ? trialWordCount : 0);
        paymentConfig.setIsPermanent(isPermanent != null ? isPermanent : true);
        paymentConfig.setValidDays(validDays);
        paymentConfig.setTotalSales(0L);
        paymentConfig.setTotalRevenue(0L);
        paymentConfig.setStatus("ACTIVE");
        paymentConfig.setCreateTime(LocalDateTime.now());
        paymentConfig.setUpdateTime(LocalDateTime.now());

        contentPaymentService.createPaymentConfig(paymentConfig);
        log.info("付费配置创建成功: contentId={}, paymentType={}", 
                createdContent.getId(), paymentConfig.getPaymentType());

        return createdContent;
    }

    @Override
    public Content updateContent(Content content) {
        log.info("更新内容: id={}", content.getId());

        if (content.getId() == null) {
            throw new IllegalArgumentException("内容ID不能为空");
        }

        content.setUpdateTime(LocalDateTime.now());
        contentMapper.updateById(content);

        log.info("内容更新成功: id={}", content.getId());
        return content;
    }

    @Override
    public Content getContentById(Long id, Boolean includeOffline) {
        log.debug("获取内容详情: id={}, includeOffline={}", id, includeOffline);

        if (id == null) {
            throw new IllegalArgumentException("内容ID不能为空");
        }

        Content content = contentMapper.selectById(id);

        // 如果不包含下线内容，检查状态
        if (content != null && Boolean.FALSE.equals(includeOffline)) {
            if (!"PUBLISHED".equals(content.getStatus())) {
                return null;
            }
        }

        return content;
    }

    @Override
    public boolean deleteContent(Long contentId, Long operatorId) {
        log.info("软删除内容: contentId={}, operatorId={}", contentId, operatorId);

        if (contentId == null) {
            throw new IllegalArgumentException("内容ID不能为空");
        }

        try {
            int result = contentMapper.softDeleteContent(contentId);
            boolean success = result > 0;
            if (success) {
                log.info("内容软删除成功: contentId={}", contentId);
            }
            return success;
        } catch (Exception e) {
            log.error("内容软删除失败: contentId={}", contentId, e);
            return false;
        }
    }

    // =================== 万能查询功能（3个方法）===================

    @Override
    public List<Content> getContentsByConditions(Long authorId, Long categoryId, String contentType,
                                                 String status, String reviewStatus, Double minScore,
                                                 Integer timeRange, String orderBy, String orderDirection,
                                                 Integer currentPage, Integer pageSize) {
        log.debug("万能条件查询内容: authorId={}, categoryId={}, contentType={}, timeRange={}",
                authorId, categoryId, contentType, timeRange);

        return contentMapper.selectContentsByConditions(
                authorId, categoryId, contentType, status, reviewStatus, minScore,
                timeRange, orderBy, orderDirection, currentPage, pageSize
        );
    }

    @Override
    public List<Content> searchContents(String keyword, String contentType, Long categoryId,
                                        Integer currentPage, Integer pageSize) {
        log.debug("搜索内容: keyword={}, contentType={}", keyword, contentType);

        if (!StringUtils.hasText(keyword)) {
            throw new IllegalArgumentException("搜索关键词不能为空");
        }

        return contentMapper.searchContents(keyword, contentType, categoryId, currentPage, pageSize);
    }

    @Override
    public List<Content> getRecommendedContents(Long userId, List<Long> excludeContentIds, Integer limit) {
        log.debug("获取推荐内容: userId={}, excludeContentIds.size={}, limit={}",
                userId, excludeContentIds != null ? excludeContentIds.size() : 0, limit);

        return contentMapper.getRecommendedContents(userId, excludeContentIds, limit);
    }

    @Override
    public List<Content> getRandomContents(Integer limit) {
        log.debug("获取随机内容: limit={}", limit);
        
        if (limit == null || limit <= 0) {
            limit = 5; // 默认返回5个
        }
        if (limit > 50) {
            limit = 50; // 最大限制50个
        }
        
        return contentMapper.getRandomContents(limit);
    }

    // =================== 状态管理功能（3个方法）===================

    @Override
    public boolean updateContentStatus(Long contentId, String status) {
        log.info("更新内容状态: contentId={}, status={}", contentId, status);

        if (contentId == null || !StringUtils.hasText(status)) {
            throw new IllegalArgumentException("内容ID和状态不能为空");
        }

        try {
            int result = contentMapper.updateContentStatus(contentId, status);
            boolean success = result > 0;
            if (success) {
                log.info("内容状态更新成功: contentId={}", contentId);
            }
            return success;
        } catch (Exception e) {
            log.error("内容状态更新失败: contentId={}", contentId, e);
            return false;
        }
    }

    @Override
    public boolean updateReviewStatus(Long contentId, String reviewStatus) {
        log.info("更新审核状态: contentId={}, reviewStatus={}", contentId, reviewStatus);

        if (contentId == null || !StringUtils.hasText(reviewStatus)) {
            throw new IllegalArgumentException("内容ID和审核状态不能为空");
        }

        try {
            int result = contentMapper.updateReviewStatus(contentId, reviewStatus);
            boolean success = result > 0;
            if (success) {
                log.info("审核状态更新成功: contentId={}", contentId);
            }
            return success;
        } catch (Exception e) {
            log.error("审核状态更新失败: contentId={}", contentId, e);
            return false;
        }
    }

    @Override
    public boolean batchUpdateStatus(List<Long> ids, String status) {
        log.info("批量更新内容状态: ids.size={}, status={}",
                ids != null ? ids.size() : 0, status);

        if (ids == null || ids.isEmpty() || !StringUtils.hasText(status)) {
            throw new IllegalArgumentException("内容ID列表和状态不能为空");
        }

        try {
            int result = contentMapper.batchUpdateStatus(ids, status);
            boolean success = result > 0;
            if (success) {
                log.info("批量更新内容状态成功: 影响行数={}", result);
            }
            return success;
        } catch (Exception e) {
            log.error("批量更新内容状态失败", e);
            return false;
        }
    }

    // =================== 统计管理功能（2个方法）===================

    @Override
    public boolean updateContentStats(Long contentId, Long viewCount, Long likeCount,
                                      Long commentCount, Long favoriteCount) {
        log.info("更新内容统计: contentId={}, viewCount={}, likeCount={}, commentCount={}, favoriteCount={}",
                contentId, viewCount, likeCount, commentCount, favoriteCount);

        if (contentId == null) {
            throw new IllegalArgumentException("内容ID不能为空");
        }

        try {
            int result = contentMapper.updateContentStats(contentId, viewCount, likeCount,
                    commentCount, favoriteCount);
            boolean success = result > 0;
            if (success) {
                log.info("内容统计更新成功: contentId={}", contentId);
            }
            return success;
        } catch (Exception e) {
            log.error("内容统计更新失败: contentId={}", contentId, e);
            return false;
        }
    }

    @Override
    public Long increaseViewCount(Long contentId, Integer increment) {
        log.debug("增加浏览量: contentId={}, increment={}", contentId, increment);

        if (contentId == null || increment == null || increment <= 0) {
            throw new IllegalArgumentException("内容ID和增量必须有效");
        }

        Content content = contentMapper.selectById(contentId);
        if (content == null) {
            throw new IllegalArgumentException("内容不存在");
        }

        Long newViewCount = content.getViewCount() + increment;
        updateContentStats(contentId, newViewCount, content.getLikeCount(),
                content.getCommentCount(), content.getFavoriteCount());

        return newViewCount;
    }

    // =================== Controller专用方法 ===================

    @Override
    public Result<PageResponse<ContentResponse>> listContentsForController(
            String contentType, String status, Long authorId, String authorIds, Long categoryId, String keyword, Boolean isPublished,
            String orderBy, String orderDirection, Integer currentPage, Integer pageSize, Long userId) {
        try {
            log.info("Controller层 - 内容列表查询: type={}, status={}, authorId={}, page={}/{}", 
                    contentType, status, authorId, currentPage, pageSize);

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
            LambdaQueryWrapper<Content> queryWrapper = new LambdaQueryWrapper<>();

            // 动态查询条件
            if (StringUtils.hasText(contentType)) {
                queryWrapper.eq(Content::getContentType, contentType);
            }
            if (StringUtils.hasText(status)) {
                queryWrapper.eq(Content::getStatus, status);
            }
            // 处理作者ID查询：优先使用多个作者ID，如果没有则使用单个作者ID
            if (authorIds != null && !authorIds.trim().isEmpty()) {
                // 解析多个作者ID
                String[] authorIdArray = authorIds.split(",");
                List<Long> authorIdList = new ArrayList<>();
                for (String id : authorIdArray) {
                    try {
                        authorIdList.add(Long.parseLong(id.trim()));
                    } catch (NumberFormatException e) {
                        log.warn("无效的作者ID: {}", id);
                    }
                }
                if (!authorIdList.isEmpty()) {
                    queryWrapper.in(Content::getAuthorId, authorIdList);
                }
            } else if (authorId != null) {
                queryWrapper.eq(Content::getAuthorId, authorId);
            }
            if (categoryId != null) {
                queryWrapper.eq(Content::getCategoryId, categoryId);
            }
            if (StringUtils.hasText(keyword)) {
                queryWrapper.and(wrapper -> wrapper
                        .like(Content::getTitle, keyword)
                        .or()
                        .like(Content::getDescription, keyword)
                        .or()
                        .like(Content::getContentData, keyword));
            }
            if (isPublished != null) {
                if (isPublished) {
                    queryWrapper.eq(Content::getStatus, "PUBLISHED");
                } else {
                    queryWrapper.ne(Content::getStatus, "PUBLISHED");
                }
            }

            // 排序
            if ("viewCount".equals(orderBy)) {
                if ("ASC".equals(orderDirection)) {
                    queryWrapper.orderByAsc(Content::getViewCount);
                } else {
                    queryWrapper.orderByDesc(Content::getViewCount);
                }
            } else if ("likeCount".equals(orderBy)) {
                if ("ASC".equals(orderDirection)) {
                    queryWrapper.orderByAsc(Content::getLikeCount);
                } else {
                    queryWrapper.orderByDesc(Content::getLikeCount);
                }
            } else if ("updateTime".equals(orderBy)) {
                if ("ASC".equals(orderDirection)) {
                    queryWrapper.orderByAsc(Content::getUpdateTime);
                } else {
                    queryWrapper.orderByDesc(Content::getUpdateTime);
                }
            } else {
                // 默认按创建时间排序
                if ("ASC".equals(orderDirection)) {
                    queryWrapper.orderByAsc(Content::getCreateTime);
                } else {
                    queryWrapper.orderByDesc(Content::getCreateTime);
                }
            }

            // 使用MyBatis-Plus分页
            Page<Content> page = new Page<>(currentPage, pageSize);
            
            // 如果是LONGVIDEO查询，添加特殊调试
            if ("LONGVIDEO".equals(contentType) && authorId != null) {
                log.info("LONGVIDEO查询调试 - 开始执行查询: contentType={}, authorId={}", contentType, authorId);
                
                // 先查询该作者的所有内容类型，用于调试
                LambdaQueryWrapper<Content> debugWrapper = new LambdaQueryWrapper<>();
                debugWrapper.eq(Content::getAuthorId, authorId)
                           .select(Content::getId, Content::getTitle, Content::getContentType, Content::getStatus);
                List<Content> debugResults = contentMapper.selectList(debugWrapper);
                
                log.info("作者{}的所有内容数据: {}", authorId, 
                        debugResults.stream().map(c -> 
                            String.format("ID:%d,Title:%s,Type:%s,Status:%s", 
                                        c.getId(), c.getTitle(), c.getContentType(), c.getStatus()))
                                .collect(Collectors.toList()));
                
                // 统计各contentType的数量
                Map<String, Long> typeCount = debugResults.stream()
                    .collect(Collectors.groupingBy(Content::getContentType, Collectors.counting()));
                log.info("作者{}的内容类型统计: {}", authorId, typeCount);
            }
            
            IPage<Content> result = contentMapper.selectPage(page, queryWrapper);
            
            // 查询结果调试
            log.info("内容查询结果: 总数={}, 当前页数据={}", result.getTotal(), result.getRecords().size());
            if ("LONGVIDEO".equals(contentType) && result.getTotal() == 0) {
                log.warn("LONGVIDEO查询无结果 - 可能原因: 1.contentType值不匹配 2.status过滤 3.数据不存在");
            }

            // 转换为Response对象
            List<Content> contents = result.getRecords();

            // 提前收集ID列表，便于批量查询互动状态
            List<Long> contentIds = contents.stream().map(Content::getId).collect(Collectors.toList());
            List<Long> authorIdsInPage = contents.stream().map(Content::getAuthorId).collect(Collectors.toList());

            // 当前用户ID（从参数获取，如果没有则设为null表示未登录状态）
            Long currentUserId = userId; // 使用传入的userId参数

            // 批量互动状态（若有对应批量接口可替换调用）
            java.util.Map<Long, Boolean> likedMap = java.util.Collections.emptyMap();
            java.util.Map<Long, Boolean> favoritedMap = java.util.Collections.emptyMap();
            java.util.Map<Long, Boolean> followedMap = java.util.Collections.emptyMap();

            // 只有在有用户ID的情况下才查询互动状态
            if (currentUserId != null) {
                try {
                    // 批量点赞状态
                    likedMap = likeService.batchCheckLikeStatus(currentUserId, "CONTENT", contentIds);
                    log.debug("批量点赞状态查询成功: userId={}, size={}", currentUserId, likedMap.size());
                } catch (Exception e) {
                    log.warn("批量点赞状态查询失败: userId={}, error={}", currentUserId, e.getMessage(), e);
                    likedMap = java.util.Collections.emptyMap();
                }
                try {
                    // 批量收藏状态
                    favoritedMap = favoriteService.batchCheckFavoriteStatus(currentUserId, "CONTENT", contentIds);
                    log.debug("批量收藏状态查询成功: userId={}, size={}", currentUserId, favoritedMap.size());
                } catch (Exception e) {
                    log.warn("批量收藏状态查询失败: userId={}, error={}", currentUserId, e.getMessage(), e);
                    favoritedMap = java.util.Collections.emptyMap();
                }
                try {
                    // 批量关注状态（作者维度）
                    followedMap = followService.batchCheckFollowStatus(currentUserId, authorIdsInPage);
                    log.debug("批量关注状态查询成功: userId={}, size={}", currentUserId, followedMap.size());
                } catch (Exception e) {
                    log.warn("批量关注状态查询失败: userId={}, error={}", currentUserId, e.getMessage(), e);
                    followedMap = java.util.Collections.emptyMap();
                }
            }

            // 供lambda安全引用的最终变量
            final java.util.Map<Long, Boolean> likedMapFinal = (likedMap != null) ? likedMap : java.util.Collections.emptyMap();
            final java.util.Map<Long, Boolean> favoritedMapFinal = (favoritedMap != null) ? favoritedMap : java.util.Collections.emptyMap();
            final java.util.Map<Long, Boolean> followedMapFinal = (followedMap != null) ? followedMap : java.util.Collections.emptyMap();

            List<ContentResponse> responses = contents.stream()
                    .map(c -> {
                        ContentResponse r = convertToResponse(c);
                        // 作者昵称/头像冗余字段回填
                        r.setAuthorNickname(c.getAuthorNickname());
                        r.setAuthorAvatar(c.getAuthorAvatar());
                        // 互动状态填充
                        r.setIsLiked(likedMapFinal.getOrDefault(c.getId(), false));
                        r.setIsFavorited(favoritedMapFinal.getOrDefault(c.getId(), false));
                        r.setIsFollowed(followedMapFinal.getOrDefault(c.getAuthorId(), false));
                        return r;
                    })
                    .collect(Collectors.toList());

            // 构建分页响应
            PageResponse<ContentResponse> pageResponse = new PageResponse<>();
            pageResponse.setRecords(responses);
            pageResponse.setTotal(result.getTotal());
            pageResponse.setCurrentPage(currentPage);
            pageResponse.setPageSize(pageSize);
            pageResponse.setTotalPages((int) Math.ceil((double) result.getTotal() / pageSize));

            log.info("内容列表查询成功: 总数={}, 当前页={}, 页面大小={}", result.getTotal(), currentPage, pageSize);
            return Result.success(pageResponse);

        } catch (Exception e) {
            log.error("Controller层 - 内容列表查询失败", e);
            return Result.error("内容列表查询失败: " + e.getMessage());
        }
    }

    /**
     * 将Content实体转换为ContentResponse
     */
    private ContentResponse convertToResponse(Content content) {
        if (content == null) {
            return null;
        }

        ContentResponse response = new ContentResponse();
        response.setId(content.getId());
        response.setTitle(content.getTitle());
        response.setDescription(content.getDescription());
        response.setContentData(content.getContentData());
        response.setContentType(content.getContentType());
        response.setAuthorId(content.getAuthorId());
        response.setCategoryId(content.getCategoryId());
        response.setStatus(content.getStatus());
        response.setReviewStatus(content.getReviewStatus());
        response.setViewCount(content.getViewCount());
        response.setLikeCount(content.getLikeCount());
        response.setCommentCount(content.getCommentCount());
        response.setFavoriteCount(content.getFavoriteCount());
        response.setCreateTime(content.getCreateTime());
        response.setUpdateTime(content.getUpdateTime());

        return response;
    }

    @Override
    public Result<PageResponse<ContentResponse>> getFollowingContentsForController(
            Long userId, String contentType, String orderBy, String orderDirection, Integer currentPage, Integer pageSize) {
        try {
            log.info("Controller层 - 获取关注内容: userId={}, contentType={}, page={}/{}", 
                    userId, contentType, currentPage, pageSize);

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

            // 获取用户关注的作者ID列表
            List<Long> followingAuthorIds = followService.getFollowingAuthorIds(userId);
            if (followingAuthorIds == null || followingAuthorIds.isEmpty()) {
                // 如果没有关注任何作者，返回空结果
                PageResponse<ContentResponse> emptyResponse = new PageResponse<>();
                emptyResponse.setRecords(new ArrayList<>());
                emptyResponse.setTotal(0L);
                emptyResponse.setCurrentPage(currentPage);
                emptyResponse.setPageSize(pageSize);
                emptyResponse.setTotalPages(0);
                return Result.success(emptyResponse);
            }

            // 构建查询条件
            LambdaQueryWrapper<Content> queryWrapper = new LambdaQueryWrapper<>();

            // 只查询关注作者的内容
            queryWrapper.in(Content::getAuthorId, followingAuthorIds);

            // 只查询已发布的内容
            queryWrapper.eq(Content::getStatus, "PUBLISHED");

            // 内容类型筛选
            if (StringUtils.hasText(contentType)) {
                queryWrapper.eq(Content::getContentType, contentType);
            }

            // 排序
            if ("viewCount".equals(orderBy)) {
                if ("ASC".equals(orderDirection)) {
                    queryWrapper.orderByAsc(Content::getViewCount);
                } else {
                    queryWrapper.orderByDesc(Content::getViewCount);
                }
            } else if ("likeCount".equals(orderBy)) {
                if ("ASC".equals(orderDirection)) {
                    queryWrapper.orderByAsc(Content::getLikeCount);
                } else {
                    queryWrapper.orderByDesc(Content::getLikeCount);
                }
            } else if ("updateTime".equals(orderBy)) {
                if ("ASC".equals(orderDirection)) {
                    queryWrapper.orderByAsc(Content::getUpdateTime);
                } else {
                    queryWrapper.orderByDesc(Content::getUpdateTime);
                }
            } else {
                // 默认按创建时间排序
                if ("ASC".equals(orderDirection)) {
                    queryWrapper.orderByAsc(Content::getCreateTime);
                } else {
                    queryWrapper.orderByDesc(Content::getCreateTime);
                }
            }

            // 使用MyBatis-Plus分页
            Page<Content> page = new Page<>(currentPage, pageSize);
            IPage<Content> result = contentMapper.selectPage(page, queryWrapper);

            // 转换为Response对象
            List<ContentResponse> responses = result.getRecords().stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());

            // 构建分页响应
            PageResponse<ContentResponse> pageResponse = new PageResponse<>();
            pageResponse.setRecords(responses);
            pageResponse.setTotal(result.getTotal());
            pageResponse.setCurrentPage(currentPage);
            pageResponse.setPageSize(pageSize);
            pageResponse.setTotalPages((int) Math.ceil((double) result.getTotal() / pageSize));

            log.info("关注内容查询成功: 总数={}, 当前页={}, 页面大小={}", result.getTotal(), currentPage, pageSize);
            return Result.success(pageResponse);

        } catch (Exception e) {
            log.error("Controller层 - 获取关注内容失败", e);
            return Result.error("获取关注内容失败: " + e.getMessage());
        }
    }
}
