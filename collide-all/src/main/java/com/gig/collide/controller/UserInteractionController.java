package com.gig.collide.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gig.collide.Apientry.api.common.response.Result;
import com.gig.collide.domain.Like;
import com.gig.collide.domain.Favorite;
import com.gig.collide.domain.Follow;
import com.gig.collide.service.LikeService;
import com.gig.collide.service.FavoriteService;
import com.gig.collide.service.FollowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户互动管理控制器
 * 提供用户的点赞、收藏、关注等互动信息查询接口
 * 
 * @author GIG Team
 * @version 1.0.0
 * @since 2024-08-22
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/user-interactions")
@RequiredArgsConstructor
@Tag(name = "用户互动管理", description = "用户互动相关接口")
public class UserInteractionController {

    private final LikeService likeService;
    private final FavoriteService favoriteService;
    private final FollowService followService;

    /**
     * 获取用户所有互动信息
     */
    @GetMapping("/all")
    @Operation(summary = "获取用户所有互动", description = "获取用户的点赞、收藏、关注等互动信息")
    public Result<Map<String, Object>> getAllUserInteractions(
            @Parameter(description = "用户ID") @RequestParam Long userId,
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer currentPage,
            @Parameter(description = "页面大小") @RequestParam(defaultValue = "20") Integer pageSize) {
        
        try {
            log.info("REST请求 - 获取用户所有互动: userId={}, page={}/{}", userId, currentPage, pageSize);
            
            Map<String, Object> result = new HashMap<>();
            
            // 获取用户点赞列表
            try {
                IPage<Like> likePage = likeService.findUserLikes(currentPage, pageSize, userId, null, "active");
                result.put("likes", likePage);
                log.debug("用户点赞数据获取成功: 总数={}", likePage.getTotal());
            } catch (Exception e) {
                log.warn("获取用户点赞数据失败: userId={}", userId, e);
                result.put("likes", null);
            }
            
            // 获取用户收藏列表  
            try {
                IPage<Favorite> favoritePage = favoriteService.getUserFavorites(userId, null, currentPage, pageSize);
                result.put("favorites", favoritePage);
                log.debug("用户收藏数据获取成功: 总数={}", favoritePage.getTotal());
            } catch (Exception e) {
                log.warn("获取用户收藏数据失败: userId={}", userId, e);
                result.put("favorites", null);
            }
            
            // 获取用户关注列表
            try {
                // 注意：这里假设有getUserFollows方法，如果没有需要使用其他查询方法
                Map<String, Object> followData = getFollowData(userId, currentPage, pageSize);
                result.put("follows", followData);
                log.debug("用户关注数据获取成功");
            } catch (Exception e) {
                log.warn("获取用户关注数据失败: userId={}", userId, e);
                result.put("follows", null);
            }
            
            // 统计信息
            Map<String, Long> statistics = new HashMap<>();
            try {
                statistics.put("totalLikes", likeService.countUserLikes(userId, null));
            } catch (Exception e) {
                log.warn("获取用户点赞统计失败: userId={}", userId, e);
                statistics.put("totalLikes", 0L);
            }
            
            try {
                statistics.put("totalFavorites", favoriteService.getUserFavoriteCount(userId, null));
            } catch (Exception e) {
                log.warn("获取用户收藏统计失败: userId={}", userId, e);
                statistics.put("totalFavorites", 0L);
            }
            
            try {
                // 获取用户关注数（用户关注了多少人）
                Long followingCount = followService.getFollowingCount(userId);
                statistics.put("totalFollowing", followingCount);
                
                // 获取用户粉丝数（多少人关注了用户）
                Long followersCount = followService.getFollowersCount(userId);
                statistics.put("totalFollowers", followersCount);
            } catch (Exception e) {
                log.warn("获取用户关注统计失败: userId={}", userId, e);
                statistics.put("totalFollowing", 0L);
                statistics.put("totalFollowers", 0L);
            }
            
            result.put("statistics", statistics);
            
            log.info("用户互动信息获取成功: userId={}, 统计={}", userId, statistics);
            return Result.success(result);
            
        } catch (Exception e) {
            log.error("获取用户互动信息失败: userId={}", userId, e);
            return Result.error("获取用户互动信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户点赞统计
     */
    @GetMapping("/likes/statistics")
    @Operation(summary = "获取用户点赞统计", description = "获取用户的点赞统计信息")
    public Result<Map<String, Object>> getLikeStatistics(
            @Parameter(description = "用户ID") @RequestParam Long userId) {
        
        try {
            log.info("REST请求 - 获取用户点赞统计: userId={}", userId);
            
            Map<String, Object> statistics = new HashMap<>();
            
            // 总点赞数
            Long totalLikes = likeService.countUserLikes(userId, null);
            statistics.put("totalLikes", totalLikes);
            
            // 各类型点赞数
            statistics.put("contentLikes", likeService.countUserLikes(userId, "CONTENT"));
            statistics.put("commentLikes", likeService.countUserLikes(userId, "COMMENT"));
            statistics.put("dynamicLikes", likeService.countUserLikes(userId, "DYNAMIC"));
            
            log.info("用户点赞统计获取成功: userId={}, totalLikes={}", userId, totalLikes);
            return Result.success(statistics);
            
        } catch (Exception e) {
            log.error("获取用户点赞统计失败: userId={}", userId, e);
            return Result.error("获取用户点赞统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户收藏统计
     */
    @GetMapping("/favorites/statistics")
    @Operation(summary = "获取用户收藏统计", description = "获取用户的收藏统计信息")
    public Result<Map<String, Object>> getFavoriteStatistics(
            @Parameter(description = "用户ID") @RequestParam Long userId) {
        
        try {
            log.info("REST请求 - 获取用户收藏统计: userId={}", userId);
            
            Map<String, Object> statistics = favoriteService.getUserFavoriteStatistics(userId);
            
            log.info("用户收藏统计获取成功: userId={}", userId);
            return Result.success(statistics);
            
        } catch (Exception e) {
            log.error("获取用户收藏统计失败: userId={}", userId, e);
            return Result.error("获取用户收藏统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取关注数据的辅助方法
     */
    private Map<String, Object> getFollowData(Long userId, Integer currentPage, Integer pageSize) {
        Map<String, Object> followData = new HashMap<>();
        
        try {
            // 获取用户的关注列表（用户关注了哪些人）
            IPage<Follow> followingPage = followService.getFollowing(userId, currentPage, pageSize);
            followData.put("following", followingPage);
            log.debug("用户关注列表获取成功: 总数={}", followingPage.getTotal());
        } catch (Exception e) {
            log.warn("获取用户关注列表失败: userId={}", userId, e);
            followData.put("following", null);
        }
        
        try {
            // 获取用户的粉丝列表（哪些人关注了用户）
            IPage<Follow> followersPage = followService.getFollowers(userId, currentPage, pageSize);
            followData.put("followers", followersPage);
            log.debug("用户粉丝列表获取成功: 总数={}", followersPage.getTotal());
        } catch (Exception e) {
            log.warn("获取用户粉丝列表失败: userId={}", userId, e);
            followData.put("followers", null);
        }
        
        return followData;
    }

    /**
     * 获取用户关注统计
     */
    @GetMapping("/follows/statistics")
    @Operation(summary = "获取用户关注统计", description = "获取用户的关注和粉丝统计信息")
    public Result<Map<String, Object>> getFollowStatistics(
            @Parameter(description = "用户ID") @RequestParam Long userId) {
        
        try {
            log.info("REST请求 - 获取用户关注统计: userId={}", userId);
            
            Map<String, Object> statistics = new HashMap<>();
            
            // 关注数（用户关注了多少人）
            Long followingCount = followService.getFollowingCount(userId);
            statistics.put("followingCount", followingCount);
            
            // 粉丝数（多少人关注了用户）
            Long followersCount = followService.getFollowersCount(userId);
            statistics.put("followersCount", followersCount);
            
            // 互关数（可选，如果有相应接口）
            try {
                Map<String, Object> followStats = followService.getFollowStatistics(userId);
                statistics.putAll(followStats);
            } catch (Exception e) {
                log.debug("获取详细关注统计失败，仅返回基础统计", e);
            }
            
            log.info("用户关注统计获取成功: userId={}, following={}, followers={}", 
                    userId, followingCount, followersCount);
            return Result.success(statistics);
            
        } catch (Exception e) {
            log.error("获取用户关注统计失败: userId={}", userId, e);
            return Result.error("获取用户关注统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取关注数量的辅助方法
     */
    private Long getFollowCount(Long userId) {
        try {
            // 获取用户关注的人数（关注数）
            Long followingCount = followService.getFollowingCount(userId);
            log.debug("用户关注数量获取成功: userId={}, followingCount={}", userId, followingCount);
            return followingCount;
        } catch (Exception e) {
            log.warn("获取关注数量失败: userId={}", userId, e);
            return 0L;
        }
    }
}
