package com.gig.collide.Apientry.api.social;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gig.collide.Apientry.api.common.response.PageResponse;
import com.gig.collide.Apientry.api.common.response.Result;
import com.gig.collide.Apientry.api.social.request.SocialDynamicCreateRequest;
import com.gig.collide.Apientry.api.social.request.SocialDynamicQueryRequest;
import com.gig.collide.Apientry.api.social.request.SocialDynamicUpdateRequest;
import com.gig.collide.Apientry.api.social.response.SocialDynamicResponse;
import com.gig.collide.domain.SocialDynamic;
import com.gig.collide.domain.Content;
import com.gig.collide.domain.User;
import com.gig.collide.service.*;
import com.gig.collide.cache.LikeCacheConstant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 社交动态门面服务实现类 - 重新设计版
 * 实现SocialFacadeService接口的所有方法
 * 负责请求转换、结果包装、异常处理
 *
 * @author GIG Team
 * @version 3.0.0 (重新设计版)
 * @since 2024-01-30
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SocialServiceImpl implements SocialService {

    private final SocialDynamicService socialDynamicService;
    private final ContentService contentService;
    private final LikeService likeService;
    private final FollowService followService;
    private final UserService userService;
    private final CommentService commentService;
    
    // =================== 业务CRUD操作 ===================

    @Override
    public Result<SocialDynamicResponse> createDynamic(SocialDynamicCreateRequest request) {
        try {
            log.debug("门面服务 - 创建动态: 用户ID={}, 类型={}", request.getUserId(), request.getDynamicType());
            
            // 请求转换为实体
            SocialDynamic dynamic = convertToEntity(request);
            
            // 调用Service层创建动态
            SocialDynamic result = socialDynamicService.createDynamic(dynamic);
            
            // 同时写入t_content表
            Content content = createContentFromDynamic(request, result);
            contentService.createContent(content);
            log.info("同步创建内容成功: dynamicId={}, contentId={}", result.getId(), content.getId());
            
            // 结果转换
            SocialDynamicResponse response = convertToResponse(result);
            
            return Result.success(response);
        } catch (Exception e) {
            log.error("创建动态失败: {}", e.getMessage(), e);
            return Result.error("创建动态失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Integer> batchCreateDynamics(List<SocialDynamicCreateRequest> requests, Long operatorId) {
        try {
            log.debug("门面服务 - 批量创建动态: 数量={}, 操作者ID={}", requests.size(), operatorId);
            
            // 请求转换
            List<SocialDynamic> dynamics = requests.stream()
                    .map(this::convertToEntity)
                    .collect(Collectors.toList());
            
            // 调用Service层
            int result = socialDynamicService.batchCreateDynamics(dynamics);
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("批量创建动态失败: {}", e.getMessage(), e);
            return Result.error("批量创建动态失败: " + e.getMessage());
        }
    }

    @Override
    public Result<SocialDynamicResponse> createShareDynamic(SocialDynamicCreateRequest request) {
        try {
            log.debug("门面服务 - 创建分享动态: 用户ID={}, 分享目标={}", request.getUserId(), request.getShareTargetType());
            
            // 请求转换为实体
            SocialDynamic dynamic = convertToEntity(request);
            
            // 调用Service层
            SocialDynamic result = socialDynamicService.createShareDynamic(dynamic);
            
            // 结果转换
            SocialDynamicResponse response = convertToResponse(result);
            
            return Result.success(response);
        } catch (Exception e) {
            log.error("创建分享动态失败: {}", e.getMessage(), e);
            return Result.error("创建分享动态失败: " + e.getMessage());
        }
    }

    @Override
    public Result<SocialDynamicResponse> updateDynamic(SocialDynamicUpdateRequest request) {
        try {
            log.debug("门面服务 - 更新动态: 动态ID={}, 用户ID={}", request.getId(), request.getUserId());
            
            // 先查询原动态
            SocialDynamic existingDynamic = socialDynamicService.selectById(request.getId());
            if (existingDynamic == null) {
                return Result.error("动态不存在");
            }
            
            // 权限验证
            if (!existingDynamic.getUserId().equals(request.getUserId())) {
                return Result.error("无权限更新此动态");
            }
            
            // 更新字段
            if (StringUtils.hasText(request.getContent())) {
                existingDynamic.setContent(request.getContent());
            }
            if (StringUtils.hasText(request.getDynamicType())) {
                existingDynamic.setDynamicType(request.getDynamicType());
            }
            if (StringUtils.hasText(request.getImages())) {
                existingDynamic.setImages(request.getImages());
            }
            if (StringUtils.hasText(request.getVideoUrl())) {
                existingDynamic.setVideoUrl(request.getVideoUrl());
            }
            if (StringUtils.hasText(request.getStatus())) {
                existingDynamic.setStatus(request.getStatus());
            }
            
            // 调用Service层更新
            int result = socialDynamicService.updateById(existingDynamic);
            if (result > 0) {
                // 重新查询更新后的数据
                SocialDynamic updatedDynamic = socialDynamicService.selectById(existingDynamic.getId());
                SocialDynamicResponse response = convertToResponse(updatedDynamic);
                return Result.success(response);
            } else {
                return Result.error("更新动态失败");
            }
        } catch (Exception e) {
            log.error("更新动态失败: {}", e.getMessage(), e);
            return Result.error("更新动态失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Void> deleteDynamic(Long dynamicId, Long operatorId) {
        try {
            log.debug("门面服务 - 删除动态: 动态ID={}, 操作者ID={}", dynamicId, operatorId);
            
            // 先查询动态
            SocialDynamic dynamic = socialDynamicService.selectById(dynamicId);
            if (dynamic == null) {
                return Result.error("动态不存在");
            }
            
            // 权限验证（只有动态作者或管理员可以删除）
            if (!dynamic.getUserId().equals(operatorId)) {
                return Result.error("无权限删除此动态");
            }
            
            // 逻辑删除
            int result = socialDynamicService.updateStatus(dynamicId, "deleted", operatorId);
            if (result > 0) {
                return Result.success();
            } else {
                return Result.error("删除动态失败");
            }
        } catch (Exception e) {
            log.error("删除动态失败: {}", e.getMessage(), e);
            return Result.error("删除动态失败: " + e.getMessage());
        }
    }

    @Override
    public Result<SocialDynamicResponse> getDynamicById(Long dynamicId, Long currentUserId, Boolean includeDeleted) {
        try {
            log.debug("门面服务 - 查询动态详情: 动态ID={}, 当前用户ID={}, 包含已删除={}", dynamicId, currentUserId, includeDeleted);
            
            SocialDynamic dynamic = socialDynamicService.selectById(dynamicId);

            if (dynamic == null) {
                return Result.error("动态不存在");
            }
            
            // 查询并设置评论数量
            long commentCount = commentService.countTargetComments(dynamicId, "DYNAMIC");
            dynamic.setCommentCount(commentCount);
            
            // 如果不包含已删除且动态已删除，则返回错误
            if (!includeDeleted && "deleted".equals(dynamic.getStatus())) {
                return Result.error("动态已删除");
            }
            
            // 使用包含互动状态的转换方法，传入当前用户ID
            SocialDynamicResponse response = convertToResponseWithInteractionStatus(dynamic, currentUserId);
            return Result.success(response);
        } catch (Exception e) {
            log.error("查询动态详情失败: {}", e.getMessage(), e);
            return Result.error("查询动态详情失败: " + e.getMessage());
        }
    }

    @Override
    public Result<PageResponse<SocialDynamicResponse>> queryDynamics(SocialDynamicQueryRequest request) {
        try {
            log.debug("门面服务 - 分页查询动态: 页码={}, 大小={}, 当前用户ID={}", 
                    request.getPageNumber(), request.getPageSize(), request.getCurrentUserId());
            
            // 构建查询条件
            Page<SocialDynamic> page = new Page<>(request.getPageNumber(), request.getPageSize());
            
            // 根据条件调用不同的查询方法
            IPage<SocialDynamic> result;
            if (request.getUserId() != null) {
                result = socialDynamicService.selectByUserId(page, request.getUserId(), request.getStatus(), request.getDynamicType());
            } else if (StringUtils.hasText(request.getDynamicType())) {
                result = socialDynamicService.selectByDynamicType(page, request.getDynamicType(), request.getStatus());
            } else if (StringUtils.hasText(request.getStatus())) {
                result = socialDynamicService.selectByStatus(page, request.getStatus());
            } else if (StringUtils.hasText(request.getKeyword())) {
                result = socialDynamicService.searchByContent(page, request.getKeyword(), request.getStatus());
            } else {
                // 默认查询所有正常状态的动态
                result = socialDynamicService.selectByStatus(page, "normal");
            }
            
            // 结果转换，包含互动状态
            PageResponse<SocialDynamicResponse> response = convertToPageResponseWithInteractionStatus(result, request.getCurrentUserId());
            return Result.success(response);
        } catch (Exception e) {
            log.error("分页查询动态失败: {}", e.getMessage(), e);
            return Result.error("分页查询动态失败: " + e.getMessage());
        }
    }

    // =================== 核心查询方法 ===================

    @Override
    public Result<PageResponse<SocialDynamicResponse>> selectByUserId(Integer pageNumber, Integer pageSize, Long userId, String status, String dynamicType) {
        try {
            log.debug("门面服务 - 根据用户ID查询动态: 用户ID={}, 状态={}, 类型={}, 页码={}/{}", 
                    userId, status, dynamicType, pageNumber, pageSize);
            
            Page<SocialDynamic> page = new Page<>(pageNumber, pageSize);
            IPage<SocialDynamic> result = socialDynamicService.selectByUserId(page, userId, status, dynamicType);
            
            // 使用查询用户ID作为当前用户ID来获取互动状态
            PageResponse<SocialDynamicResponse> response = convertToPageResponseWithInteractionStatus(result, userId);
            return Result.success(response);
        } catch (Exception e) {
            log.error("根据用户ID查询动态失败: {}", e.getMessage(), e);
            return Result.error("根据用户ID查询动态失败: " + e.getMessage());
        }
    }

    @Override
    public Result<PageResponse<SocialDynamicResponse>> selectByDynamicType(Integer pageNumber, Integer pageSize, String dynamicType, String status, Long currentUserId) {
        try {
            log.debug("门面服务 - 根据动态类型查询: 类型={}, 状态={}, 页码={}/{}", dynamicType, status, pageNumber, pageSize);
            
            Page<SocialDynamic> page = new Page<>(pageNumber, pageSize);
            IPage<SocialDynamic> result = socialDynamicService.selectByDynamicType(page, dynamicType, status);
            
            PageResponse<SocialDynamicResponse> response = convertToPageResponseWithInteractionStatus(result, currentUserId);
            return Result.success(response);
        } catch (Exception e) {
            log.error("根据动态类型查询失败: {}", e.getMessage(), e);
            return Result.error("根据动态类型查询失败: " + e.getMessage());
        }
    }

    @Override
    public Result<PageResponse<SocialDynamicResponse>> selectByStatus(Integer pageNumber, Integer pageSize, String status, Long currentUserId) {
        try {
            log.debug("门面服务 - 根据状态查询动态: 状态={}, 页码={}/{}", status, pageNumber, pageSize);
            
            Page<SocialDynamic> page = new Page<>(pageNumber, pageSize);
            IPage<SocialDynamic> result = socialDynamicService.selectByStatus(page, status);
            
            PageResponse<SocialDynamicResponse> response = convertToPageResponseWithInteractionStatus(result, currentUserId);
            return Result.success(response);
        } catch (Exception e) {
            log.error("根据状态查询动态失败: {}", e.getMessage(), e);
            return Result.error("根据状态查询动态失败: " + e.getMessage());
        }
    }

    @Override
    public Result<PageResponse<SocialDynamicResponse>> selectFollowingDynamics(Integer pageNumber, Integer pageSize, Long userId, String status, Long currentUserId) {
        try {
            log.debug("门面服务 - 获取关注用户动态流: 用户ID={}, 状态={}, 页码={}/{}", userId, status, pageNumber, pageSize);
            
            // 获取用户关注的其他用户ID列表
            List<Long> followingUserIds;
            try {
                followingUserIds = followService.getFollowingAuthorIds(userId);
                log.debug("获取关注用户ID列表成功: userId={}, followingCount={}", userId, followingUserIds.size());
            } catch (Exception e) {
                log.warn("获取关注用户ID列表失败: userId={}, error={}", userId, e.getMessage(), e);
                followingUserIds = List.of(); // 如果获取失败，使用空列表
            }
            // 防御性短路：当关注列表为空时，直接返回空分页，避免下发无意义SQL
            if (followingUserIds == null || followingUserIds.isEmpty()) {
                PageResponse<SocialDynamicResponse> empty = new PageResponse<>();
                empty.setRecords(java.util.Collections.emptyList());
                Integer cp = (pageNumber != null && pageNumber > 0) ? pageNumber : 1;
                Integer ps = (pageSize != null && pageSize > 0) ? pageSize : 20;
                // 同步设置 total/currentPage/pageSize，并计算 totalPages/hasNext/hasPrevious
                empty.setPagination(0L, cp, ps);
                return Result.success(empty);
            }
            
            Page<SocialDynamic> page = new Page<>(pageNumber, pageSize);
            IPage<SocialDynamic> result = socialDynamicService.selectFollowingDynamics(page, followingUserIds, status);
            
            PageResponse<SocialDynamicResponse> response = convertToPageResponseWithInteractionStatus(result, currentUserId);
            return Result.success(response);
        } catch (Exception e) {
            log.error("获取关注用户动态流失败: {}", e.getMessage(), e);
            return Result.error("获取关注用户动态流失败: " + e.getMessage());
        }
    }

    @Override
    public Result<PageResponse<SocialDynamicResponse>> searchByContent(Integer pageNumber, Integer pageSize, String keyword, String status, Long currentUserId) {
        try {
            log.debug("门面服务 - 搜索动态内容: 关键词={}, 状态={}, 页码={}/{}", keyword, status, pageNumber, pageSize);
            
            Page<SocialDynamic> page = new Page<>(pageNumber, pageSize);
            IPage<SocialDynamic> result = socialDynamicService.searchByContent(page, keyword, status);
            
            PageResponse<SocialDynamicResponse> response = convertToPageResponseWithInteractionStatus(result, currentUserId);
            return Result.success(response);
        } catch (Exception e) {
            log.error("搜索动态内容失败: {}", e.getMessage(), e);
            return Result.error("搜索动态内容失败: " + e.getMessage());
        }
    }

    @Override
    public Result<PageResponse<SocialDynamicResponse>> selectHotDynamics(Integer pageNumber, Integer pageSize, String status, String dynamicType, Long currentUserId) {
        try {
            log.debug("门面服务 - 查询热门动态: 状态={}, 类型={}, 页码={}/{}", status, dynamicType, pageNumber, pageSize);
            
            Page<SocialDynamic> page = new Page<>(pageNumber, pageSize);
            IPage<SocialDynamic> result = socialDynamicService.selectHotDynamics(page, status, dynamicType);
            
            PageResponse<SocialDynamicResponse> response = convertToPageResponseWithInteractionStatus(result, currentUserId);
            return Result.success(response);
        } catch (Exception e) {
            log.error("查询热门动态失败: {}", e.getMessage(), e);
            return Result.error("查询热门动态失败: " + e.getMessage());
        }
    }

    @Override
    public Result<PageResponse<SocialDynamicResponse>> selectByShareTarget(Integer pageNumber, Integer pageSize, String shareTargetType, Long shareTargetId, String status, Long currentUserId) {
        try {
            log.debug("门面服务 - 根据分享目标查询: 类型={}, 目标ID={}, 状态={}, 页码={}/{}", 
                    shareTargetType, shareTargetId, status, pageNumber, pageSize);
            
            Page<SocialDynamic> page = new Page<>(pageNumber, pageSize);
            IPage<SocialDynamic> result = socialDynamicService.selectByShareTarget(page, shareTargetType, shareTargetId, status);
            
            PageResponse<SocialDynamicResponse> response = convertToPageResponseWithInteractionStatus(result, currentUserId);
            return Result.success(response);
        } catch (Exception e) {
            log.error("根据分享目标查询失败: {}", e.getMessage(), e);
            return Result.error("根据分享目标查询失败: " + e.getMessage());
        }
    }

    // =================== 统计计数方法 ===================

    @Override
    public Result<Long> countByUserId(Long userId, String status, String dynamicType) {
        try {
            log.debug("门面服务 - 统计用户动态数量: 用户ID={}, 状态={}, 类型={}", userId, status, dynamicType);
            
            Long count = socialDynamicService.countByUserId(userId, status, dynamicType);
            return Result.success(count);
        } catch (Exception e) {
            log.error("统计用户动态数量失败: {}", e.getMessage(), e);
            return Result.error("统计用户动态数量失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Long> countByDynamicType(String dynamicType, String status) {
        try {
            log.debug("门面服务 - 统计动态类型数量: 类型={}, 状态={}", dynamicType, status);
            
            Long count = socialDynamicService.countByDynamicType(dynamicType, status);
            return Result.success(count);
        } catch (Exception e) {
            log.error("统计动态类型数量失败: {}", e.getMessage(), e);
            return Result.error("统计动态类型数量失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Long> countByTimeRange(LocalDateTime startTime, LocalDateTime endTime, String status) {
        try {
            log.debug("门面服务 - 统计时间范围动态数量: 开始时间={}, 结束时间={}, 状态={}", startTime, endTime, status);
            
            Long count = socialDynamicService.countByTimeRange(startTime, endTime, status);
            return Result.success(count);
        } catch (Exception e) {
            log.error("统计时间范围动态数量失败: {}", e.getMessage(), e);
            return Result.error("统计时间范围动态数量失败: " + e.getMessage());
        }
    }

    // =================== 互动统计更新 ===================

    @Override
    public Result<Integer> increaseLikeCount(Long dynamicId, Long operatorId) {
        try {
            log.debug("门面服务 - 增加点赞数: 动态ID={}, 操作者ID={}", dynamicId, operatorId);
            
            int result = socialDynamicService.increaseLikeCount(dynamicId, operatorId);
            return Result.success(result);
        } catch (Exception e) {
            log.error("增加点赞数失败: {}", e.getMessage(), e);
            return Result.error("增加点赞数失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Integer> decreaseLikeCount(Long dynamicId, Long operatorId) {
        try {
            log.debug("门面服务 - 减少点赞数: 动态ID={}, 操作者ID={}", dynamicId, operatorId);
            
            int result = socialDynamicService.decreaseLikeCount(dynamicId, operatorId);
            return Result.success(result);
        } catch (Exception e) {
            log.error("减少点赞数失败: {}", e.getMessage(), e);
            return Result.error("减少点赞数失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Integer> increaseCommentCount(Long dynamicId, Long operatorId) {
        try {
            log.debug("门面服务 - 增加评论数: 动态ID={}, 操作者ID={}", dynamicId, operatorId);
            
            int result = socialDynamicService.increaseCommentCount(dynamicId, operatorId);
            return Result.success(result);
        } catch (Exception e) {
            log.error("增加评论数失败: {}", e.getMessage(), e);
            return Result.error("增加评论数失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Integer> increaseShareCount(Long dynamicId, Long operatorId) {
        try {
            log.debug("门面服务 - 增加分享数: 动态ID={}, 操作者ID={}", dynamicId, operatorId);
            
            int result = socialDynamicService.increaseShareCount(dynamicId, operatorId);
            return Result.success(result);
        } catch (Exception e) {
            log.error("增加分享数失败: {}", e.getMessage(), e);
            return Result.error("增加分享数失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Integer> updateStatistics(Long dynamicId, Long likeCount, Long commentCount, Long shareCount, Long operatorId) {
        try {
            log.debug("门面服务 - 批量更新统计数据: 动态ID={}, 点赞数={}, 评论数={}, 分享数={}, 操作者ID={}", 
                    dynamicId, likeCount, commentCount, shareCount, operatorId);
            
            int result = socialDynamicService.updateStatistics(dynamicId, likeCount, commentCount, shareCount, operatorId);
            return Result.success(result);
        } catch (Exception e) {
            log.error("批量更新统计数据失败: {}", e.getMessage(), e);
            return Result.error("批量更新统计数据失败: " + e.getMessage());
        }
    }

    // =================== 状态管理 ===================

    @Override
    public Result<Integer> updateStatus(Long dynamicId, String status, Long operatorId) {
        try {
            log.debug("门面服务 - 更新动态状态: 动态ID={}, 新状态={}, 操作者ID={}", dynamicId, status, operatorId);
            
            int result = socialDynamicService.updateStatus(dynamicId, status, operatorId);
            return Result.success(result);
        } catch (Exception e) {
            log.error("更新动态状态失败: {}", e.getMessage(), e);
            return Result.error("更新动态状态失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Integer> batchUpdateStatus(List<Long> dynamicIds, String status, Long operatorId) {
        try {
            log.debug("门面服务 - 批量更新动态状态: 动态数量={}, 新状态={}, 操作者ID={}", dynamicIds.size(), status, operatorId);
            
            int result = socialDynamicService.batchUpdateStatus(dynamicIds, status, operatorId);
            return Result.success(result);
        } catch (Exception e) {
            log.error("批量更新动态状态失败: {}", e.getMessage(), e);
            return Result.error("批量更新动态状态失败: " + e.getMessage());
        }
    }

    // =================== 用户信息同步 ===================

    @Override
    public Result<Integer> updateUserInfo(Long userId, String userNickname, String userAvatar, Long operatorId) {
        try {
            log.debug("门面服务 - 更新用户冗余信息: 用户ID={}, 昵称={}, 操作者ID={}", userId, userNickname, operatorId);
            
            int result = socialDynamicService.updateUserInfo(userId, userNickname, userAvatar, operatorId);
            return Result.success(result);
        } catch (Exception e) {
            log.error("更新用户冗余信息失败: {}", e.getMessage(), e);
            return Result.error("更新用户冗余信息失败: " + e.getMessage());
        }
    }

    // =================== 数据清理 ===================

    @Override
    public Result<Integer> deleteByStatusAndTime(String status, LocalDateTime beforeTime, Integer limit, Long operatorId) {
        try {
            log.debug("门面服务 - 清理历史动态: 状态={}, 时间={}, 限制={}, 操作者ID={}", status, beforeTime, limit, operatorId);
            
            int result = socialDynamicService.deleteByStatusAndTime(status, beforeTime, limit, operatorId);
            return Result.success(result);
        } catch (Exception e) {
            log.error("清理历史动态失败: {}", e.getMessage(), e);
            return Result.error("清理历史动态失败: " + e.getMessage());
        }
    }

    // =================== 特殊查询 ===================

    @Override
    public Result<List<SocialDynamicResponse>> selectLatestDynamics(Integer limit, String status) {
        try {
            log.debug("门面服务 - 查询最新动态: 限制数量={}, 状态={}", limit, status);
            
            List<SocialDynamic> dynamics = socialDynamicService.selectLatestDynamics(limit, status);
            List<SocialDynamicResponse> responses = dynamics.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
            
            return Result.success(responses);
        } catch (Exception e) {
            log.error("查询最新动态失败: {}", e.getMessage(), e);
            return Result.error("查询最新动态失败: " + e.getMessage());
        }
    }

    @Override
    public Result<List<SocialDynamicResponse>> selectUserLatestDynamics(Long userId, Integer limit, String status) {
        try {
            log.debug("门面服务 - 查询用户最新动态: 用户ID={}, 限制数量={}, 状态={}", userId, limit, status);
            
            List<SocialDynamic> dynamics = socialDynamicService.selectUserLatestDynamics(userId, limit, status);
            List<SocialDynamicResponse> responses = dynamics.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
            
            return Result.success(responses);
        } catch (Exception e) {
            log.error("查询用户最新动态失败: {}", e.getMessage(), e);
            return Result.error("查询用户最新动态失败: " + e.getMessage());
        }
    }

    @Override
    public Result<List<SocialDynamicResponse>> selectShareDynamics(String shareTargetType, Integer limit, String status) {
        try {
            log.debug("门面服务 - 查询分享动态列表: 目标类型={}, 限制数量={}, 状态={}", shareTargetType, limit, status);
            
            List<SocialDynamic> dynamics = socialDynamicService.selectShareDynamics(shareTargetType, limit, status);
            List<SocialDynamicResponse> responses = dynamics.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
            
            return Result.success(responses);
        } catch (Exception e) {
            log.error("查询分享动态列表失败: {}", e.getMessage(), e);
            return Result.error("查询分享动态列表失败: " + e.getMessage());
        }
    }

    // =================== 系统健康检查 ===================

    @Override
    public Result<String> healthCheck() {
        try {
            log.debug("门面服务 - 系统健康检查");
            
            // 简单的健康检查：查询一条动态
            List<SocialDynamic> dynamics = socialDynamicService.selectLatestDynamics(1, "normal");
            
            if (dynamics != null) {
                return Result.success("社交动态系统运行正常");
            } else {
                return Result.error("社交动态系统异常");
            }
        } catch (Exception e) {
            log.error("系统健康检查失败: {}", e.getMessage(), e);
            return Result.error("系统健康检查失败: " + e.getMessage());
        }
    }

    // =================== 私有辅助方法 ===================

    /**
     * 从社交动态创建对应的内容对象
     * 将社交动态数据映射到t_content表结构
     */
    private Content createContentFromDynamic(SocialDynamicCreateRequest request, SocialDynamic dynamic) {
        Content content = new Content();
        
        // 基础信息映射
        content.setTitle(StringUtils.hasText(request.getTitle()) ? request.getTitle() : "社交动态-" + dynamic.getId());
        content.setDescription(request.getContent());
        
        // 根据动态类型设置内容类型
        String contentType = mapDynamicTypeToContentType(request.getDynamicType());
        content.setContentType(contentType);
        
        // 内容数据：将动态的具体信息组织为JSON格式
        String contentData = buildContentData(request);
        content.setContentData(contentData);
        
        // 封面图片：如果有图片则使用第一张作为封面
        if (StringUtils.hasText(request.getImages())) {
            content.setCoverUrl(extractFirstImage(request.getImages()));
        }
        
        // 作者信息（从动态的用户信息映射）
        content.setAuthorId(request.getUserId());
        content.setAuthorNickname(request.getUserNickname());
        content.setAuthorAvatar(request.getUserAvatar());
        
        // 分类信息：动态默认归类为"社交动态"分类
        content.setCategoryName("社交动态");
        
        // 状态设置：根据动态是否免费设置内容状态
        content.setStatus("PUBLISHED"); // 动态默认发布
        content.setReviewStatus("APPROVED"); // 动态默认审核通过
        
        // 统计数据初始化（从动态同步）
        content.setViewCount(0L);
        content.setLikeCount(dynamic.getLikeCount() != null ? dynamic.getLikeCount() : 0L);
        content.setCommentCount(dynamic.getCommentCount() != null ? dynamic.getCommentCount() : 0L);
        content.setFavoriteCount(0L);
        content.setShareCount(dynamic.getShareCount() != null ? dynamic.getShareCount() : 0L);
        content.setScoreCount(0L);
        content.setScoreTotal(0L);
        
        // 时间信息
        content.setPublishTime(dynamic.getCreateTime());
        
        log.debug("创建内容对象: title={}, contentType={}, authorId={}", 
                content.getTitle(), content.getContentType(), content.getAuthorId());
        
        return content;
    }
    
    /**
     * 将动态类型映射为内容类型
     */
    private String mapDynamicTypeToContentType(String dynamicType) {
        if (!StringUtils.hasText(dynamicType)) {
            return "ARTICLE";
        }
        
        switch (dynamicType.toLowerCase()) {
            case "text":
                return "ARTICLE";
            case "image":
                return "ARTICLE"; // 图文动态归类为文章
            case "video":
                return "VIDEO";
            case "share":
                return "ARTICLE";
            default:
                return "ARTICLE";
        }
    }
    
    /**
     * 构建内容数据JSON
     */
    private String buildContentData(SocialDynamicCreateRequest request) {
        try {
            // 构建包含动态完整信息的JSON数据
            StringBuilder contentData = new StringBuilder();
            contentData.append("{");
            contentData.append("\"type\":\"social_dynamic\",");
            contentData.append("\"content\":\"").append(escapeJson(request.getContent())).append("\",");
            
            if (StringUtils.hasText(request.getImages())) {
                contentData.append("\"images\":\"").append(escapeJson(request.getImages())).append("\",");
            }
            
            if (StringUtils.hasText(request.getVideoUrl())) {
                contentData.append("\"videoUrl\":\"").append(escapeJson(request.getVideoUrl())).append("\",");
            }
            
            if (request.getIsFree() != null) {
                contentData.append("\"isFree\":").append(request.getIsFree()).append(",");
            }
            
            if (request.getPrice() != null) {
                contentData.append("\"price\":").append(request.getPrice()).append(",");
            }
            
            if (StringUtils.hasText(request.getShareTargetType())) {
                contentData.append("\"shareTargetType\":\"").append(escapeJson(request.getShareTargetType())).append("\",");
                contentData.append("\"shareTargetId\":").append(request.getShareTargetId()).append(",");
                if (StringUtils.hasText(request.getShareTargetTitle())) {
                    contentData.append("\"shareTargetTitle\":\"").append(escapeJson(request.getShareTargetTitle())).append("\",");
                }
            }
            
            // 移除最后的逗号并关闭JSON
            String result = contentData.toString();
            if (result.endsWith(",")) {
                result = result.substring(0, result.length() - 1);
            }
            result += "}";
            
            return result;
        } catch (Exception e) {
            log.warn("构建内容数据JSON失败: {}", e.getMessage());
            return "{\"type\":\"social_dynamic\",\"content\":\"" + escapeJson(request.getContent()) + "\"}";
        }
    }
    
    /**
     * 提取第一张图片作为封面
     */
    private String extractFirstImage(String images) {
        if (!StringUtils.hasText(images)) {
            return null;
        }
        
        try {
            // 尝试解析JSON数组格式
            if (images.trim().startsWith("[")) {
                images = images.trim().substring(1, images.length() - 1);
                String[] imageArray = images.split(",");
                if (imageArray.length > 0) {
                    return imageArray[0].trim().replace("\"", "");
                }
            } else {
                // 如果是逗号分隔的格式
                String[] imageArray = images.split(",");
                if (imageArray.length > 0) {
                    return imageArray[0].trim();
                }
            }
        } catch (Exception e) {
            log.warn("解析图片URL失败: {}", e.getMessage());
        }
        
        return null;
    }
    
    /**
     * JSON字符串转义
     */
    private String escapeJson(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }

    /**
     * 将请求对象转换为实体对象
     */
    private SocialDynamic convertToEntity(SocialDynamicCreateRequest request) {
        SocialDynamic dynamic = new SocialDynamic();
        BeanUtils.copyProperties(request, dynamic);
        
        // =================== 重要修复：自动填充用户信息 ===================
        // 如果userNickname为空，从User表中获取
        if (!StringUtils.hasText(dynamic.getUserNickname()) && dynamic.getUserId() != null) {
            try {
                User user = userService.getUserById(dynamic.getUserId());
                if (user != null) {
                    dynamic.setUserNickname(user.getNickname());
                    // 如果userAvatar也为空，一并设置
                    if (!StringUtils.hasText(dynamic.getUserAvatar())) {
                        dynamic.setUserAvatar(user.getAvatar());
                    }
                    log.debug("自动填充用户信息: userId={}, nickname={}, avatar={}", 
                            dynamic.getUserId(), dynamic.getUserNickname(), dynamic.getUserAvatar());
                } else {
                    log.warn("创建动态时用户不存在: userId={}", dynamic.getUserId());
                }
            } catch (Exception e) {
                log.error("获取用户信息失败: userId={}, error={}", dynamic.getUserId(), e.getMessage(), e);
            }
        }
        
        // 设置默认值
        if (!StringUtils.hasText(dynamic.getDynamicType())) {
            dynamic.setDynamicType("text");
        }
        if (!StringUtils.hasText(dynamic.getStatus())) {
            dynamic.setStatus("normal");
        }
        if (dynamic.getLikeCount() == null) {
            dynamic.setLikeCount(0L);
        }
        if (dynamic.getCommentCount() == null) {
            dynamic.setCommentCount(0L);
        }
        if (dynamic.getShareCount() == null) {
            dynamic.setShareCount(0L);
        }
        
        return dynamic;
    }

    /**
     * 将实体对象转换为响应对象，包含互动状态
     */
    private SocialDynamicResponse convertToResponseWithInteractionStatus(SocialDynamic dynamic, Long currentUserId) {
        SocialDynamicResponse response = new SocialDynamicResponse();
        
        // 手动设置字段，避免BeanUtils.copyProperties的类型不匹配问题
        response.setId(dynamic.getId());
        response.setContent(dynamic.getContent());
        response.setTitle(dynamic.getTitle());
        response.setDynamicType(dynamic.getDynamicType());
        response.setVideoUrl(dynamic.getVideoUrl());
        response.setIsFree(dynamic.getIsFree());
        response.setPrice(dynamic.getPrice());
        response.setUserId(dynamic.getUserId());
        response.setUserNickname(dynamic.getUserNickname());
        response.setUserAvatar(dynamic.getUserAvatar());
        response.setShareTargetType(dynamic.getShareTargetType());
        response.setShareTargetId(dynamic.getShareTargetId());
        response.setShareTargetTitle(dynamic.getShareTargetTitle());
        response.setLikeCount(dynamic.getLikeCount());
        // 动态查询评论数量
        long commentCount = commentService.countTargetComments(dynamic.getId(), "DYNAMIC");
        response.setCommentCount(commentCount);
        response.setShareCount(dynamic.getShareCount());
        response.setStatus(dynamic.getStatus());
        response.setCreateTime(dynamic.getCreateTime());
        response.setUpdateTime(dynamic.getUpdateTime());
        
        // 处理images字段：将String转换为List<String>
        if (dynamic.getImages() != null && !dynamic.getImages().trim().isEmpty()) {
            try {
                // 如果是JSON数组格式，解析JSON
                if (dynamic.getImages().startsWith("[") && dynamic.getImages().endsWith("]")) {
                    // 简单的JSON数组解析，假设格式为["url1","url2"]
                    String imagesStr = dynamic.getImages().substring(1, dynamic.getImages().length() - 1);
                    if (!imagesStr.trim().isEmpty()) {
                        List<String> imagesList = java.util.Arrays.asList(imagesStr.split(","))
                                .stream()
                                .map(url -> url.trim().replace("\"", ""))
                                .filter(url -> !url.isEmpty())
                                .collect(java.util.stream.Collectors.toList());
                        response.setImages(imagesList);
                    } else {
                        response.setImages(new java.util.ArrayList<>());
                    }
                } else {
                    // 如果是逗号分隔的格式
                    List<String> imagesList = java.util.Arrays.asList(dynamic.getImages().split(","))
                            .stream()
                            .map(url -> url.trim())
                            .filter(url -> !url.isEmpty())
                            .collect(java.util.stream.Collectors.toList());
                    response.setImages(imagesList);
                }
            } catch (Exception e) {
                log.warn("解析images字段失败: images={}", dynamic.getImages(), e);
                response.setImages(new java.util.ArrayList<>());
            }
        } else {
            // 如果images为空或null，返回空数组
            response.setImages(new java.util.ArrayList<>());
        }
        
        // 如果提供了当前用户ID，则获取互动状态
        if (currentUserId != null) {
            // 获取点赞状态
            try {
                log.info("开始获取点赞状态: currentUserId={}, dynamicId={}", currentUserId, dynamic.getId());
                boolean isLiked = likeService.checkLikeStatus(currentUserId, LikeCacheConstant.LIKE_TYPE_DYNAMIC, dynamic.getId());
                log.info("点赞状态获取结果: isLiked={}", isLiked);
                response.setIsLiked(isLiked);
            } catch (Exception e) {
                log.warn("获取点赞状态失败: dynamicId={}, currentUserId={}", dynamic.getId(), currentUserId, e);
                response.setIsLiked(false);
            }
            
            // 获取关注状态（当前用户是否关注了动态作者）
            try {
                if (dynamic.getUserId() != null && !dynamic.getUserId().equals(currentUserId)) {
                    boolean isFollowed = followService.checkFollowStatus(currentUserId, dynamic.getUserId());
                    response.setIsFollowed(isFollowed);
                } else {
                    // 如果是自己的动态，设置为false（不能关注自己）
                    response.setIsFollowed(false);
                }
            } catch (Exception e) {
                log.warn("获取关注状态失败: dynamicId={}, authorId={}, currentUserId={}", 
                        dynamic.getId(), dynamic.getUserId(), currentUserId, e);
                response.setIsFollowed(false);
            }
        } else {
            // 如果没有提供当前用户ID（未登录状态），则设置为false
            response.setIsLiked(false);
            response.setIsFollowed(false);
        }
        
        return response;
    }

    /**
     * 将分页结果转换为响应对象，包含互动状态
     */
    private PageResponse<SocialDynamicResponse> convertToPageResponseWithInteractionStatus(IPage<SocialDynamic> page, Long currentUserId) {
        List<SocialDynamicResponse> records = page.getRecords().stream()
                .map(dynamic -> convertToResponseWithInteractionStatus(dynamic, currentUserId))
                .collect(Collectors.toList());
        
        PageResponse<SocialDynamicResponse> response = new PageResponse<>();
        response.setRecords(records);
        response.setTotal(page.getTotal());
        response.setCurrentPage((int) page.getCurrent());
        response.setPageSize((int) page.getSize());
        response.setTotalPages((int) page.getPages());
        
        return response;
    }

    /**
     * 将实体对象转换为响应对象（保持原有方法兼容性）
     */
    private SocialDynamicResponse convertToResponse(SocialDynamic dynamic) {
        return convertToResponseWithInteractionStatus(dynamic, null);
    }

    /**
     * 将分页结果转换为响应对象（保持原有方法兼容性）
     */
    private PageResponse<SocialDynamicResponse> convertToPageResponse(IPage<SocialDynamic> page) {
        return convertToPageResponseWithInteractionStatus(page, null);
    }
}
