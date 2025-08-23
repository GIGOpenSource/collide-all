package com.gig.collide.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gig.collide.Apientry.api.common.response.PageResponse;
import com.gig.collide.Apientry.api.common.response.Result;
import com.gig.collide.Apientry.api.follow.request.FollowCreateRequest;
import com.gig.collide.Apientry.api.follow.request.FollowUnfollowRequest;
import com.gig.collide.domain.Follow;
import com.gig.collide.domain.User;
import com.gig.collide.service.FollowService;
import com.gig.collide.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 关注管理控制器
 * 提供完整的关注功能HTTP接口
 * 
 * 功能模块：
 * - 基础操作：关注/取消关注/检查状态/获取详情
 * - 列表查询：关注列表/粉丝列表/互关列表/分页查询
 * - 搜索功能：昵称搜索/关系链查询/批量检查
 * - 统计功能：关注数/粉丝数/关注统计/活跃度检测
 * - 管理功能：用户信息同步/数据清理/关系激活/参数验证
 * - 特殊检测：双向关系/批量状态/活跃度分析
 * 
 * @author GIG Team
 * @version 1.0.0
 * @since 2025-01-27
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/follow")
@RequiredArgsConstructor
@Validated
@Tag(name = "关注管理", description = "关注相关的API接口")
public class FollowController {

    private final FollowService followService;
    private final UserService userService;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 关注用户
     */
    @PostMapping("/create")
    @Operation(summary = "关注用户", description = "创建关注关系")
    public Result<FollowResponse> followUser(
            @Parameter(description = "关注者ID（下划线格式）", required = false) @RequestParam(value = "follower_id", required = false) Long followerIdUnderscore,
            @Parameter(description = "关注者ID（驼峰格式）", required = false) @RequestParam(value = "followerId", required = false) Long followerIdCamel,
            @Parameter(description = "被关注者ID（下划线格式）", required = false) @RequestParam(value = "followee_id", required = false) Long followeeIdUnderscore,
            @Parameter(description = "被关注者ID（驼峰格式）", required = false) @RequestParam(value = "followeeId", required = false) Long followeeIdCamel,
            @Parameter(description = "被关注者ID（兼容参数）", required = false) @RequestParam(value = "followedId", required = false) Long followedId,
            @Parameter(description = "关注请求对象") @RequestBody(required = false) FollowCreateRequest request) {
        
        // =================== 完整的参数兼容性处理 ===================
        
        // 1. 处理关注者ID的多种格式兼容
        Long followerId = null;
        if (followerIdUnderscore != null) {
            followerId = followerIdUnderscore;
            log.debug("使用下划线格式关注者ID: follower_id={}", followerIdUnderscore);
        } else if (followerIdCamel != null) {
            followerId = followerIdCamel;
            log.debug("使用驼峰格式关注者ID: followerId={}", followerIdCamel);
        }
        
        // 2. 处理被关注者ID的多种格式兼容
        Long followeeId = null;
        if (followeeIdUnderscore != null) {
            followeeId = followeeIdUnderscore;
            log.debug("使用下划线格式被关注者ID: followee_id={}", followeeIdUnderscore);
        } else if (followeeIdCamel != null) {
            followeeId = followeeIdCamel;
            log.debug("使用驼峰格式被关注者ID: followeeId={}", followeeIdCamel);
        } else if (followedId != null) {
            followeeId = followedId;
            log.debug("使用兼容参数被关注者ID: followedId={}", followedId);
        }
        
        // 3. 请求体参数优先级最高，如果存在则覆盖查询参数
        if (request != null) {
            if (request.getFollowerId() != null) {
                followerId = request.getFollowerId();
                log.debug("使用请求体关注者ID: followerId={}", followerId);
            }
            
            // 处理被关注者ID的请求体兼容性
            if (request.getFolloweeId() != null) {
                followeeId = request.getFolloweeId();
                log.debug("使用请求体被关注者ID: followeeId={}", followeeId);
            } else if (request.getFollowedId() != null) {
                followeeId = request.getFollowedId();
                log.debug("使用请求体兼容参数被关注者ID: followedId={}", followeeId);
            }
        }
        
        // 4. 参数处理结果日志
        log.debug("参数处理结果: followerId={}, followeeId={}", followerId, followeeId);
        
        try {
            log.info("REST请求 - 关注用户: followerId={}, followeeId={}", followerId, followeeId);
            
            // 参数验证
            if (followerId == null || followeeId == null) {
                return Result.error("关注者ID(follower_id/followerId)和被关注者ID(followee_id/followeeId/followedId)不能为空");
            }
            
            if (followerId.equals(followeeId)) {
                return Result.error("不能关注自己");
            }
            
            // 创建关注对象并填充用户信息
            Follow follow = new Follow();
            follow.setFollowerId(followerId);
            follow.setFolloweeId(followeeId);
            
            // 查询并填充关注者信息
            try {
                User follower = userService.getUserById(followerId);
                if (follower != null) {
                    follow.setFollowerNickname(follower.getNickname());
                    follow.setFollowerAvatar(follower.getAvatar());
                    log.debug("关注者信息填充成功: followerId={}, nickname={}", followerId, follower.getNickname());
                } else {
                    log.warn("关注者不存在: followerId={}", followerId);
                }
            } catch (Exception e) {
                log.warn("查询关注者信息失败: followerId={}, error={}", followerId, e.getMessage());
                // 继续执行，不阻断关注流程
            }
            
            // 查询并填充被关注者信息
            try {
                User followee = userService.getUserById(followeeId);
                if (followee != null) {
                    follow.setFolloweeNickname(followee.getNickname());
                    follow.setFolloweeAvatar(followee.getAvatar());
                    log.debug("被关注者信息填充成功: followeeId={}, nickname={}", followeeId, followee.getNickname());
                } else {
                    log.warn("被关注者不存在: followeeId={}", followeeId);
                }
            } catch (Exception e) {
                log.warn("查询被关注者信息失败: followeeId={}, error={}", followeeId, e.getMessage());
                // 继续执行，不阻断关注流程
            }
            
            // 调用服务层
            Follow createdFollow = followService.followUser(follow);
            
            // 转换为响应对象
            FollowResponse response = convertToResponse(createdFollow);
            return Result.success(response);
            
        } catch (Exception e) {
            log.error("关注用户失败: followerId={}, followeeId={}", followerId, followeeId, e);
            return Result.error("关注用户失败: " + e.getMessage());
        }
    }

