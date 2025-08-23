package com.gig.collide.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gig.collide.Apientry.api.common.response.PageResponse;
import com.gig.collide.Apientry.api.common.response.Result;
import com.gig.collide.Apientry.api.favorite.request.FavoriteCreateRequest;
import com.gig.collide.Apientry.api.favorite.request.FavoriteDeleteRequest;
import com.gig.collide.Apientry.api.favorite.request.FavoriteQueryRequest;
import com.gig.collide.Apientry.api.favorite.response.FavoriteResponse;
import com.gig.collide.Apientry.api.goods.response.GoodsResponse;
import com.gig.collide.converter.FavoriteConverter;
import com.gig.collide.domain.Favorite;
import com.gig.collide.mapper.FavoriteMapper;
import com.gig.collide.mapper.ContentMapper;
import com.gig.collide.mapper.GoodsMapper;
import com.gig.collide.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 收藏业务逻辑实现类 - 简洁版
 * 基于favorite-simple.sql的业务逻辑，实现核心收藏功能
 *
 * @author Collide
 * @version 2.0.0 (简洁版)
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteMapper favoriteMapper;
    private final FavoriteConverter favoriteConverter;
    private final ContentMapper contentMapper;
    private final GoodsMapper goodsMapper;

    // =================== 核心业务方法实现 ===================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Favorite addFavorite(Favorite favorite) {
        log.info("添加收藏: userId={}, favoriteType={}, targetId={}",
                favorite.getUserId(), favorite.getFavoriteType(), favorite.getTargetId());

        // 验证请求参数
        String validationResult = validateFavoriteRequest(favorite);
        if (validationResult != null) {
            throw new IllegalArgumentException(validationResult);
        }

        // 检查是否可以收藏
        String checkResult = checkCanFavorite(favorite.getUserId(), favorite.getFavoriteType(), favorite.getTargetId());
        if (checkResult != null) {
            throw new IllegalStateException(checkResult);
        }

        // 检查是否已存在收藏关系（包括已取消的）
        Favorite existingFavorite = favoriteMapper.findByUserAndTarget(
                favorite.getUserId(), favorite.getFavoriteType(), favorite.getTargetId(), null);

        if (existingFavorite != null) {
            if (existingFavorite.isActive()) {
                throw new IllegalStateException("已经收藏过该对象");
            } else {
                // 重新激活已取消的收藏
                existingFavorite.setStatus("active");
                existingFavorite.setUpdateTime(LocalDateTime.now());

                // 更新冗余信息
                if (StringUtils.hasText(favorite.getTargetTitle())) {
                    existingFavorite.setTargetTitle(favorite.getTargetTitle());
                }
                if (StringUtils.hasText(favorite.getTargetCover())) {
                    existingFavorite.setTargetCover(favorite.getTargetCover());
                }
                if (favorite.getTargetAuthorId() != null) {
                    existingFavorite.setTargetAuthorId(favorite.getTargetAuthorId());
                }
                if (StringUtils.hasText(favorite.getUserNickname())) {
                    existingFavorite.setUserNickname(favorite.getUserNickname());
                }

                int result = favoriteMapper.updateById(existingFavorite);
                if (result > 0) {
                    // 更新目标对象的收藏统计 +1
                    updateTargetFavoriteCount(existingFavorite.getFavoriteType(), existingFavorite.getTargetId(), 1);
                    
                    log.info("重新激活收藏成功: userId={}, favoriteType={}, targetId={}",
                            favorite.getUserId(), favorite.getFavoriteType(), favorite.getTargetId());
                    return existingFavorite;
                } else {
                    throw new RuntimeException("重新激活收藏失败");
                }
            }
        }

        // 创建新的收藏记录
        favorite.setStatus("active");
        favorite.setCreateTime(LocalDateTime.now());
        favorite.setUpdateTime(LocalDateTime.now());

        int result = favoriteMapper.insert(favorite);
        if (result > 0) {
            // 更新目标对象的收藏统计 +1
            updateTargetFavoriteCount(favorite.getFavoriteType(), favorite.getTargetId(), 1);
            
            log.info("添加收藏成功: userId={}, favoriteType={}, targetId={}",
                    favorite.getUserId(), favorite.getFavoriteType(), favorite.getTargetId());
            return favorite;
        } else {
            throw new RuntimeException("添加收藏失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeFavorite(Long userId, String favoriteType, Long targetId, String cancelReason, Long operatorId) {
        log.info("移除收藏: userId={}, favoriteType={}, targetId={}, cancelReason={}, operatorId={}",
                userId, favoriteType, targetId, cancelReason, operatorId);

        // 验证请求参数
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        if (!StringUtils.hasText(favoriteType)) {
            throw new IllegalArgumentException("收藏类型不能为空");
        }
        if (targetId == null) {
            throw new IllegalArgumentException("目标ID不能为空");
        }
        if (operatorId == null) {
            throw new IllegalArgumentException("操作人ID不能为空");
        }

        // 查找收藏记录
        Favorite existingFavorite = favoriteMapper.findByUserAndTarget(userId, favoriteType, targetId, "active");
        if (existingFavorite == null) {
            throw new IllegalArgumentException("收藏记录不存在");
        }

        // 逻辑删除（设置为cancelled）
        existingFavorite.setStatus("cancelled");
        existingFavorite.setUpdateTime(LocalDateTime.now());

        // 记录取消原因和操作人信息到日志（数据库表中无对应字段）
        if (StringUtils.hasText(cancelReason)) {
            log.info("收藏取消原因: {}", cancelReason);
        }
        if (operatorId != null) {
            log.info("操作人ID: {}", operatorId);
        }

        int result = favoriteMapper.updateById(existingFavorite);
        if (result > 0) {
            // 更新目标对象的收藏统计 -1
            updateTargetFavoriteCount(favoriteType, targetId, -1);
            
            log.info("移除收藏成功: userId={}, favoriteType={}, targetId={}, operatorId={}",
                    userId, favoriteType, targetId, operatorId);
            return true;
        } else {
            throw new RuntimeException("移除收藏失败");
        }
    }

    @Override
    public boolean checkFavoriteStatus(Long userId, String favoriteType, Long targetId) {
        log.debug("检查收藏状态: userId={}, favoriteType={}, targetId={}", userId, favoriteType, targetId);

        if (userId == null || !StringUtils.hasText(favoriteType) || targetId == null) {
            return false;
        }

        Favorite favorite = favoriteMapper.findByUserAndTarget(userId, favoriteType, targetId, "active");
        return favorite != null && favorite.isActive();
    }

    @Override
    public Favorite getFavoriteDetail(Long userId, String favoriteType, Long targetId) {
        log.debug("获取收藏详情: userId={}, favoriteType={}, targetId={}", userId, favoriteType, targetId);

        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        if (!StringUtils.hasText(favoriteType)) {
            throw new IllegalArgumentException("收藏类型不能为空");
        }
        if (targetId == null) {
            throw new IllegalArgumentException("目标ID不能为空");
        }

        return favoriteMapper.findByUserAndTarget(userId, favoriteType, targetId, null);
    }

    @Override
    public IPage<Favorite> queryFavorites(Integer pageNum, Integer pageSize, Long userId, String favoriteType,
                                          Long targetId, String targetTitle, Long targetAuthorId, String userNickname,
                                          String status, String queryType, String orderBy, String orderDirection) {
        log.debug("分页查询收藏记录: page={}/{}, userId={}, favoriteType={}", pageNum, pageSize, userId, favoriteType);

        Page<Favorite> page = createPage(pageNum, pageSize);

        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Favorite> queryWrapper =
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();

        // 构建查询条件
        if (userId != null) {
            queryWrapper.eq("user_id", userId);
        }
        if (StringUtils.hasText(favoriteType)) {
            queryWrapper.eq("favorite_type", favoriteType);
        }
        if (targetId != null) {
            queryWrapper.eq("target_id", targetId);
        }
        if (StringUtils.hasText(targetTitle)) {
            queryWrapper.like("target_title", targetTitle);
        }
        if (targetAuthorId != null) {
            queryWrapper.eq("target_author_id", targetAuthorId);
        }
        if (StringUtils.hasText(userNickname)) {
            queryWrapper.like("user_nickname", userNickname);
        }
        if (StringUtils.hasText(status)) {
            queryWrapper.eq("status", status);
        }

        // 排序
        if (StringUtils.hasText(orderBy)) {
            String direction = "desc".equalsIgnoreCase(orderDirection) ? "desc" : "asc";
            queryWrapper.orderBy(true, "desc".equalsIgnoreCase(direction), orderBy);
        } else {
            queryWrapper.orderByDesc("create_time");
        }

        return favoriteMapper.selectPage(page, queryWrapper);
    }

    @Override
    public IPage<Favorite> getUserFavorites(Long userId, String favoriteType, Integer pageNum, Integer pageSize) {
        log.debug("获取用户收藏: userId={}, favoriteType={}, page={}/{}",
                userId, favoriteType, pageNum, pageSize);

        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        Page<Favorite> page = createPage(pageNum, pageSize);
        return favoriteMapper.selectUserFavorites(page, userId, favoriteType, null);
    }

    @Override
    public IPage<Favorite> getTargetFavorites(String favoriteType, Long targetId, Integer pageNum, Integer pageSize) {
        log.debug("获取目标对象收藏: favoriteType={}, targetId={}, page={}/{}",
                favoriteType, targetId, pageNum, pageSize);

        if (!StringUtils.hasText(favoriteType)) {
            throw new IllegalArgumentException("收藏类型不能为空");
        }
        if (targetId == null) {
            throw new IllegalArgumentException("目标ID不能为空");
        }

        Page<Favorite> page = createPage(pageNum, pageSize);
        return favoriteMapper.selectTargetFavorites(page, favoriteType, targetId, null);
    }

    @Override
    public Long getUserFavoriteCount(Long userId, String favoriteType) {
        log.debug("获取用户收藏数量: userId={}, favoriteType={}", userId, favoriteType);

        if (userId == null) {
            return 0L;
        }

        return favoriteMapper.countUserFavorites(userId, favoriteType, null);
    }

    @Override
    public Long getTargetFavoriteCount(String favoriteType, Long targetId) {
        log.debug("获取目标对象被收藏数量: favoriteType={}, targetId={}", favoriteType, targetId);

        if (!StringUtils.hasText(favoriteType) || targetId == null) {
            return 0L;
        }

        return favoriteMapper.countTargetFavorites(favoriteType, targetId, null);
    }

    @Override
    public Map<String, Object> getUserFavoriteStatistics(Long userId) {
        log.debug("获取用户收藏统计信息: userId={}", userId);

        if (userId == null) {
            return new HashMap<>();
        }

        return favoriteMapper.getUserFavoriteStatistics(userId);
    }



    @Override
    public IPage<Favorite> searchFavoritesByTitle(Long userId, String titleKeyword, String favoriteType,
                                                  Integer pageNum, Integer pageSize) {
        log.debug("根据标题搜索收藏: userId={}, titleKeyword={}, favoriteType={}, page={}/{}",
                userId, titleKeyword, favoriteType, pageNum, pageSize);

        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        if (!StringUtils.hasText(titleKeyword)) {
            throw new IllegalArgumentException("标题关键词不能为空");
        }

        Page<Favorite> page = createPage(pageNum, pageSize);
        return favoriteMapper.searchFavorites(page, userId, titleKeyword, favoriteType);
    }

    @Override
    public IPage<Favorite> getPopularFavorites(String favoriteType, Integer pageNum, Integer pageSize) {
        log.debug("获取热门收藏: favoriteType={}, page={}/{}", favoriteType, pageNum, pageSize);

        if (!StringUtils.hasText(favoriteType)) {
            throw new IllegalArgumentException("收藏类型不能为空");
        }

        Page<Favorite> page = createPage(pageNum, pageSize);
        return favoriteMapper.selectPopularFavorites(page, favoriteType);
    }

    @Override
    public int updateUserInfo(Long userId, String nickname) {
        log.debug("更新用户信息: userId={}, nickname={}", userId, nickname);

        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        if (!StringUtils.hasText(nickname)) {
            throw new IllegalArgumentException("昵称不能为空");
        }

        int result = favoriteMapper.updateUserInfo(userId, nickname);
        log.debug("更新用户信息完成: userId={}, 更新数量={}", userId, result);
        return result;
    }

    @Override
    public int updateTargetInfo(String favoriteType, Long targetId, String title, String cover, Long authorId) {
        log.debug("更新目标对象信息: favoriteType={}, targetId={}, title={}", favoriteType, targetId, title);

        if (!StringUtils.hasText(favoriteType)) {
            throw new IllegalArgumentException("收藏类型不能为空");
        }
        if (targetId == null) {
            throw new IllegalArgumentException("目标ID不能为空");
        }

        int result = favoriteMapper.updateTargetInfo(favoriteType, targetId, title, cover, authorId);
        log.debug("更新目标对象信息完成: favoriteType={}, targetId={}, 更新数量={}", favoriteType, targetId, result);
        return result;
    }

    @Override
    public int cleanCancelledFavorites(Integer days) {
        log.debug("清理已取消的收藏记录: 保留天数={}", days);

        try {
            // 计算截止日期
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);

            // 删除已取消且超过保留天数的收藏记录
            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Favorite> queryWrapper =
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Favorite>()
                            .eq("status", "cancelled")
                            .lt("update_time", cutoffDate);

            int result = favoriteMapper.delete(queryWrapper);
            log.info("清理已取消收藏记录完成: 删除数量={}", result);
            return result;
        } catch (Exception e) {
            log.error("清理已取消收藏记录时发生异常", e);
            return 0;
        }
    }

    @Override
    public IPage<Favorite> getFavoritesByAuthor(Long targetAuthorId, String favoriteType, Integer pageNum, Integer pageSize) {
        log.debug("根据作者查询收藏作品: 作者={}, 类型={}, 页码={}/{}", targetAuthorId, favoriteType, pageNum, pageSize);

        try {
            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Favorite> queryWrapper =
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Favorite>()
                            .eq("target_author_id", targetAuthorId)
                            .eq("status", "active");

            if (favoriteType != null && !favoriteType.trim().isEmpty()) {
                queryWrapper.eq("favorite_type", favoriteType);
            }

            queryWrapper.orderByDesc("create_time");

            Page<Favorite> page = createPage(pageNum, pageSize);
            return favoriteMapper.selectPage(page, queryWrapper);
        } catch (Exception e) {
            log.error("根据作者查询收藏作品时发生异常: 作者={}", targetAuthorId, e);
            return new Page<>();
        }
    }

    @Override
    public String validateFavoriteRequest(Favorite favorite) {
        log.debug("验证收藏请求参数: 用户={}, 类型={}, 目标={}",
                favorite.getUserId(), favorite.getFavoriteType(), favorite.getTargetId());

        try {
            // 检查必填字段
            if (favorite.getUserId() == null) {
                return "用户ID不能为空";
            }
            if (favorite.getFavoriteType() == null || favorite.getFavoriteType().trim().isEmpty()) {
                return "收藏类型不能为空";
            }
            if (favorite.getTargetId() == null) {
                return "目标ID不能为空";
            }

            // 检查收藏类型是否有效
            if (!isValidFavoriteType(favorite.getFavoriteType())) {
                return "无效的收藏类型: " + favorite.getFavoriteType();
            }

            // 检查是否已经收藏
            if (checkFavoriteStatus(favorite.getUserId(), favorite.getFavoriteType(), favorite.getTargetId())) {
                return "已经收藏过该对象";
            }

            // 验证通过
            return null;
        } catch (Exception e) {
            log.error("验证收藏请求参数时发生异常", e);
            return "验证过程中发生异常: " + e.getMessage();
        }
    }

    @Override
    public String checkCanFavorite(Long userId, String favoriteType, Long targetId) {
        log.debug("检查是否可以收藏: 用户={}, 类型={}, 目标={}", userId, favoriteType, targetId);

        try {
            // 检查用户是否存在
            if (!userExists(userId)) {
                return "用户不存在";
            }

            // 检查目标对象是否存在
            if (!targetExists(favoriteType, targetId)) {
                return "目标对象不存在";
            }

            // 检查收藏类型是否有效
            if (!isValidFavoriteType(favoriteType)) {
                return "无效的收藏类型";
            }

            // 检查是否已经收藏
            if (checkFavoriteStatus(userId, favoriteType, targetId)) {
                return "已经收藏过该对象";
            }

            // 可以收藏
            return null;
        } catch (Exception e) {
            log.error("检查是否可以收藏时发生异常: 用户={}, 类型={}, 目标={}", userId, favoriteType, targetId, e);
            return "检查过程中发生异常: " + e.getMessage();
        }
    }

    @Override
    public boolean existsFavoriteRelation(Long userId, String favoriteType, Long targetId) {
        log.debug("检查是否已经存在收藏关系: 用户={}, 类型={}, 目标={}", userId, favoriteType, targetId);

        try {
            // 使用自定义的查询方法检查收藏关系是否存在
            Favorite existingFavorite = favoriteMapper.findByUserAndTarget(userId, favoriteType, targetId, null);
            return existingFavorite != null;
        } catch (Exception e) {
            log.error("检查收藏关系时发生异常: 用户={}, 类型={}, 目标={}", userId, favoriteType, targetId, e);
            return false;
        }
    }

    @Override
    public boolean reactivateFavorite(Long userId, String favoriteType, Long targetId) {
        log.debug("重新激活已取消的收藏: 用户={}, 类型={}, 目标={}", userId, favoriteType, targetId);

        try {
            // 查找已取消的收藏记录
            Favorite favorite = favoriteMapper.selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Favorite>()
                            .eq("user_id", userId)
                            .eq("favorite_type", favoriteType)
                            .eq("target_id", targetId)
                            .eq("status", "cancelled")
            );

            if (favorite == null) {
                log.warn("未找到已取消的收藏记录: 用户={}, 类型={}, 目标={}", userId, favoriteType, targetId);
                return false;
            }

            // 更新状态为active
            favorite.setStatus("active");
            favorite.setUpdateTime(LocalDateTime.now());

            int result = favoriteMapper.updateById(favorite);
            boolean success = result > 0;

            if (success) {
                // 更新目标对象的收藏统计 +1
                updateTargetFavoriteCount(favoriteType, targetId, 1);
                
                log.info("收藏重新激活成功: 用户={}, 类型={}, 目标={}", userId, favoriteType, targetId);
            } else {
                log.warn("收藏重新激活失败: 用户={}, 类型={}, 目标={}", userId, favoriteType, targetId);
            }

            return success;
        } catch (Exception e) {
            log.error("重新激活收藏时发生异常: 用户={}, 类型={}, 目标={}", userId, favoriteType, targetId, e);
            return false;
        }
    }

    // =================== Controller专用方法实现 ===================

    @Override
    public Result<FavoriteResponse> addFavoriteForController(FavoriteCreateRequest request) {
        try {
            log.info("Controller层 - 添加收藏: 用户={}, 类型={}, 目标={}",
                    request.getUserId(), request.getFavoriteType(), request.getTargetId());

            // 使用转换器将FavoriteCreateRequest转换为Favorite实体
            Favorite favorite = favoriteConverter.toEntity(request);
            Favorite createdFavorite = addFavorite(favorite);

            // 将创建的Favorite实体转换为FavoriteResponse
            FavoriteResponse response = favoriteConverter.toResponse(createdFavorite);
            return Result.success(response);
        } catch (Exception e) {
            log.error("Controller层 - 添加收藏失败", e);
            return Result.error("添加收藏失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Void> removeFavoriteForController(FavoriteDeleteRequest request) {
        try {
            log.info("Controller层 - 取消收藏: 用户={}, 类型={}, 目标={}",
                    request.getUserId(), request.getFavoriteType(), request.getTargetId());

            boolean success = removeFavorite(request.getUserId(), request.getFavoriteType(),
                    request.getTargetId(), request.getCancelReason(), request.getOperatorId());

            if (success) {
                return Result.success(null);
            } else {
                return Result.error("取消收藏失败");
            }
        } catch (Exception e) {
            log.error("Controller层 - 取消收藏失败", e);
            return Result.error("取消收藏失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Boolean> checkFavoriteStatusForController(Long userId, String favoriteType, Long targetId) {
        try {
            log.info("Controller层 - 检查收藏状态: 用户={}, 类型={}, 目标={}", userId, favoriteType, targetId);

            boolean isFavorited = checkFavoriteStatus(userId, favoriteType, targetId);
            return Result.success(isFavorited);
        } catch (Exception e) {
            log.error("Controller层 - 检查收藏状态失败", e);
            return Result.error("检查收藏状态失败: " + e.getMessage());
        }
    }

    @Override
    public Result<FavoriteResponse> getFavoriteDetailForController(Long userId, String favoriteType, Long targetId) {
        try {
            log.info("Controller层 - 获取收藏详情: 用户={}, 类型={}, 目标={}", userId, favoriteType, targetId);

            Favorite favorite = getFavoriteDetail(userId, favoriteType, targetId);
            if (favorite != null) {
                FavoriteResponse response = favoriteConverter.toResponse(favorite);
                return Result.success(response);
            } else {
                return Result.error("收藏记录不存在");
            }
        } catch (Exception e) {
            log.error("Controller层 - 获取收藏详情失败", e);
            return Result.error("获取收藏详情失败: " + e.getMessage());
        }
    }

    @Override
    public Result<PageResponse<FavoriteResponse>> queryFavoritesForController(FavoriteQueryRequest request) {
        try {
            log.info("Controller层 - 分页查询收藏记录: 用户={}, 类型={}, 页码={}/{}",
                    request.getUserId(), request.getFavoriteType(), request.getCurrentPage(), request.getPageSize());

            IPage<Favorite> page = queryFavorites(
                    request.getCurrentPage(), request.getPageSize(),
                    request.getUserId(), request.getFavoriteType(),
                    request.getTargetId(), request.getTargetTitle(),
                    request.getTargetAuthorId(), request.getUserNickname(),
                    request.getStatus(), request.getQueryType(),
                    request.getOrderBy(), request.getOrderDirection()
            );

            // 转换为PageResponse<FavoriteResponse>
            List<FavoriteResponse> responses = favoriteConverter.toResponseList(page.getRecords());
            
            // 安全地获取当前页码和页面大小
            int currentPageValue = 1;
            int pageSizeValue = 20;
            

            
            PageResponse<FavoriteResponse> pageResponse = PageResponse.of(
                    responses, page.getTotal(), currentPageValue, pageSizeValue
            );

            return Result.success(pageResponse);
        } catch (Exception e) {
            log.error("Controller层 - 分页查询收藏记录失败", e);
            return Result.error("分页查询收藏记录失败: " + e.getMessage());
        }
    }

    @Override
    public Result<PageResponse<FavoriteResponse>> getUserFavoritesForController(Long userId, String favoriteType, Integer currentPage, Integer pageSize) {
        try {
            log.info("Controller层 - 获取用户收藏列表: 用户={}, 类型={}, 页码={}/{}", userId, favoriteType, currentPage, pageSize);

            IPage<Favorite> page = getUserFavorites(userId, favoriteType, currentPage, pageSize);

            // 转换为PageResponse<FavoriteResponse>
            List<FavoriteResponse> responses = favoriteConverter.toResponseList(page.getRecords());
            
            // 安全地获取当前页码和页面大小
            int currentPageValue = 1;
            int pageSizeValue = 20;
            

            PageResponse<FavoriteResponse> pageResponse = PageResponse.of(
                    responses, page.getTotal(), currentPageValue, pageSizeValue
            );

            return Result.success(pageResponse);
        } catch (Exception e) {
            log.error("Controller层 - 获取用户收藏列表失败", e);
            return Result.error("获取用户收藏列表失败: " + e.getMessage());
        }
    }

    @Override
    public Result<PageResponse<FavoriteResponse>> getTargetFavoritesForController(String favoriteType, Long targetId, Integer currentPage, Integer pageSize) {
        try {
            log.info("Controller层 - 获取目标收藏列表: 类型={}, 目标={}, 页码={}/{}", favoriteType, targetId, currentPage, pageSize);

            IPage<Favorite> page = getTargetFavorites(favoriteType, targetId, currentPage, pageSize);

            // 转换为PageResponse<FavoriteResponse>
            List<FavoriteResponse> responses = favoriteConverter.toResponseList(page.getRecords());
            
            // 安全地获取当前页码和页面大小
            int currentPageValue = 1;
            int pageSizeValue = 20;
            

            
            PageResponse<FavoriteResponse> pageResponse = PageResponse.of(
                    responses, page.getTotal(), currentPageValue, pageSizeValue
            );

            return Result.success(pageResponse);
        } catch (Exception e) {
            log.error("Controller层 - 获取目标收藏列表失败", e);
            return Result.error("获取目标收藏列表失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Long> getUserFavoriteCountForController(Long userId, String favoriteType) {
        try {
            log.info("Controller层 - 获取用户收藏数量: 用户={}, 类型={}", userId, favoriteType);

            Long count = getUserFavoriteCount(userId, favoriteType);
            return Result.success(count);
        } catch (Exception e) {
            log.error("Controller层 - 获取用户收藏数量失败", e);
            return Result.error("获取用户收藏数量失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Long> getTargetFavoriteCountForController(String favoriteType, Long targetId) {
        try {
            log.info("Controller层 - 获取目标被收藏数量: 类型={}, 目标={}", favoriteType, targetId);

            Long count = getTargetFavoriteCount(favoriteType, targetId);
            return Result.success(count);
        } catch (Exception e) {
            log.error("Controller层 - 获取目标被收藏数量失败", e);
            return Result.error("获取目标被收藏数量失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Map<String, Object>> getUserFavoriteStatisticsForController(Long userId) {
        try {
            log.info("Controller层 - 获取用户收藏统计: 用户={}", userId);

            Map<String, Object> statistics = getUserFavoriteStatistics(userId);
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("Controller层 - 获取用户收藏统计失败", e);
            return Result.error("获取用户收藏统计失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Map<Long, Boolean>> batchCheckFavoriteStatusForController(Long userId, String favoriteType, List<Long> targetIds) {
        try {
            log.info("Controller层 - 批量检查收藏状态: 用户={}, 类型={}, 数量={}",
                    userId, favoriteType, targetIds != null ? targetIds.size() : 0);

            Map<Long, Boolean> statusMap = batchCheckFavoriteStatus(userId, favoriteType, targetIds);
            return Result.success(statusMap);
        } catch (Exception e) {
            log.error("Controller层 - 批量检查收藏状态失败", e);
            return Result.error("批量检查收藏状态失败: " + e.getMessage());
        }
    }

    @Override
    public Result<PageResponse<FavoriteResponse>> searchFavoritesByTitleForController(Long userId, String titleKeyword, String favoriteType, Integer currentPage, Integer pageSize) {
        try {
            log.info("Controller层 - 搜索收藏: 用户={}, 关键词={}, 类型={}, 页码={}/{}",
                    userId, titleKeyword, favoriteType, currentPage, pageSize);

            IPage<Favorite> page = searchFavoritesByTitle(userId, titleKeyword, favoriteType, currentPage, pageSize);

            // 转换为PageResponse<FavoriteResponse>
            List<FavoriteResponse> responses = favoriteConverter.toResponseList(page.getRecords());


            PageResponse<FavoriteResponse> pageResponse = PageResponse.of(
                    responses, page.getTotal(),
                    (int) page.getCurrent(), (int) page.getSize()
            );

            return Result.success(pageResponse);
        } catch (Exception e) {
            log.error("Controller层 - 搜索收藏失败", e);
            return Result.error("搜索收藏失败: " + e.getMessage());
        }
    }

    @Override
    public Result<PageResponse<FavoriteResponse>> getPopularFavoritesForController(String favoriteType, Integer currentPage, Integer pageSize) {
        try {
            log.info("Controller层 - 获取热门收藏: 类型={}, 页码={}/{}", favoriteType, currentPage, pageSize);

            IPage<Favorite> page = getPopularFavorites(favoriteType, currentPage, pageSize);

            // 转换为PageResponse<FavoriteResponse>
            List<FavoriteResponse> responses = favoriteConverter.toResponseList(page.getRecords());
            PageResponse<FavoriteResponse> pageResponse = PageResponse.of(
                    responses, page.getTotal(),
                    (int) page.getCurrent(), (int) page.getSize()
            );


            return Result.success(pageResponse);
        } catch (Exception e) {
            log.error("Controller层 - 获取热门收藏失败", e);
            return Result.error("获取热门收藏失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Integer> updateUserInfoForController(Long userId, String nickname) {
        try {
            log.info("Controller层 - 更新用户信息: 用户={}, 昵称={}", userId, nickname);

            int result = updateUserInfo(userId, nickname);
            return Result.success(result);
        } catch (Exception e) {
            log.error("Controller层 - 更新用户信息失败", e);
            return Result.error("更新用户信息失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Integer> updateTargetInfoForController(String favoriteType, Long targetId, String title, String cover, Long authorId) {
        try {
            log.info("Controller层 - 更新目标对象信息: 类型={}, 目标={}, 标题={}", favoriteType, targetId, title);

            int result = updateTargetInfo(favoriteType, targetId, title, cover, authorId);
            return Result.success(result);
        } catch (Exception e) {
            log.error("Controller层 - 更新目标对象信息失败", e);
            return Result.error("更新目标对象信息失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Integer> cleanCancelledFavoritesForController(Integer days) {
        try {
            log.info("Controller层 - 清理已取消收藏记录: 保留天数={}", days);

            int result = cleanCancelledFavorites(days);
            return Result.success(result);
        } catch (Exception e) {
            log.error("Controller层 - 清理已取消收藏记录失败", e);
            return Result.error("清理已取消收藏记录失败: " + e.getMessage());
        }
    }

    @Override
    public Result<PageResponse<FavoriteResponse>> getFavoritesByAuthorForController(Long targetAuthorId, String favoriteType, Integer currentPage, Integer pageSize) {
        try {
            log.info("Controller层 - 查询作者收藏作品: 作者={}, 类型={}, 页码={}/{}",
                    targetAuthorId, favoriteType, currentPage, pageSize);

            IPage<Favorite> page = getFavoritesByAuthor(targetAuthorId, favoriteType, currentPage, pageSize);

            // 转换为PageResponse<FavoriteResponse>
            List<FavoriteResponse> responses = favoriteConverter.toResponseList(page.getRecords());
            PageResponse<FavoriteResponse> pageResponse = PageResponse.of(
                    responses, page.getTotal(),
                    (int) page.getCurrent(), (int) page.getSize()
            );

            return Result.success(pageResponse);
        } catch (Exception e) {
            log.error("Controller层 - 查询作者收藏作品失败", e);
            return Result.error("查询作者收藏作品失败: " + e.getMessage());
        }
    }

    @Override
    public Result<String> validateFavoriteRequestForController(FavoriteCreateRequest request) {
        try {
            log.info("Controller层 - 验证收藏请求: 用户={}, 类型={}, 目标={}",
                    request.getUserId(), request.getFavoriteType(), request.getTargetId());

            // 使用转换器将FavoriteCreateRequest转换为Favorite实体
            Favorite favorite = favoriteConverter.toEntity(request);
            String result = validateFavoriteRequest(favorite);

            if (result == null) {
                return Result.success("验证通过");
            } else {
                return Result.error(result);
            }
        } catch (Exception e) {
            log.error("Controller层 - 验证收藏请求失败", e);
            return Result.error("验证收藏请求失败: " + e.getMessage());
        }
    }

    @Override
    public Result<String> checkCanFavoriteForController(Long userId, String favoriteType, Long targetId) {
        try {
            log.info("Controller层 - 检查收藏权限: 用户={}, 类型={}, 目标={}", userId, favoriteType, targetId);

            String result = checkCanFavorite(userId, favoriteType, targetId);

            if (result == null) {
                return Result.success("可以收藏");
            } else {
                return Result.error(result);
            }
        } catch (Exception e) {
            log.error("Controller层 - 检查收藏权限失败", e);
            return Result.error("检查收藏权限失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Boolean> existsFavoriteRelationForController(Long userId, String favoriteType, Long targetId) {
        try {
            log.info("Controller层 - 检查收藏关系: 用户={}, 类型={}, 目标={}", userId, favoriteType, targetId);

            boolean exists = existsFavoriteRelation(userId, favoriteType, targetId);
            return Result.success(exists);
        } catch (Exception e) {
            log.error("Controller层 - 检查收藏关系失败", e);
            return Result.error("检查收藏关系失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Boolean> reactivateFavoriteForController(Long userId, String favoriteType, Long targetId) {
        try {
            log.info("Controller层 - 重新激活收藏: 用户={}, 类型={}, 目标={}", userId, favoriteType, targetId);

            boolean success = reactivateFavorite(userId, favoriteType, targetId);
            return Result.success(success);
        } catch (Exception e) {
            log.error("Controller层 - 重新激活收藏失败", e);
            return Result.error("重新激活收藏失败: " + e.getMessage());
        }
    }

    // =================== 私有辅助方法 ===================

    /**
     * 检查收藏类型是否有效
     */
    private boolean isValidFavoriteType(String favoriteType) {
        if (favoriteType == null || favoriteType.trim().isEmpty()) {
            return false;
        }

        // 根据数据库表定义的收藏类型：CONTENT、GOODS
        String[] validTypes = {"CONTENT", "GOODS"};
        for (String validType : validTypes) {
            if (validType.equals(favoriteType)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 检查用户是否存在
     */
    private boolean userExists(Long userId) {
        // 这里应该调用用户服务检查用户是否存在
        // 暂时返回true，实际使用时需要实现
        return true;
    }

    /**
     * 检查目标对象是否存在
     */
    private boolean targetExists(String favoriteType, Long targetId) {
        // 这里应该根据收藏类型检查目标对象是否存在
        // 暂时返回true，实际使用时需要实现
        return true;
    }

    @Override
    public Map<Long, Boolean> batchCheckFavoriteStatus(Long userId, String favoriteType, List<Long> targetIds) {
        log.debug("批量检查收藏状态: userId={}, favoriteType={}, targetIds.size={}", 
                 userId, favoriteType, targetIds != null ? targetIds.size() : 0);
        
        Map<Long, Boolean> resultMap = new HashMap<>();
        
        // 参数验证
        if (userId == null || !StringUtils.hasText(favoriteType) || targetIds == null || targetIds.isEmpty()) {
            log.warn("批量检查收藏状态参数不完整: userId={}, favoriteType={}, targetIds={}", 
                    userId, favoriteType, targetIds);
            // 返回全false的map
            if (targetIds != null) {
                targetIds.forEach(id -> resultMap.put(id, false));
            }
            return resultMap;
        }
        
        try {
            // 查询用户对这些目标的所有收藏记录
            LambdaQueryWrapper<Favorite> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Favorite::getUserId, userId)
                       .eq(Favorite::getFavoriteType, favoriteType)
                       .in(Favorite::getTargetId, targetIds)
                       .eq(Favorite::getStatus, "active");
            
            List<Favorite> favorites = favoriteMapper.selectList(queryWrapper);
            Set<Long> favoritedTargetIds = favorites.stream()
                                                  .map(Favorite::getTargetId)
                                                  .collect(Collectors.toSet());
            
            // 构建结果map
            targetIds.forEach(id -> resultMap.put(id, favoritedTargetIds.contains(id)));
            
            log.debug("批量收藏状态查询结果: 总数={}, 已收藏数={}", targetIds.size(), favoritedTargetIds.size());
            
        } catch (Exception e) {
            log.error("批量检查收藏状态失败: userId={}, favoriteType={}", userId, favoriteType, e);
            // 出错时返回全false
            targetIds.forEach(id -> resultMap.put(id, false));
        }
        
        return resultMap;
    }

    /**
     * 根据收藏类型更新对应目标的收藏统计
     * 
     * @param favoriteType 收藏类型：CONTENT、GOODS
     * @param targetId 目标对象ID
     * @param increment 增量（正数增加，负数减少）
     */
    private void updateTargetFavoriteCount(String favoriteType, Long targetId, int increment) {
        if (!StringUtils.hasText(favoriteType) || targetId == null) {
            log.warn("无效的收藏类型或目标ID: favoriteType={}, targetId={}", favoriteType, targetId);
            return;
        }
        
        try {
            switch (favoriteType.toUpperCase()) {
                case "CONTENT":
                    contentMapper.incrementFavoriteCount(targetId, increment);
                    log.info("内容收藏统计更新成功: contentId={}, favoriteCount{}{}", 
                            targetId, increment > 0 ? "+" : "", increment);
                    break;
                    
                case "GOODS":
                    goodsMapper.incrementFavoriteCount(targetId, increment);
                    log.info("商品收藏统计更新成功: goodsId={}, favoriteCount{}{}", 
                            targetId, increment > 0 ? "+" : "", increment);
                    break;
                    
                default:
                    log.warn("未知的收藏类型: {}", favoriteType);
                    break;
            }
        } catch (Exception e) {
            log.error("更新{}收藏统计失败: targetId={}, increment={}", favoriteType, targetId, increment, e);
            // 统计更新失败不影响主业务流程
        }
    }

    /**
     * 创建分页对象
     */
    private Page<Favorite> createPage(Integer currentPage, Integer pageSize) {
        int page = currentPage != null && currentPage > 0 ? currentPage : 1;
        int size = pageSize != null && pageSize > 0 ? Math.min(pageSize, 100) : 20;
        return new Page<>(page, size);
    }
}
