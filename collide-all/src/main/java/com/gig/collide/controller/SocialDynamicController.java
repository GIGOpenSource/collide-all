package com.gig.collide.controller;

import com.gig.collide.Apientry.api.common.response.PageResponse;
import com.gig.collide.Apientry.api.common.response.Result;
import com.gig.collide.Apientry.api.social.SocialService;
import com.gig.collide.Apientry.api.social.request.SocialDynamicCreateRequest;
import com.gig.collide.Apientry.api.social.request.SocialDynamicQueryRequest;
import com.gig.collide.Apientry.api.social.request.SocialDynamicUpdateRequest;
import com.gig.collide.Apientry.api.social.response.SocialDynamicResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

import cn.dev33.satoken.stp.StpUtil;

/**
 * 社交动态管理控制器 - 严格对应接口版
 * 严格按照SocialService接口设计，提供HTTP REST接口
 * 包含完整的CRUD功能和统计查询功能
 * 
 * 核心功能：
 * - 业务CRUD操作
 * - 核心查询方法
 * - 统计计数方法
 * - 互动统计更新
 * - 状态管理
 * - 用户信息同步
 * - 数据清理
 * - 特殊查询
 * - 系统健康检查
 *
 * =================== 组合接口调用说明 ===================
 * 
 * 1. 动态创建流程：
 *    - 创建动态：POST /api/v1/social/dynamics/create
 *    - 批量创建：POST /api/v1/social/dynamics/batch-create
 *    - 创建分享：POST /api/v1/social/dynamics/create-share
 * 
 * 2. 动态查询流程：
 *    - 分页查询：POST /api/v1/social/dynamics/query (支持多条件组合)
 *    - 用户动态：GET /api/v1/social/dynamics/user/{userId}
 *    - 类型查询：GET /api/v1/social/dynamics/type/{dynamicType}
 *    - 状态查询：GET /api/v1/social/dynamics/status/{status}
 *    - 关注动态：GET /api/v1/social/dynamics/following/{userId}
 *    - 内容搜索：GET /api/v1/social/dynamics/search?keyword=xxx
 *    - 热门动态：GET /api/v1/social/dynamics/hot
 *    - 分享动态：GET /api/v1/social/dynamics/share-target
 * 
 * 3. 统计查询流程：
 *    - 用户统计：GET /api/v1/social/dynamics/count/user/{userId}
 *    - 类型统计：GET /api/v1/social/dynamics/count/type/{dynamicType}
 *    - 时间统计：GET /api/v1/social/dynamics/count/time-range
 * 
 * 4. 互动更新流程：
 *    - 点赞：POST /api/v1/social/dynamics/{dynamicId}/like/increase
 *    - 取消点赞：POST /api/v1/social/dynamics/{dynamicId}/like/decrease
 *    - 评论：POST /api/v1/social/dynamics/{dynamicId}/comment/increase
 *    - 分享：POST /api/v1/social/dynamics/{dynamicId}/share/increase
 *    - 批量更新统计：PUT /api/v1/social/dynamics/{dynamicId}/statistics
 * 
 * 5. 状态管理流程：
 *    - 更新状态：PUT /api/v1/social/dynamics/{dynamicId}/status
 *    - 批量更新状态：PUT /api/v1/social/dynamics/batch-status
 *    - 更新用户信息：PUT /api/v1/social/dynamics/user-info
 * 
 * 6. 数据管理流程：
 *    - 清理历史：DELETE /api/v1/social/dynamics/cleanup
 *    - 查询最新：GET /api/v1/social/dynamics/latest
 *    - 用户最新：GET /api/v1/social/dynamics/user/{userId}/latest
 *    - 分享列表：GET /api/v1/social/dynamics/share-list
 * 
 * =================== 组合调用示例 ===================
 * 
 * 示例1：完整的动态发布流程
 * 1. 创建动态：POST /create
 * 2. 获取动态列表：GET /user/{userId}
 * 3. 更新统计数据：PUT /{dynamicId}/statistics
 * 
 * 示例2：用户动态流查询流程
 * 1. 获取用户动态：GET /user/{userId}
 * 2. 获取关注用户动态：GET /following/{userId}
 * 3. 获取热门动态：GET /hot
 * 4. 组合展示：前端合并三个接口结果
 * 
 * 示例3：动态互动流程
 * 1. 查询动态详情：GET /detail/{dynamicId}
 * 2. 增加点赞：POST /{dynamicId}/like/increase
 * 3. 更新统计数据：PUT /{dynamicId}/statistics
 * 
 * 示例4：批量操作流程
 * 1. 批量创建：POST /batch-create
 * 2. 批量查询：POST /query (多条件)
 * 3. 批量更新状态：PUT /batch-status
 * 4. 批量清理：DELETE /cleanup
 * 
 * =================== 注意事项 ===================
 * 
 * 1. 所有需要权限的操作都需要传递operatorId参数
 * 2. 分页查询默认页码为1，页面大小为20
 * 3. 时间参数使用ISO格式：2024-01-30T10:30:00
 * 4. 状态值：normal(正常)、hidden(隐藏)、deleted(删除)
 * 5. 动态类型：text(文本)、image(图片)、video(视频)、share(分享)
 * 6. 分享目标类型：content(内容)、goods(商品)、dynamic(动态)
 *
 * @author GIG Team
 * @version 3.0.0 (严格对应接口版)
 * @since 2024-01-30
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/social/dynamics")
@RequiredArgsConstructor
@Validated
@Tag(name = "社交动态管理", description = "社交动态管理相关接口 - 严格对应接口版")
public class SocialDynamicController {

    private final SocialService socialService;

    // =================== 业务CRUD操作 ===================

    /**
     * 创建动态
     * 
     * 接口说明：
     * - 功能：创建新的社交动态，包含用户验证和内容检查
     * - 权限：需要用户登录，自动获取当前用户信息
     * - 业务逻辑：自动设置默认值，验证内容长度和类型
     * 
     * 请求示例：
     * POST /api/v1/social/dynamics/create
     * {
     *   "content": "今天天气真不错！",
     *   "dynamicType": "text",
     *   "userId": 123,
     *   "userNickname": "张三",
     *   "userAvatar": "https://example.com/avatar.jpg"
     * }
     * 
     * 响应示例：
     * {
     *   "code": 200,
     *   "message": "success",
     *   "data": {
     *     "id": 456,
     *     "content": "今天天气真不错！",
     *     "dynamicType": "text",
     *     "userId": 123,
     *     "userNickname": "张三",
     *     "userAvatar": "https://example.com/avatar.jpg",
     *     "likeCount": 0,
     *     "commentCount": 0,
     *     "shareCount": 0,
     *     "status": "normal",
     *     "createTime": "2024-01-30T10:30:00"
     *   }
     * }
     * 
     * 组合调用：
     * 1. 创建动态后，可以调用 GET /user/{userId} 获取用户动态列表
     * 2. 创建动态后，可以调用 PUT /{dynamicId}/statistics 更新统计数据
     * 3. 创建分享动态时，会自动更新原动态的分享数
     */
    @PostMapping("/create")
    @Operation(summary = "创建动态", description = "创建新的社交动态，包含用户验证和内容检查")
    public Result<SocialDynamicResponse> createDynamic(@Valid @RequestBody SocialDynamicCreateRequest request) {
        log.info("REST请求 - 创建动态: 用户ID={}, 类型={}", request.getUserId(), request.getDynamicType());
        return socialService.createDynamic(request);
    }

    /**
     * 批量创建动态
     */
    @PostMapping("/batch-create")
    @Operation(summary = "批量创建动态", description = "批量创建动态，用于批量导入或管理员操作")
    public Result<Integer> batchCreateDynamics(
            @Valid @RequestBody @NotNull(message = "请求列表不能为空") List<SocialDynamicCreateRequest> requests,
            @Parameter(description = "操作者ID") @RequestParam @NotNull(message = "操作者ID不能为空") Long operatorId) {
        log.info("REST请求 - 批量创建动态: 数量={}, 操作者ID={}", requests.size(), operatorId);
        return socialService.batchCreateDynamics(requests, operatorId);
    }

    /**
     * 创建分享动态
     */
    @PostMapping("/create-share")
    @Operation(summary = "创建分享动态", description = "创建分享其他内容的动态")
    public Result<SocialDynamicResponse> createShareDynamic(@Valid @RequestBody SocialDynamicCreateRequest request) {
        log.info("REST请求 - 创建分享动态: 用户ID={}, 分享目标={}", request.getUserId(), request.getShareTargetType());
        return socialService.createShareDynamic(request);
    }

    /**
     * 更新动态
     */
    @PutMapping("/update")
    @Operation(summary = "更新动态", description = "更新动态内容，包含权限验证")
    public Result<SocialDynamicResponse> updateDynamic(@Valid @RequestBody SocialDynamicUpdateRequest request) {
        log.info("REST请求 - 更新动态: 动态ID={}, 用户ID={}", request.getId(), request.getUserId());
        return socialService.updateDynamic(request);
    }

    /**
     * 删除动态
     */
    @DeleteMapping("/{dynamicId}")
    @Operation(summary = "删除动态", description = "逻辑删除动态，包含权限验证")
    public Result<Void> deleteDynamic(
            @Parameter(description = "动态ID") @PathVariable @NotNull(message = "动态ID不能为空") Long dynamicId,
            @Parameter(description = "操作者ID") @RequestParam @NotNull(message = "操作者ID不能为空") Long operatorId) {
        log.info("REST请求 - 删除动态: 动态ID={}, 操作者ID={}", dynamicId, operatorId);
        return socialService.deleteDynamic(dynamicId, operatorId);
    }

    /**
     * 根据ID查询动态详情
     */
    @GetMapping({"/detail/{dynamicId}", "/detail"})
    @Operation(summary = "查询动态详情", description = "根据ID查询动态详情，支持路径参数和查询参数两种方式")
    public Result<SocialDynamicResponse> getDynamicById(
            @Parameter(description = "动态ID") @PathVariable(required = false) Long dynamicId,
            @Parameter(description = "动态ID（查询参数）") @RequestParam(required = false) Long dynamic_id,
            @Parameter(description = "是否包含已删除") @RequestParam(defaultValue = "false") Boolean includeDeleted) {
        
        // 优先使用路径参数，如果没有则使用查询参数
        Long finalDynamicId = dynamicId != null ? dynamicId : dynamic_id;
        
        if (finalDynamicId == null) {
            return Result.error("动态ID不能为空");
        }
        
        log.info("REST请求 - 查询动态详情: 动态ID={}, 包含已删除={}", finalDynamicId, includeDeleted);
        return socialService.getDynamicById(finalDynamicId, includeDeleted);
    }

    /**
     * 分页查询动态列表
     * 
     * 接口说明：
     * - 功能：支持多条件组合查询动态列表，最灵活的查询接口
     * - 权限：公开接口，无需登录
     * - 业务逻辑：根据传入条件智能选择最优查询策略
     * 
     * 查询策略优先级：
     * 1. 如果指定userId，优先使用用户动态查询
     * 2. 如果指定dynamicType，使用类型查询
     * 3. 如果指定status，使用状态查询
     * 4. 如果指定keyword，使用内容搜索
     * 5. 默认查询所有正常状态的动态
     * 
     * 请求示例：
     * POST /api/v1/social/dynamics/query
     * {
     *   "pageNumber": 1,
     *   "pageSize": 20,
     *   "userId": 123,           // 可选：指定用户ID
     *   "dynamicType": "text",   // 可选：指定动态类型
     *   "status": "normal",      // 可选：指定状态
     *   "keyword": "天气"        // 可选：搜索关键词
     * }
     * 
     * 响应示例：
     * {
     *   "code": 200,
     *   "message": "success",
     *   "data": {
     *     "records": [
     *       {
     *         "id": 456,
     *         "content": "今天天气真不错！",
     *         "dynamicType": "text",
     *         "userId": 123,
     *         "userNickname": "张三",
     *         "likeCount": 5,
     *         "commentCount": 2,
     *         "shareCount": 1,
     *         "createTime": "2024-01-30T10:30:00",
     *         "isLiked": true,        // 新增：当前用户是否点赞过
     *         "isFollowed": false     // 新增：当前用户是否关注过作者
     *       }
     *     ],
     *     "total": 100,
     *     "current": 1,
     *     "size": 20,
     *     "pages": 5
     *   }
     * }
     * 
     * 组合调用场景：
     * 1. 首页动态流：不传任何条件，获取最新动态
     * 2. 用户主页：传入userId，获取用户动态
     * 3. 分类浏览：传入dynamicType，获取特定类型动态
     * 4. 内容搜索：传入keyword，搜索相关内容
     * 5. 状态筛选：传入status，筛选特定状态动态
     * 
     * 性能优化：
     * - 支持数据库索引优化
     * - 分页查询避免大数据量传输
     * - 智能查询策略选择最优执行计划
     */
    @PostMapping("/query")
    @Operation(summary = "分页查询动态", description = "支持多条件组合查询动态列表")
    public Result<PageResponse<SocialDynamicResponse>> queryDynamics(@Valid @RequestBody SocialDynamicQueryRequest request) {
        try {
            // 参数验证和默认值处理
            if (request.getPageNumber() == null || request.getPageNumber() < 1) {
                request.setPageNumber(1);
            }
            if (request.getPageSize() == null || request.getPageSize() < 1) {
                request.setPageSize(20);
            }
            
            // =================== 获取当前用户ID ===================
            Long currentUserId = null;
            
            // 优先从请求参数中获取当前用户ID
            if (request.getCurrentUserId() != null) {
                currentUserId = request.getCurrentUserId();
                log.debug("从请求参数获取当前用户ID: {}", currentUserId);
            } else {
                // 尝试从token中获取当前用户ID
                try {
                    if (StpUtil.isLogin()) {
                        currentUserId = StpUtil.getLoginIdAsLong();
                        log.debug("从token获取当前用户ID: {}", currentUserId);
                    }
                } catch (Exception e) {
                    log.debug("获取当前用户ID失败，可能是未登录状态: {}", e.getMessage());
                }
            }
            
            // 设置当前用户ID到请求对象
            request.setCurrentUserId(currentUserId);
            
            log.info("REST请求 - 分页查询动态: 页码={}, 大小={}, 当前用户ID={}", 
                    request.getPageNumber(), request.getPageSize(), currentUserId);
            
            return socialService.queryDynamics(request);
        } catch (Exception e) {
            log.error("分页查询动态失败", e);
            return Result.error("分页查询动态失败: " + e.getMessage());
        }
    }

    // =================== 核心查询方法 ===================

    /**
     * 根据用户ID分页查询动态
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "查询用户动态", description = "根据用户ID分页查询动态")
    public Result<PageResponse<SocialDynamicResponse>> selectByUserId(
            @Parameter(description = "用户ID") @PathVariable @NotNull(message = "用户ID不能为空") Long userId,
            @Parameter(description = "状态") @RequestParam(required = false) String status,
            @Parameter(description = "动态类型") @RequestParam(required = false) String dynamicType,
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer currentPage,
            @Parameter(description = "页面大小") @RequestParam(defaultValue = "20") Integer pageSize) {
        log.info("REST请求 - 查询用户动态: 用户ID={}, 状态={}, 类型={}, 页码={}/{}", 
                userId, status, dynamicType, currentPage, pageSize);
        return socialService.selectByUserId(currentPage, pageSize, userId, status, dynamicType);
    }

    /**
     * 根据动态类型分页查询
     */
    @GetMapping("/type/{dynamicType}")
    @Operation(summary = "按类型查询动态", description = "根据动态类型分页查询")
    public Result<PageResponse<SocialDynamicResponse>> selectByDynamicType(
            @Parameter(description = "动态类型") @PathVariable @NotNull(message = "动态类型不能为空") String dynamicType,
            @Parameter(description = "状态") @RequestParam(required = false) String status,
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer currentPage,
            @Parameter(description = "页面大小") @RequestParam(defaultValue = "20") Integer pageSize,
            @Parameter(description = "当前用户ID") @RequestParam(required = false) Long currentUserId) {
        log.info("REST请求 - 按类型查询动态: 类型={}, 状态={}, 页码={}/{}", dynamicType, status, currentPage, pageSize);
        return socialService.selectByDynamicType(currentPage, pageSize, dynamicType, status, currentUserId);
    }

    /**
     * 根据状态分页查询动态
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "按状态查询动态", description = "根据状态分页查询动态")
    public Result<PageResponse<SocialDynamicResponse>> selectByStatus(
            @Parameter(description = "状态") @PathVariable @NotNull(message = "状态不能为空") String status,
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer currentPage,
            @Parameter(description = "页面大小") @RequestParam(defaultValue = "20") Integer pageSize,
            @Parameter(description = "当前用户ID") @RequestParam(required = false) Long currentUserId) {
        log.info("REST请求 - 按状态查询动态: 状态={}, 页码={}/{}", status, currentPage, pageSize);
        return socialService.selectByStatus(currentPage, pageSize, status, currentUserId);
    }

    /**
     * 获取关注用户的动态流
     */
    @GetMapping("/following/{userId}")
    @Operation(summary = "获取关注用户动态流", description = "获取指定用户关注的其他用户的动态")
    public Result<PageResponse<SocialDynamicResponse>> selectFollowingDynamics(
            @Parameter(description = "用户ID") @PathVariable @NotNull(message = "用户ID不能为空") Long userId,
            @Parameter(description = "状态") @RequestParam(required = false) String status,
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer currentPage,
            @Parameter(description = "页面大小") @RequestParam(defaultValue = "20") Integer pageSize,
            @Parameter(description = "当前用户ID") @RequestParam(required = false) Long currentUserId) {
        log.info("REST请求 - 获取关注用户动态流: 用户ID={}, 状态={}, 页码={}/{}", userId, status, currentPage, pageSize);
        return socialService.selectFollowingDynamics(currentPage, pageSize, userId, status, currentUserId);
    }

    /**
     * 获取关注用户的动态流（查询参数版）
     * 接受 userId/currentPage/pageSize 作为查询参数，便于统一前端调用方式
     */
    @GetMapping(value = "/following", params = {"userId"})
    @Operation(summary = "获取关注用户动态流(参数版)", description = "通过查询参数 userId 获取关注动态，支持分页")
    public Result<PageResponse<SocialDynamicResponse>> selectFollowingDynamicsByQuery(
            @Parameter(description = "用户ID") @RequestParam @NotNull(message = "用户ID不能为空") Long userId,
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer currentPage,
            @Parameter(description = "页面大小") @RequestParam(defaultValue = "20") Integer pageSize,
            @Parameter(description = "状态") @RequestParam(required = false) String status,
            @Parameter(description = "当前用户ID") @RequestParam(required = false) Long currentUserId) {
        log.info("REST请求 - 获取关注用户动态流(参数版): 用户ID={}, 状态={}, 页码={}/{}", userId, status, currentPage, pageSize);
        return socialService.selectFollowingDynamics(currentPage, pageSize, userId, status, currentUserId);
    }

    /**
     * 搜索动态
     */
    @GetMapping("/search")
    @Operation(summary = "搜索动态", description = "根据关键词搜索动态内容")
    public Result<PageResponse<SocialDynamicResponse>> searchByContent(
            @Parameter(description = "搜索关键词", required = true) @RequestParam @NotNull(message = "搜索关键词不能为空") String keyword,
            @Parameter(description = "状态") @RequestParam(required = false) String status,
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer currentPage,
            @Parameter(description = "页面大小") @RequestParam(defaultValue = "20") Integer pageSize,
            @Parameter(description = "当前用户ID") @RequestParam(required = false) Long currentUserId) {
        log.info("REST请求 - 搜索动态: 关键词={}, 状态={}, 页码={}/{}", keyword, status, currentPage, pageSize);
        return socialService.searchByContent(currentPage, pageSize, keyword, status, currentUserId);
    }

    /**
     * 获取热门动态
     */
    @GetMapping("/hot")
    @Operation(summary = "获取热门动态", description = "获取热门动态，按互动数排序")
    public Result<PageResponse<SocialDynamicResponse>> selectHotDynamics(
            @Parameter(description = "状态") @RequestParam(required = false) String status,
            @Parameter(description = "动态类型") @RequestParam(required = false) String dynamicType,
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer currentPage,
            @Parameter(description = "页面大小") @RequestParam(defaultValue = "20") Integer pageSize,
            @Parameter(description = "当前用户ID") @RequestParam(required = false) Long currentUserId) {
        log.info("REST请求 - 获取热门动态: 状态={}, 类型={}, 页码={}/{}", status, dynamicType, currentPage, pageSize);
        return socialService.selectHotDynamics(currentPage, pageSize, status, dynamicType, currentUserId);
    }

    /**
     * 根据分享目标查询分享动态
     */
    @GetMapping("/share-target")
    @Operation(summary = "查询分享动态", description = "根据分享目标查询分享动态")
    public Result<PageResponse<SocialDynamicResponse>> selectByShareTarget(
            @Parameter(description = "分享目标类型") @RequestParam @NotNull(message = "分享目标类型不能为空") String shareTargetType,
            @Parameter(description = "分享目标ID") @RequestParam @NotNull(message = "分享目标ID不能为空") Long shareTargetId,
            @Parameter(description = "状态") @RequestParam(required = false) String status,
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer currentPage,
            @Parameter(description = "页面大小") @RequestParam(defaultValue = "20") Integer pageSize,
            @Parameter(description = "当前用户ID") @RequestParam(required = false) Long currentUserId) {
        log.info("REST请求 - 查询分享动态: 目标类型={}, 目标ID={}, 状态={}, 页码={}/{}", 
                shareTargetType, shareTargetId, status, currentPage, pageSize);
        return socialService.selectByShareTarget(currentPage, pageSize, shareTargetType, shareTargetId, status, currentUserId);
    }

    // =================== 统计计数方法 ===================

    /**
     * 统计用户动态数量
     */
    @GetMapping("/count/user/{userId}")
    @Operation(summary = "统计用户动态数量", description = "统计指定用户的动态数量")
    public Result<Long> countByUserId(
            @Parameter(description = "用户ID") @PathVariable @NotNull(message = "用户ID不能为空") Long userId,
            @Parameter(description = "状态") @RequestParam(required = false) String status,
            @Parameter(description = "动态类型") @RequestParam(required = false) String dynamicType) {
        log.info("REST请求 - 统计用户动态数量: 用户ID={}, 状态={}, 类型={}", userId, status, dynamicType);
        return socialService.countByUserId(userId, status, dynamicType);
    }

    /**
     * 统计动态类型数量
     */
    @GetMapping("/count/type/{dynamicType}")
    @Operation(summary = "统计动态类型数量", description = "统计指定类型的动态数量")
    public Result<Long> countByDynamicType(
            @Parameter(description = "动态类型") @PathVariable @NotNull(message = "动态类型不能为空") String dynamicType,
            @Parameter(description = "状态") @RequestParam(required = false) String status) {
        log.info("REST请求 - 统计动态类型数量: 类型={}, 状态={}", dynamicType, status);
        return socialService.countByDynamicType(dynamicType, status);
    }

    /**
     * 统计时间范围内动态数量
     */
    @GetMapping("/count/time-range")
    @Operation(summary = "统计时间范围动态数量", description = "统计指定时间范围内的动态数量")
    public Result<Long> countByTimeRange(
            @Parameter(description = "开始时间") @RequestParam @NotNull(message = "开始时间不能为空") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam @NotNull(message = "结束时间不能为空") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @Parameter(description = "状态") @RequestParam(required = false) String status) {
        log.info("REST请求 - 统计时间范围动态数量: 开始时间={}, 结束时间={}, 状态={}", startTime, endTime, status);
        return socialService.countByTimeRange(startTime, endTime, status);
    }

    // =================== 互动统计更新 ===================

    /**
     * 增加点赞数
     */
    @PostMapping({"/{dynamicId}/like/increase", "/like/increase/{dynamicId}"})
    @Operation(summary = "增加点赞数", description = "为动态增加点赞数")
    public Result<Integer> increaseLikeCount(
            @Parameter(description = "动态ID") @PathVariable @NotNull(message = "动态ID不能为空") Long dynamicId,
            @Parameter(description = "操作者ID") @RequestParam @NotNull(message = "操作者ID不能为空") Long operatorId) {
        log.info("REST请求 - 增加点赞数: 动态ID={}, 操作者ID={}", dynamicId, operatorId);
        return socialService.increaseLikeCount(dynamicId, operatorId);
    }

    /**
     * 增加点赞数(请求体版本)
     * 
     * 接口说明：
     * - 功能：为动态增加点赞数，支持通过请求体传递参数
     * - 权限：需要传递userId，验证操作权限
     * - 业务逻辑：原子性更新，自动记录操作者信息
     * 
     * 请求示例：
     * POST /api/v1/social/dynamics/like/increase
     * {
     *   "id": 2,
     *   "userId": 3
     * }
     * 
     * 响应示例：
     * {
     *   "code": 200,
     *   "message": "success",
     *   "data": 1
     * }
     * 
     * 注意事项：
     * - id字段为动态ID，userId字段为操作者ID
     * - 两个字段都是必填的
     * - 建议点赞后立即更新统计数据，保持数据同步
     */
    @PostMapping("/like/increase")
    @Operation(summary = "增加点赞数(请求体版本)", description = "为动态增加点赞数，参数通过请求体传递")
    public Result<Integer> increaseLikeCountFromBody(@Valid @RequestBody SocialDynamicUpdateRequest request) {
        log.info("REST请求 - 增加点赞数(请求体版本): 动态ID={}, 操作者ID={}", request.getId(), request.getUserId());
        return socialService.increaseLikeCount(request.getId(), request.getUserId());
    }

    /**
     * 减少点赞数
     */
    @PostMapping({"/{dynamicId}/like/decrease", "/like/decrease/{dynamicId}"})
    @Operation(summary = "减少点赞数", description = "为动态减少点赞数")
    public Result<Integer> decreaseLikeCount(
            @Parameter(description = "动态ID") @PathVariable @NotNull(message = "动态ID不能为空") Long dynamicId,
            @Parameter(description = "操作者ID") @RequestParam @NotNull(message = "操作者ID不能为空") Long operatorId) {
        log.info("REST请求 - 减少点赞数: 动态ID={}, 操作者ID={}", dynamicId, operatorId);
        return socialService.decreaseLikeCount(dynamicId, operatorId);
    }

    /**
     * 减少点赞数(请求体版本)
     * 
     * 接口说明：
     * - 功能：为动态减少点赞数，支持通过请求体传递参数
     * - 权限：需要传递userId，验证操作权限
     * - 业务逻辑：原子性更新，自动记录操作者信息
     * 
     * 请求示例：
     * POST /api/v1/social/dynamics/like/decrease
     * {
     *   "id": 2,
     *   "userId": 3
     * }
     * 
     * 响应示例：
     * {
     *   "code": 200,
     *   "message": "success",
     *   "data": 1
     * }
     */
    @PostMapping("/like/decrease")
    @Operation(summary = "减少点赞数(请求体版本)", description = "为动态减少点赞数，参数通过请求体传递")
    public Result<Integer> decreaseLikeCountFromBody(@Valid @RequestBody SocialDynamicUpdateRequest request) {
        log.info("REST请求 - 减少点赞数(请求体版本): 动态ID={}, 操作者ID={}", request.getId(), request.getUserId());
        return socialService.decreaseLikeCount(request.getId(), request.getUserId());
    }

    /**
     * 增加评论数
     */
    @PostMapping({"/{dynamicId}/comment/increase", "/comment/increase/{dynamicId}"})
    @Operation(summary = "增加评论数", description = "为动态增加评论数")
    public Result<Integer> increaseCommentCount(
            @Parameter(description = "动态ID") @PathVariable @NotNull(message = "动态ID不能为空") Long dynamicId,
            @Parameter(description = "操作者ID") @RequestParam @NotNull(message = "操作者ID不能为空") Long operatorId) {
        log.info("REST请求 - 增加评论数: 动态ID={}, 操作者ID={}", dynamicId, operatorId);
        return socialService.increaseCommentCount(dynamicId, operatorId);
    }

    /**
     * 增加评论数(请求体版本)
     * 
     * 接口说明：
     * - 功能：为动态增加评论数，支持通过请求体传递参数
     * - 权限：需要传递userId，验证操作权限
     * - 业务逻辑：原子性更新，自动记录操作者信息
     * 
     * 请求示例：
     * POST /api/v1/social/dynamics/comment/increase
     * {
     *   "id": 2,
     *   "userId": 3
     * }
     * 
     * 响应示例：
     * {
     *   "code": 200,
     *   "message": "success",
     *   "data": 1
     * }
     */
    @PostMapping("/comment/increase")
    @Operation(summary = "增加评论数(请求体版本)", description = "为动态增加评论数，参数通过请求体传递")
    public Result<Integer> increaseCommentCountFromBody(@Valid @RequestBody SocialDynamicUpdateRequest request) {
        log.info("REST请求 - 增加评论数(请求体版本): 动态ID={}, 操作者ID={}", request.getId(), request.getUserId());
        return socialService.increaseCommentCount(request.getId(), request.getUserId());
    }

    /**
     * 增加分享数
     */
    @PostMapping({"/{dynamicId}/share/increase", "/share/increase/{dynamicId}"})
    @Operation(summary = "增加分享数", description = "为动态增加分享数")
    public Result<Integer> increaseShareCount(
            @Parameter(description = "动态ID") @PathVariable @NotNull(message = "动态ID不能为空") Long dynamicId,
            @Parameter(description = "操作者ID") @RequestParam @NotNull(message = "操作者ID不能为空") Long operatorId) {
        log.info("REST请求 - 增加分享数: 动态ID={}, 操作者ID={}", dynamicId, operatorId);
        return socialService.increaseShareCount(dynamicId, operatorId);
    }

    // 添加一个新的支持请求体参数的方法
    @PostMapping("/share/increase")
    @Operation(summary = "增加分享数(请求体版本)", description = "为动态增加分享数，参数通过请求体传递")
    public Result<Integer> increaseShareCountFromBody(@Valid @RequestBody SocialDynamicUpdateRequest request) {
        log.info("REST请求 - 增加分享数(请求体版本): 动态ID={}, 操作者ID={}", request.getId(), request.getUserId());
        return socialService.increaseShareCount(request.getId(), request.getUserId());
    }

    /**
     * 批量更新统计数据
     */
    @PutMapping({"/{dynamicId}/statistics", "/statistics/{dynamicId}"})
    @Operation(summary = "批量更新统计数据", description = "批量更新动态的点赞数、评论数、分享数")
    public Result<Integer> updateStatistics(
            @Parameter(description = "动态ID") @PathVariable @NotNull(message = "动态ID不能为空") Long dynamicId,
            @Parameter(description = "点赞数") @RequestParam(required = false) Long likeCount,
            @Parameter(description = "评论数") @RequestParam(required = false) Long commentCount,
            @Parameter(description = "分享数") @RequestParam(required = false) Long shareCount,
            @Parameter(description = "操作者ID") @RequestParam @NotNull(message = "操作者ID不能为空") Long operatorId) {
        log.info("REST请求 - 批量更新统计数据: 动态ID={}, 点赞数={}, 评论数={}, 分享数={}, 操作者ID={}", 
                dynamicId, likeCount, commentCount, shareCount, operatorId);
        return socialService.updateStatistics(dynamicId, likeCount, commentCount, shareCount, operatorId);
    }

    // =================== 状态管理 ===================

    /**
     * 更新动态状态
     */
    @PutMapping({"/{dynamicId}/status", "/status/{dynamicId}"})
    @Operation(summary = "更新动态状态", description = "更新动态的状态")
    public Result<Integer> updateStatus(
            @Parameter(description = "动态ID") @PathVariable @NotNull(message = "动态ID不能为空") Long dynamicId,
            @Parameter(description = "新状态") @RequestParam @NotNull(message = "新状态不能为空") String status,
            @Parameter(description = "操作者ID") @RequestParam @NotNull(message = "操作者ID不能为空") Long operatorId) {
        log.info("REST请求 - 更新动态状态: 动态ID={}, 新状态={}, 操作者ID={}", dynamicId, status, operatorId);
        return socialService.updateStatus(dynamicId, status, operatorId);
    }

    /**
     * 批量更新动态状态
     */
    @PutMapping("/batch-status")
    @Operation(summary = "批量更新动态状态", description = "批量更新多个动态的状态")
    public Result<Integer> batchUpdateStatus(
            @Parameter(description = "动态ID列表") @RequestBody @NotNull(message = "动态ID列表不能为空") List<Long> dynamicIds,
            @Parameter(description = "新状态") @RequestParam @NotNull(message = "新状态不能为空") String status,
            @Parameter(description = "操作者ID") @RequestParam @NotNull(message = "操作者ID不能为空") Long operatorId) {
        log.info("REST请求 - 批量更新动态状态: 动态数量={}, 新状态={}, 操作者ID={}", dynamicIds.size(), status, operatorId);
        return socialService.batchUpdateStatus(dynamicIds, status, operatorId);
    }

    // =================== 用户信息同步 ===================

    /**
     * 批量更新用户冗余信息
     */
    @PutMapping("/user-info")
    @Operation(summary = "更新用户冗余信息", description = "批量更新动态中用户的冗余信息")
    public Result<Integer> updateUserInfo(
            @Parameter(description = "用户ID") @RequestParam @NotNull(message = "用户ID不能为空") Long userId,
            @Parameter(description = "用户昵称") @RequestParam @NotNull(message = "用户昵称不能为空") String userNickname,
            @Parameter(description = "用户头像") @RequestParam @NotNull(message = "用户头像不能为空") String userAvatar,
            @Parameter(description = "操作者ID") @RequestParam @NotNull(message = "操作者ID不能为空") Long operatorId) {
        log.info("REST请求 - 更新用户冗余信息: 用户ID={}, 昵称={}, 操作者ID={}", userId, userNickname, operatorId);
        return socialService.updateUserInfo(userId, userNickname, userAvatar, operatorId);
    }

    // =================== 数据清理 ===================

    /**
     * 物理删除历史动态
     */
    @DeleteMapping("/cleanup")
    @Operation(summary = "清理历史动态", description = "物理删除指定状态的历史动态")
    public Result<Integer> deleteByStatusAndTime(
            @Parameter(description = "状态") @RequestParam @NotNull(message = "状态不能为空") String status,
            @Parameter(description = "删除时间之前") @RequestParam @NotNull(message = "删除时间不能为空") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime beforeTime,
            @Parameter(description = "删除数量限制") @RequestParam(defaultValue = "1000") Integer limit,
            @Parameter(description = "操作者ID") @RequestParam @NotNull(message = "操作者ID不能为空") Long operatorId) {
        log.info("REST请求 - 清理历史动态: 状态={}, 时间={}, 限制={}, 操作者ID={}", status, beforeTime, limit, operatorId);
        return socialService.deleteByStatusAndTime(status, beforeTime, limit, operatorId);
    }

    // =================== 特殊查询 ===================

    /**
     * 查询最新动态
     */
    @GetMapping("/latest")
    @Operation(summary = "查询最新动态", description = "查询全局最新动态")
    public Result<List<SocialDynamicResponse>> selectLatestDynamics(
            @Parameter(description = "限制数量") @RequestParam(defaultValue = "10") Integer limit,
            @Parameter(description = "状态") @RequestParam(required = false) String status) {
        log.info("REST请求 - 查询最新动态: 限制数量={}, 状态={}", limit, status);
        return socialService.selectLatestDynamics(limit, status);
    }

    /**
     * 分页查询最新动态（当携带 currentPage/pageSize 参数时匹配）
     * 与原有 /latest 接口兼容：当前方法仅在请求同时包含 currentPage 和 pageSize 参数时生效
     */
    @GetMapping(value = "/latest", params = {"currentPage", "pageSize"})
    @Operation(summary = "分页查询最新动态", description = "按创建时间倒序分页返回最新动态，保持与 /latest 兼容")
    public Result<PageResponse<SocialDynamicResponse>> selectLatestDynamicsPage(
            @Parameter(description = "当前页码") @RequestParam Integer currentPage,
            @Parameter(description = "页面大小") @RequestParam Integer pageSize,
            @Parameter(description = "状态") @RequestParam(required = false) String status,
            @Parameter(description = "当前用户ID") @RequestParam(required = false) Long currentUserId) {
        // 基本参数兜底
        if (currentPage == null || currentPage < 1) {
            currentPage = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 20;
        }
        if (status == null || status.isEmpty()) {
            status = "normal";
        }

        log.info("REST请求 - 分页查询最新动态: 页码={}/{} 状态={}", currentPage, pageSize, status);
        // 复用现有按状态分页查询能力，默认按时间倒序（由服务层保证），语义等价于“latest”
        return socialService.selectByStatus(currentPage, pageSize, status, currentUserId);
    }

    /**
     * 查询用户最新动态
     */
    @GetMapping("/user/{userId}/latest")
    @Operation(summary = "查询用户最新动态", description = "查询指定用户的最新动态")
    public Result<List<SocialDynamicResponse>> selectUserLatestDynamics(
            @Parameter(description = "用户ID") @PathVariable @NotNull(message = "用户ID不能为空") Long userId,
            @Parameter(description = "限制数量") @RequestParam(defaultValue = "10") Integer limit,
            @Parameter(description = "状态") @RequestParam(required = false) String status) {
        log.info("REST请求 - 查询用户最新动态: 用户ID={}, 限制数量={}, 状态={}", userId, limit, status);
        return socialService.selectUserLatestDynamics(userId, limit, status);
    }

    /**
     * 查询分享动态列表
     */
    @GetMapping("/share-list")
    @Operation(summary = "查询分享动态列表", description = "查询分享动态列表")
    public Result<List<SocialDynamicResponse>> selectShareDynamics(
            @Parameter(description = "分享目标类型") @RequestParam @NotNull(message = "分享目标类型不能为空") String shareTargetType,
            @Parameter(description = "限制数量") @RequestParam(defaultValue = "10") Integer limit,
            @Parameter(description = "状态") @RequestParam(required = false) String status) {
        log.info("REST请求 - 查询分享动态列表: 目标类型={}, 限制数量={}, 状态={}", shareTargetType, limit, status);
        return socialService.selectShareDynamics(shareTargetType, limit, status);
    }

    // =================== 系统健康检查 ===================

    /**
     * 系统健康检查
     */
    @GetMapping("/health")
    @Operation(summary = "系统健康检查", description = "社交动态系统健康检查")
    public Result<String> healthCheck() {
        log.info("REST请求 - 系统健康检查");
        return socialService.healthCheck();
    }


}