    /**
     * 取消关注
     */
    @PostMapping("/unfollow")
    @Operation(summary = "取消关注", description = "取消关注关系")
    public Result<Void> unfollowUser(
            @Parameter(description = "关注者ID（下划线格式）", required = false) @RequestParam(value = "follower_id", required = false) Long followerIdUnderscore,
            @Parameter(description = "关注者ID（驼峰格式）", required = false) @RequestParam(value = "followerId", required = false) Long followerIdCamel,
            @Parameter(description = "被关注者ID（下划线格式）", required = false) @RequestParam(value = "followee_id", required = false) Long followeeIdUnderscore,
            @Parameter(description = "被关注者ID（驼峰格式）", required = false) @RequestParam(value = "followeeId", required = false) Long followeeIdCamel,
            @Parameter(description = "被关注者ID（兼容参数）", required = false) @RequestParam(value = "followedId", required = false) Long followedId,
            @Parameter(description = "取消关注请求对象") @RequestBody(required = false) FollowUnfollowRequest request) {
        
        // =================== 完整的参数兼容性处理 ===================
        
        // 1. 处理关注者ID的多种格式兼容
        Long followerId = null;
        if (followerIdUnderscore != null) {
            followerId = followerIdUnderscore;
            log.debug("使用下划线格式关注者ID: follower_id={}", followerIdUnderscore);
        } else if (followerIdCamel != null) {
            followerId = followerIdCamel;
            log.debug("使用驼峰格式关注者ID: followerId={}", followerIdCamel);
        }
        
        // 2. 处理被关注者ID的多种格式兼容
        Long followeeId = null;
        if (followeeIdUnderscore != null) {
            followeeId = followeeIdUnderscore;
            log.debug("使用下划线格式被关注者ID: followee_id={}", followeeIdUnderscore);
        } else if (followeeIdCamel != null) {
            followeeId = followeeIdCamel;
            log.debug("使用驼峰格式被关注者ID: followeeId={}", followeeIdCamel);
        } else if (followedId != null) {
            followeeId = followedId;
            log.debug("使用兼容参数被关注者ID: followedId={}", followedId);
        }
        
        // 3. 请求体参数优先级最高，如果存在则覆盖查询参数
        if (request != null) {
            if (request.getFollowerId() != null) {
                followerId = request.getFollowerId();
                log.debug("使用请求体关注者ID: followerId={}", followerId);
            }
            // 处理被关注者ID的请求体兼容性
            if (request.getFolloweeId() != null) {
                followeeId = request.getFolloweeId();
                log.debug("使用请求体被关注者ID: followeeId={}", followeeId);
            } else if (request.getFollowedId() != null) {
                followeeId = request.getFollowedId();
                log.debug("使用请求体兼容参数被关注者ID: followedId={}", followeeId);
            }
        }
        
        // 4. 参数处理结果日志
        log.debug("参数处理结果: followerId={}, followeeId={}", followerId, followeeId);
        
        try {
            log.info("REST请求 - 取消关注: followerId={}, followeeId={}", followerId, followeeId);
            
            // 参数验证
            if (followerId == null || followeeId == null) {
                return Result.error("关注者ID(follower_id/followerId)和被关注者ID(followee_id/followeeId/followedId)不能为空");
            }
            
            // 调用服务层
            boolean success = followService.unfollowUser(followerId, followeeId, "用户主动取消", followerId);
            if (success) {
                return Result.success();
            } else {
                return Result.error("取消关注失败");
            }
            
        } catch (Exception e) {
            log.error("取消关注失败: followerId={}, followeeId={}", followerId, followeeId, e);
            return Result.error("取消关注失败: " + e.getMessage());
        }
    }

