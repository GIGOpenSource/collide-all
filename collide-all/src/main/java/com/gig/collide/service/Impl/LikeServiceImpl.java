package com.gig.collide.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gig.collide.Apientry.api.common.response.PageResponse;
import com.gig.collide.Apientry.api.common.response.Result;
import com.gig.collide.Apientry.api.like.response.LikeResponse;
import com.gig.collide.domain.Like;
import com.gig.collide.mapper.LikeMapper;
import com.gig.collide.service.LikeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 点赞业务逻辑实现类 - MySQL 8.0 优化版
 * 基于like-simple.sql的业务逻辑，与LikeMapper完全对应
 *
 * 实现特性：
 * - 与LikeMapper方法一一对应
 * - 支持用户、目标对象、作者三个维度的查询
 * - 支持时间范围查询和批量操作
 * - 完整的事务管理和异常处理
 *
 * @author Collide
 * @version 2.0.0 (MySQL 8.0 优化版)
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LikeServiceImpl implements LikeService {

    private final LikeMapper likeMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Like addLike(Like like) {
        log.info("添加点赞: userId={}, likeType={}, targetId={}",
                like.getUserId(), like.getLikeType(), like.getTargetId());

        // 验证请求参数
        String validationResult = validateLikeRequest(like);
        if (validationResult != null) {
            throw new IllegalArgumentException(validationResult);
        }

        // 检查是否已存在点赞记录
        Like existingLike = likeMapper.findByUserAndTarget(
                like.getUserId(), like.getLikeType(), like.getTargetId());

        if (existingLike != null) {
            if (existingLike.isActive()) {
                log.warn("用户已点赞该对象: userId={}, targetId={}", like.getUserId(), like.getTargetId());
                return existingLike;
            } else {
                // 重新激活已取消的点赞
                existingLike.activate();
                // 更新冗余信息
                existingLike.setTargetTitle(like.getTargetTitle());
                existingLike.setTargetAuthorId(like.getTargetAuthorId());
                existingLike.setUserNickname(like.getUserNickname());
                existingLike.setUserAvatar(like.getUserAvatar());

                likeMapper.updateById(existingLike);
                log.info("重新激活点赞记录: id={}", existingLike.getId());
                return existingLike;
            }
        }

        // 创建新的点赞记录
        like.setStatus("active");
        like.setCreateTime(LocalDateTime.now());
        like.setUpdateTime(LocalDateTime.now());

        int result = likeMapper.insert(like);
        if (result > 0) {
            log.info("创建点赞记录成功: id={}", like.getId());
            return like;
        } else {
            throw new RuntimeException("创建点赞记录失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelLike(Long userId, String likeType, Long targetId) {
        log.info("取消点赞: userId={}, likeType={}, targetId={}", userId, likeType, targetId);

        // 查找活跃的点赞记录
        Like existingLike = likeMapper.findByUserAndTarget(userId, likeType, targetId);
        if (existingLike == null || existingLike.isCancelled()) {
            log.warn("未找到活跃的点赞记录: userId={}, targetId={}", userId, targetId);
            return false;
        }

        // 取消点赞
        existingLike.cancel();
        existingLike.setUpdateTime(LocalDateTime.now());

        int result = likeMapper.updateById(existingLike);
        if (result > 0) {
            log.info("取消点赞成功: id={}", existingLike.getId());
            return true;
        } else {
            throw new RuntimeException("取消点赞失败");
        }
    }



    @Override
    public IPage<Like> findUserLikes(Integer pageNum, Integer pageSize, Long userId, String likeType, String status) {
        log.debug("分页查询用户点赞记录: userId={}, likeType={}, status={}, page={}/{}", userId, likeType, status, pageNum, pageSize);

        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        Page<Like> page = createPage(pageNum, pageSize);
        return likeMapper.findUserLikes(page, userId, likeType, status);
    }

    @Override
    public IPage<Like> findTargetLikes(Integer pageNum, Integer pageSize, Long targetId, String likeType, String status) {
        log.debug("分页查询目标对象点赞记录: targetId={}, likeType={}, status={}, page={}/{}", targetId, likeType, status, pageNum, pageSize);

        if (!StringUtils.hasText(likeType) || targetId == null) {
            throw new IllegalArgumentException("点赞类型和目标ID不能为空");
        }

        Page<Like> page = createPage(pageNum, pageSize);
        return likeMapper.findTargetLikes(page, targetId, likeType, status);
    }



    @Override
    public Long countUserLikes(Long userId, String likeType) {
        log.debug("统计用户点赞数: userId={}, likeType={}", userId, likeType);

        if (userId == null) {
            return 0L;
        }

        return likeMapper.countUserLikes(userId, likeType, "active");
    }

    @Override
    public Long countTargetLikes(Long targetId, String likeType) {
        log.debug("统计目标点赞数: targetId={}, likeType={}", targetId, likeType);

        if (!StringUtils.hasText(likeType) || targetId == null) {
            return 0L;
        }

        return likeMapper.countTargetLikes(targetId, likeType, "active");
    }









    @Override
    public Map<Long, Boolean> batchCheckLikeStatus(Long userId, String likeType, List<Long> targetIds) {
        log.debug("批量检查点赞状态: userId={}, likeType={}, targetIds.size={}", userId, likeType, targetIds.size());

        if (userId == null || !StringUtils.hasText(likeType) || targetIds == null || targetIds.isEmpty()) {
            return new HashMap<>();
        }

        List<Long> likedTargetIds = likeMapper.batchCheckLikeStatus(userId, likeType, targetIds);

        // 构建结果Map：targetId -> isLiked
        Map<Long, Boolean> resultMap = new HashMap<>();
        for (Long targetId : targetIds) {
            resultMap.put(targetId, likedTargetIds.contains(targetId));
        }

        return resultMap;
    }

    // =================== 私有方法 ===================

    @Override
    public String validateLikeRequest(Like like) {
        if (like == null) {
            return "点赞对象不能为空";
        }
        if (like.getUserId() == null) {
            return "用户ID不能为空";
        }
        if (!StringUtils.hasText(like.getLikeType())) {
            return "点赞类型不能为空";
        }
        if (like.getTargetId() == null) {
            return "目标ID不能为空";
        }
        return null;
    }

    /**
     * 创建分页对象
     */
    private Page<Like> createPage(Integer currentPage, Integer pageSize) {
        int page = currentPage != null && currentPage > 0 ? currentPage : 1;
        int size = pageSize != null && pageSize > 0 ? Math.min(pageSize, 100) : 20;
        return new Page<>(page, size);
    }

    @Override
    public Like getLikeById(Long id) {
        log.debug("根据ID获取点赞记录: id={}", id);

        if (id == null) {
            throw new IllegalArgumentException("点赞ID不能为空");
        }

        return likeMapper.selectById(id);
    }

    @Override
    public List<Like> findByTimeRange(LocalDateTime startTime, LocalDateTime endTime,
                                      String likeType, String status) {
        log.debug("查询时间范围内的点赞记录: startTime={}, endTime={}, likeType={}, status={}",
                startTime, endTime, likeType, status);

        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("开始时间和结束时间不能为空");
        }

        return likeMapper.findByTimeRange(startTime, endTime, likeType, status);
    }

    @Override
    public Like getLikeRecord(Long userId, String likeType, Long targetId) {
        log.debug("获取用户对目标对象的点赞记录: userId={}, likeType={}, targetId={}", userId, likeType, targetId);

        if (userId == null || !StringUtils.hasText(likeType) || targetId == null) {
            return null;
        }

        return likeMapper.findByUserAndTarget(userId, likeType, targetId);
    }

    @Override
    public IPage<Like> findAuthorLikes(Integer pageNum, Integer pageSize, Long targetAuthorId, String likeType, String status) {
        log.debug("分页查询作者作品点赞记录: targetAuthorId={}, likeType={}, status={}, page={}/{}",
                targetAuthorId, likeType, status, pageNum, pageSize);

        if (targetAuthorId == null) {
            throw new IllegalArgumentException("作者ID不能为空");
        }

        Page<Like> page = createPage(pageNum, pageSize);
        return likeMapper.findAuthorLikes(page, targetAuthorId, likeType, status);
    }

    @Override
    public Long countAuthorLikes(Long targetAuthorId, String likeType) {
        log.debug("统计作者作品被点赞数量: targetAuthorId={}, likeType={}", targetAuthorId, likeType);

        if (targetAuthorId == null) {
            return 0L;
        }

        return likeMapper.countAuthorLikes(targetAuthorId, likeType, "active");
    }

    @Override
    public Like toggleLike(Like like) {
        log.info("切换点赞状态: userId={}, likeType={}, targetId={}",
                like.getUserId(), like.getLikeType(), like.getTargetId());

        // 验证请求参数
        String validationResult = validateLikeRequest(like);
        if (validationResult != null) {
            throw new IllegalArgumentException(validationResult);
        }

        // 检查是否已存在点赞记录
        Like existingLike = likeMapper.findByUserAndTarget(
                like.getUserId(), like.getLikeType(), like.getTargetId());

        if (existingLike != null && existingLike.isActive()) {
            // 如果已点赞，则取消点赞
            cancelLike(like.getUserId(), like.getLikeType(), like.getTargetId());
            return null; // 返回null表示已取消点赞
        } else {
            // 如果未点赞或已取消，则添加点赞
            return addLike(like);
        }
    }

    @Override
    public boolean checkLikeStatus(Long userId, String likeType, Long targetId) {
        log.info("检查点赞状态: userId={}, likeType={}, targetId={}", userId, likeType, targetId);

        if (userId == null || !StringUtils.hasText(likeType) || targetId == null) {
            log.warn("参数验证失败: userId={}, likeType={}, targetId={}", userId, likeType, targetId);
            return false;
        }

        Like like = likeMapper.findByUserAndTarget(userId, likeType, targetId);
        log.info("查询结果: like={}", like);
        
        if (like != null) {
            log.info("点赞记录存在: id={}, status={}", like.getId(), like.getStatus());
        } else {
            log.info("未找到点赞记录");
        }
        
        // 检查记录是否存在且状态为active
        boolean result = like != null && like.isActive();
        log.info("最终结果: {}", result);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteLike(Long id) {
        log.info("删除点赞记录: id={}", id);

        if (id == null) {
            throw new IllegalArgumentException("点赞ID不能为空");
        }

        // 检查点赞记录是否存在
        Like existingLike = likeMapper.selectById(id);
        if (existingLike == null) {
            log.warn("点赞记录不存在: id={}", id);
            return false;
        }

        // 执行物理删除
        int result = likeMapper.deleteById(id);
        boolean success = result > 0;

        if (success) {
            log.info("点赞记录删除成功: id={}", id);
        } else {
            log.warn("点赞记录删除失败: id={}", id);
        }

        return success;
    }

    // =================== Controller专用方法 ===================

    @Override
    public Result<PageResponse<LikeResponse>> listLikesForController(
            Long userId, Long targetId, String likeType, String targetType, String status,
            String orderBy, String orderDirection, Integer currentPage, Integer pageSize) {
        try {
            log.info("Controller层 - 点赞列表查询: userId={}, targetId={}, likeType={}, page={}/{}", 
                    userId, targetId, likeType, currentPage, pageSize);

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
            LambdaQueryWrapper<Like> queryWrapper = new LambdaQueryWrapper<>();

            // 动态查询条件
            if (userId != null) {
                queryWrapper.eq(Like::getUserId, userId);
            }
            if (targetId != null) {
                queryWrapper.eq(Like::getTargetId, targetId);
            }
            if (StringUtils.hasText(likeType)) {
                queryWrapper.eq(Like::getLikeType, likeType);
            }
            // targetType 字段在当前 Like 实体中不存在，暂时注释掉
            // if (StringUtils.hasText(targetType)) {
            //     queryWrapper.eq(Like::getTargetType, targetType);
            // }
            if (StringUtils.hasText(status)) {
                queryWrapper.eq(Like::getStatus, status);
            }

            // 排序
            if ("updateTime".equals(orderBy)) {
                if ("ASC".equals(orderDirection)) {
                    queryWrapper.orderByAsc(Like::getUpdateTime);
                } else {
                    queryWrapper.orderByDesc(Like::getUpdateTime);
                }
            } else {
                // 默认按创建时间排序
                if ("ASC".equals(orderDirection)) {
                    queryWrapper.orderByAsc(Like::getCreateTime);
                } else {
                    queryWrapper.orderByDesc(Like::getCreateTime);
                }
            }

            // 使用MyBatis-Plus分页
            Page<Like> page = new Page<>(currentPage, pageSize);
            IPage<Like> result = likeMapper.selectPage(page, queryWrapper);

            // 转换为Response对象
            List<LikeResponse> responses = result.getRecords().stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());

            // 构建分页响应
            PageResponse<LikeResponse> pageResponse = new PageResponse<>();
            pageResponse.setRecords(responses);
            pageResponse.setTotal(result.getTotal());
            pageResponse.setCurrentPage(currentPage);
            pageResponse.setPageSize(pageSize);
            pageResponse.setTotalPages((int) Math.ceil((double) result.getTotal() / pageSize));

            log.info("点赞列表查询成功: 总数={}, 当前页={}, 页面大小={}", result.getTotal(), currentPage, pageSize);
            return Result.success(pageResponse);

        } catch (Exception e) {
            log.error("Controller层 - 点赞列表查询失败", e);
            return Result.error("点赞列表查询失败: " + e.getMessage());
        }
    }

    /**
     * 将Like实体转换为LikeResponse
     */
    private LikeResponse convertToResponse(Like like) {
        if (like == null) {
            return null;
        }

        LikeResponse response = new LikeResponse();
        response.setId(like.getId());
        response.setUserId(like.getUserId());
        response.setTargetId(like.getTargetId());
        response.setLikeType(like.getLikeType());
        // targetType 字段在当前 Like 实体中不存在，暂时设置为 null
        //response.setTargetType(null);
        response.setStatus(like.getStatus());
        response.setTargetTitle(like.getTargetTitle());
        response.setTargetAuthorId(like.getTargetAuthorId());
        response.setUserNickname(like.getUserNickname());
        response.setUserAvatar(like.getUserAvatar());
        response.setCreateTime(like.getCreateTime());
        response.setUpdateTime(like.getUpdateTime());

        return response;
    }
}