    /**
     * 检查关注状态
     */
    @GetMapping("/check")
    @Operation(summary = "检查关注状态", description = "查询用户是否已关注目标用户")
    public Result<Boolean> checkFollowStatus(
            @Parameter(description = "关注者ID（下划线格式）", required = false) @RequestParam(value = "follower_id", required = false) Long followerIdUnderscore,
            @Parameter(description = "关注者ID（驼峰格式）", required = false) @RequestParam(value = "followerId", required = false) Long followerIdCamel,
            @Parameter(description = "被关注者ID（下划线格式）", required = false) @RequestParam(value = "followee_id", required = false) Long followeeIdUnderscore,
            @Parameter(description = "被关注者ID（驼峰格式）", required = false) @RequestParam(value = "followeeId", required = false) Long followeeIdCamel,
            @Parameter(description = "被关注者ID（兼容参数）", required = false) @RequestParam(value = "followedId", required = false) Long followedId) {
        
        // =================== 完整的参数兼容性处理 ===================
        
        // 1. 处理关注者ID的多种格式兼容
        Long followerId = null;
        if (followerIdUnderscore != null) {
            followerId = followerIdUnderscore;
            log.debug("使用下划线格式关注者ID: follower_id={}", followerIdUnderscore);
        } else if (followerIdCamel != null) {
            followerId = followerIdCamel;
            log.debug("使用驼峰格式关注者ID: followerId={}", followerIdCamel);
        }
        
        // 2. 处理被关注者ID的多种格式兼容
        Long followeeId = null;
        if (followeeIdUnderscore != null) {
            followeeId = followeeIdUnderscore;
            log.debug("使用下划线格式被关注者ID: followee_id={}", followeeIdUnderscore);
        } else if (followeeIdCamel != null) {
            followeeId = followeeIdCamel;
            log.debug("使用驼峰格式被关注者ID: followeeId={}", followeeIdCamel);
        } else if (followedId != null) {
            followeeId = followedId;
            log.debug("使用兼容参数被关注者ID: followedId={}", followedId);
        }
        
        // 3. 参数处理结果日志
        log.debug("参数处理结果: followerId={}, followeeId={}", followerId, followeeId);
        
        try {
            log.info("REST请求 - 检查关注状态: followerId={}, followeeId={}", followerId, followeeId);
            
            // 参数验证
            if (followerId == null || followeeId == null) {
                return Result.error("关注者ID(follower_id/followerId)和被关注者ID(followee_id/followeeId/followedId)不能为空");
            }
            
            // 调用服务层
            boolean isFollowing = followService.checkFollowStatus(followerId, followeeId);
            return Result.success(isFollowing);
            
        } catch (Exception e) {
            log.error("检查关注状态失败: followerId={}, followeeId={}", followerId, followeeId, e);
            return Result.error("检查关注状态失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户的关注列表
     */
    @GetMapping("/following/{userId}")
    @Operation(summary = "获取用户的关注列表", description = "查询某用户关注的所有人")
    public Result<PageResponse<FollowResponse>> getFollowing(
            @Parameter(description = "用户ID", required = true) @PathVariable Long userId,
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer currentPage,
            @Parameter(description = "页面大小") @RequestParam(defaultValue = "20") Integer pageSize) {
        
        try {
            log.info("REST请求 - 获取关注列表: userId={}, page={}/{}", userId, currentPage, pageSize);
            
            // 参数验证
            if (userId == null) {
                return Result.error("用户ID不能为空");
            }
            
            // 调用服务层
            IPage<Follow> followPage = followService.getFollowing(userId, currentPage, pageSize);
            
            // 转换为响应对象
            PageResponse<FollowResponse> response = convertToPageResponse(followPage);
            return Result.success(response);
            
        } catch (Exception e) {
            log.error("获取关注列表失败: userId={}", userId, e);
            return Result.error("获取关注列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户的粉丝列表
     */
    @GetMapping("/followers/{userId}")
    @Operation(summary = "获取用户的粉丝列表", description = "查询关注某用户的所有人")
    public Result<PageResponse<FollowResponse>> getFollowers(
            @Parameter(description = "用户ID", required = true) @PathVariable Long userId,
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer currentPage,
            @Parameter(description = "页面大小") @RequestParam(defaultValue = "20") Integer pageSize) {
        
        try {
            log.info("REST请求 - 获取粉丝列表: userId={}, page={}/{}", userId, currentPage, pageSize);
            
            // 参数验证
            if (userId == null) {
                return Result.error("用户ID不能为空");
            }
            
            // 调用服务层
            IPage<Follow> followPage = followService.getFollowers(userId, currentPage, pageSize);
            
            // 转换为响应对象
            PageResponse<FollowResponse> response = convertToPageResponse(followPage);
            return Result.success(response);
            
        } catch (Exception e) {
            log.error("获取粉丝列表失败: userId={}", userId, e);
            return Result.error("获取粉丝列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户关注数量
     */
    @GetMapping("/following/count/{userId}")
    @Operation(summary = "获取用户关注数量", description = "统计用户关注的人数")
    public Result<Long> getFollowingCount(
            @Parameter(description = "用户ID", required = true) @PathVariable Long userId) {
        
        try {
            log.info("REST请求 - 获取关注数量: userId={}", userId);
            
            // 参数验证
            if (userId == null) {
                return Result.error("用户ID不能为空");
            }
            
            // 调用服务层
            Long count = followService.getFollowingCount(userId);
            return Result.success(count);
            
        } catch (Exception e) {
            log.error("获取关注数量失败: userId={}", userId, e);
            return Result.error("获取关注数量失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户粉丝数量
     */
    @GetMapping("/followers/count/{userId}")
    @Operation(summary = "获取用户粉丝数量", description = "统计关注某用户的人数")
    public Result<Long> getFollowersCount(
            @Parameter(description = "用户ID", required = true) @PathVariable Long userId) {
        
        try {
            log.info("REST请求 - 获取粉丝数量: userId={}", userId);
            
            // 参数验证
            if (userId == null) {
                return Result.error("用户ID不能为空");
            }
            
            // 调用服务层
            Long count = followService.getFollowersCount(userId);
            return Result.success(count);
            
        } catch (Exception e) {
            log.error("获取粉丝数量失败: userId={}", userId, e);
            return Result.error("获取粉丝数量失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户关注统计信息
     */
    @GetMapping("/statistics/{userId}")
    @Operation(summary = "获取用户关注统计信息", description = "包含关注数和粉丝数")
    public Result<FollowStatisticsResponse> getFollowStatistics(
            @Parameter(description = "用户ID", required = true) @PathVariable Long userId) {
        
        try {
            log.info("REST请求 - 获取关注统计: userId={}", userId);
            
            // 参数验证
            if (userId == null) {
                return Result.error("用户ID不能为空");
            }
            
            // 调用服务层
            Long followingCount = followService.getFollowingCount(userId);
            Long followersCount = followService.getFollowersCount(userId);
            
            FollowStatisticsResponse response = new FollowStatisticsResponse();
            response.setUserId(userId);
            response.setFollowingCount(followingCount);
            response.setFollowersCount(followersCount);
            
            return Result.success(response);
            
        } catch (Exception e) {
            log.error("获取关注统计失败: userId={}", userId, e);
            return Result.error("获取关注统计失败: " + e.getMessage());
        }
    }

    /**
     * 批量检查关注状态
     */
    @PostMapping("/batch-check")
    @Operation(summary = "批量检查关注状态", description = "检查用户对多个目标用户的关注状态")
    public Result<Map<Long, Boolean>> batchCheckFollowStatus(
            @Parameter(description = "关注者ID", required = true) @RequestParam(value = "follower_id") Long followerId,
            @Parameter(description = "被关注者ID列表", required = true) @RequestBody List<Long> followedIds) {
        
        try {
            log.info("REST请求 - 批量检查关注状态: followerId={}, followedIds={}", followerId, followedIds);
            
            // 参数验证
            if (followerId == null) {
                return Result.error("关注者ID不能为空");
            }
            if (followedIds == null || followedIds.isEmpty()) {
                return Result.error("被关注者ID列表不能为空");
            }
            
            // 调用服务层
            Map<Long, Boolean> result = followService.batchCheckFollowStatus(followerId, followedIds);
            return Result.success(result);
            
        } catch (Exception e) {
            log.error("批量检查关注状态失败: followerId={}", followerId, e);
            return Result.error("批量检查关注状态失败: " + e.getMessage());
        }
    }

    /**
     * 获取互相关注的好友
     */
    @GetMapping("/mutual/{userId}")
    @Operation(summary = "获取互相关注的好友", description = "查询双向关注关系")
    public Result<PageResponse<FollowResponse>> getMutualFollows(
            @Parameter(description = "用户ID", required = true) @PathVariable Long userId,
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer currentPage,
            @Parameter(description = "页面大小") @RequestParam(defaultValue = "20") Integer pageSize) {
        
        try {
            log.info("REST请求 - 获取互关好友: userId={}, page={}/{}", userId, currentPage, pageSize);
            
            // 参数验证
            if (userId == null) {
                return Result.error("用户ID不能为空");
            }
            
            // 调用服务层
            IPage<Follow> followPage = followService.getMutualFollows(userId, currentPage, pageSize);
            
            // 转换为响应对象
            PageResponse<FollowResponse> response = convertToPageResponse(followPage);
            return Result.success(response);
            
        } catch (Exception e) {
            log.error("获取互关好友失败: userId={}", userId, e);
            return Result.error("获取互关好友失败: " + e.getMessage());
        }
    }

    /**
     * 根据昵称搜索关注关系
     */
    @GetMapping("/search")
    @Operation(summary = "根据昵称搜索关注关系", description = "根据关注者或被关注者昵称进行模糊搜索")
    public Result<PageResponse<FollowResponse>> searchByNickname(
            @Parameter(description = "关注者ID") @RequestParam(value = "follower_id", required = false) Long followerIdUnderscore,
            @Parameter(description = "关注者ID（驼峰格式）") @RequestParam(value = "followerId", required = false) Long followerIdCamel,
            @Parameter(description = "被关注者ID") @RequestParam(value = "followee_id", required = false) Long followeeId,
            @Parameter(description = "被关注者ID（兼容参数）", required = false) @RequestParam(value = "followedId", required = false) Long followedId,
            @Parameter(description = "昵称关键词", required = true) @RequestParam String nicknameKeyword,
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer currentPage,
            @Parameter(description = "页面大小") @RequestParam(defaultValue = "20") Integer pageSize) {
        
        // 参数兼容性处理：支持下划线和驼峰两种格式
        Long followerId = followerIdUnderscore != null ? followerIdUnderscore : followerIdCamel;
        
        // 兼容处理：如果传入的是followedId参数，则使用它作为followeeId
        if (followeeId == null && followedId != null) {
            followeeId = followedId;
            log.info("使用兼容参数: followedId={} 作为 followeeId", followedId);
        }
        
        try {
            log.info("REST请求 - 昵称搜索: keyword={}, page={}/{}", nicknameKeyword, currentPage, pageSize);
            
            // 参数验证
            if (nicknameKeyword == null || nicknameKeyword.trim().isEmpty()) {
                return Result.error("昵称关键词不能为空");
            }
            
            // 调用服务层
            IPage<Follow> followPage = followService.searchByNickname(
                    followerId, followeeId, nicknameKeyword.trim(), currentPage, pageSize);
            
            // 转换为响应对象
            PageResponse<FollowResponse> response = convertToPageResponse(followPage);
            return Result.success(response);
            
        } catch (Exception e) {
            log.error("昵称搜索失败: keyword={}", nicknameKeyword, e);
            return Result.error("昵称搜索失败: " + e.getMessage());
        }
    }

    /**
     * 更新用户信息（冗余字段同步）
     */
    @PutMapping("/user/info")
    @Operation(summary = "更新用户信息", description = "当用户信息变更时，同步更新关注表中的冗余信息")
    public Result<Integer> updateUserInfo(
            @Parameter(description = "用户ID", required = true) @RequestParam Long userId,
            @Parameter(description = "新昵称", required = true) @RequestParam String nickname,
            @Parameter(description = "新头像") @RequestParam(required = false) String avatar) {
        
        try {
            log.info("REST请求 - 更新用户信息: userId={}, nickname={}", userId, nickname);
            
            // 参数验证
            if (userId == null) {
                return Result.error("用户ID不能为空");
            }
            if (nickname == null || nickname.trim().isEmpty()) {
                return Result.error("昵称不能为空");
            }
            
            // 调用服务层
            Integer updatedCount = followService.updateUserInfo(userId, nickname.trim(), avatar);
            return Result.success(updatedCount);
            
        } catch (Exception e) {
            log.error("更新用户信息失败: userId={}", userId, e);
            return Result.error("更新用户信息失败: " + e.getMessage());
        }
    }

    /**
     * 重新激活已取消的关注关系
     */
    @PostMapping("/reactivate")
    @Operation(summary = "重新激活关注关系", description = "将cancelled状态的关注重新设置为active")
    public Result<Boolean> reactivateFollow(
            @Parameter(description = "关注者ID（下划线格式）", required = false) @RequestParam(value = "follower_id", required = false) Long followerIdUnderscore,
            @Parameter(description = "关注者ID（驼峰格式）", required = false) @RequestParam(value = "followerId", required = false) Long followerIdCamel,
            @Parameter(description = "被关注者ID（下划线格式）", required = false) @RequestParam(value = "followee_id", required = false) Long followeeIdUnderscore,
            @Parameter(description = "被关注者ID（驼峰格式）", required = false) @RequestParam(value = "followeeId", required = false) Long followeeIdCamel,
            @Parameter(description = "被关注者ID（兼容参数）", required = false) @RequestParam(value = "followedId", required = false) Long followedId) {
        
        // =================== 完整的参数兼容性处理 ===================
        
        // 1. 处理关注者ID的多种格式兼容
        Long followerId = null;
        if (followerIdUnderscore != null) {
            followerId = followerIdUnderscore;
            log.debug("使用下划线格式关注者ID: follower_id={}", followerIdUnderscore);
        } else if (followerIdCamel != null) {
            followerId = followerIdCamel;
            log.debug("使用驼峰格式关注者ID: followerId={}", followerIdCamel);
        }
        
        // 2. 处理被关注者ID的多种格式兼容
        Long followeeId = null;
        if (followeeIdUnderscore != null) {
            followeeId = followeeIdUnderscore;
            log.debug("使用下划线格式被关注者ID: followee_id={}", followeeIdUnderscore);
        } else if (followeeIdCamel != null) {
            followeeId = followeeIdCamel;
            log.debug("使用驼峰格式被关注者ID: followeeId={}", followeeIdCamel);
        } else if (followedId != null) {
            followeeId = followedId;
            log.debug("使用兼容参数被关注者ID: followedId={}", followedId);
        }
        
        // 3. 参数处理结果日志
        log.debug("参数处理结果: followerId={}, followeeId={}", followerId, followeeId);
        
        try {
            log.info("REST请求 - 重新激活关注: followerId={}, followeeId={}", followerId, followeeId);
            
            // 参数验证
            if (followerId == null || followeeId == null) {
                return Result.error("关注者ID(follower_id/followerId)和被关注者ID(followee_id/followeeId/followedId)不能为空");
            }

            // 调用服务层
            boolean success = followService.reactivateFollow(followerId, followeeId);
            return Result.success(success);
            
        } catch (Exception e) {
            log.error("重新激活关注失败: followerId={}, followeeId={}", followerId, followeeId, e);
            return Result.error("重新激活关注失败: " + e.getMessage());
        }
    }

    /**
     * 清理已取消的关注记录
     */
    @DeleteMapping("/cleanup")
    @Operation(summary = "清理已取消的关注记录", description = "物理删除cancelled状态的记录")
    public Result<Integer> cleanCancelledFollows(
            @Parameter(description = "保留天数") @RequestParam(defaultValue = "30") Integer days) {
        
        try {
            log.info("REST请求 - 清理已取消关注: days={}", days);
            
            // 参数验证
            if (days == null || days < 0) {
                days = 30;
            }
            
            // 调用服务层
            Integer deletedCount = followService.cleanCancelledFollows(days);
            return Result.success(deletedCount);
            
        } catch (Exception e) {
            log.error("清理已取消关注失败: days={}", days, e);
            return Result.error("清理已取消关注失败: " + e.getMessage());
        }
    }

    /**
     * 将Follow实体转换为FollowResponse
     */
    private FollowResponse convertToResponse(Follow follow) {
        if (follow == null) {
            return null;
        }

        FollowResponse response = new FollowResponse();
        response.setId(follow.getId());
        response.setFollowerId(follow.getFollowerId());
        response.setFolloweeId(follow.getFolloweeId());
        response.setFollowerNickname(follow.getFollowerNickname());
        response.setFollowerAvatar(follow.getFollowerAvatar());
        response.setFolloweeNickname(follow.getFolloweeNickname());
        response.setFolloweeAvatar(follow.getFolloweeAvatar());
        response.setStatus(follow.getStatus());
        response.setFollowType("normal"); // 默认类型
        response.setIsMutual(false); // 需要额外查询互关状态
        response.setCreateTime(follow.getCreateTime() != null ? 
                follow.getCreateTime().format(DATE_FORMATTER) : null);
        response.setUpdateTime(follow.getUpdateTime() != null ? 
                follow.getUpdateTime().format(DATE_FORMATTER) : null);

        return response;
    }

    /**
     * 将IPage<Follow>转换为PageResponse<FollowResponse>
     */
    private PageResponse<FollowResponse> convertToPageResponse(IPage<Follow> followPage) {
        PageResponse<FollowResponse> response = new PageResponse<>();
        
        List<FollowResponse> records = followPage.getRecords().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        
        response.setRecords(records);
        response.setTotal(followPage.getTotal());
        response.setCurrentPage((int) followPage.getCurrent());
        response.setPageSize((int) followPage.getSize());
        response.setTotalPages((int) followPage.getPages());
        
        return response;
    }

    /**
     * 关注关系响应对象
     */
    public static class FollowResponse {
        private Long id;
        private Long followerId;
        private String followerNickname;
        private String followerAvatar;
        private Long followeeId;
        private String followeeNickname;
        private String followeeAvatar;
        private String status;
        private String followType;
        private Boolean isMutual;
        private String createTime;
        private String updateTime;
        
        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public Long getFollowerId() { return followerId; }
        public void setFollowerId(Long followerId) { this.followerId = followerId; }
        
        public String getFollowerNickname() { return followerNickname; }
        public void setFollowerNickname(String followerNickname) { this.followerNickname = followerNickname; }
        
        public String getFollowerAvatar() { return followerAvatar; }
        public void setFollowerAvatar(String followerAvatar) { this.followerAvatar = followerAvatar; }
        
        public Long getFolloweeId() { return followeeId; }
        public void setFolloweeId(Long followeeId) { this.followeeId = followeeId; }
        
        public String getFolloweeNickname() { return followeeNickname; }
        public void setFolloweeNickname(String followeeNickname) { this.followeeNickname = followeeNickname; }
        
        public String getFolloweeAvatar() { return followeeAvatar; }
        public void setFolloweeAvatar(String followeeAvatar) { this.followeeAvatar = followeeAvatar; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public String getFollowType() { return followType; }
        public void setFollowType(String followType) { this.followType = followType; }
        
        public Boolean getIsMutual() { return isMutual; }
        public void setIsMutual(Boolean isMutual) { this.isMutual = isMutual; }
        
        public String getCreateTime() { return createTime; }
        public void setCreateTime(String createTime) { this.createTime = createTime; }
        
        public String getUpdateTime() { return updateTime; }
        public void setUpdateTime(String updateTime) { this.updateTime = updateTime; }
    }

    /**
     * 关注统计响应对象
     */
    public static class FollowStatisticsResponse {
        private Long userId;
        private Long followingCount;
        private Long followersCount;
        
        // Getters and Setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        public Long getFollowingCount() { return followingCount; }
        public void setFollowingCount(Long followingCount) { this.followingCount = followingCount; }
        
        public Long getFollowersCount() { return followersCount; }
        public void setFollowersCount(Long followersCount) { this.followersCount = followersCount; }
    }
}