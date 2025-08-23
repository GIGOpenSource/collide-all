package com.gig.collide.controller;

import com.gig.collide.Apientry.api.common.response.PageResponse;
import com.gig.collide.Apientry.api.common.response.Result;
import com.gig.collide.Apientry.api.user.request.UserCreateRequest;
import com.gig.collide.Apientry.api.user.request.UserUpdateRequest;
import com.gig.collide.Apientry.api.user.response.UserResponse;
import com.gig.collide.Apientry.api.user.response.UserContentResponse;
import com.gig.collide.domain.User;
import com.gig.collide.domain.Content;
import com.gig.collide.domain.SocialDynamic;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gig.collide.service.UserService;
import com.gig.collide.service.ContentService;
import com.gig.collide.service.SocialDynamicService;
import com.gig.collide.service.CommentService;
import com.gig.collide.mapper.ContentMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

/**
 * 用户管理控制器
 * 
 * <p>提供用户相关的增删改查功能，包括：</p>
 * <ul>
 *   <li>创建新用户</li>
 *   <li>查询用户信息</li>
 *   <li>更新用户信息</li>
 *   <li>删除用户（逻辑删除）</li>
 *   <li>分页查询用户列表</li>
 * </ul>
 *
 * @author GIG Team
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Validated
@Tag(name = "用户管理", description = "用户管理相关接口，提供完整的用户CRUD操作")
public class UserController {

    private final UserService userService;
    private final ContentService contentService;
    private final SocialDynamicService socialDynamicService;
    private final CommentService commentService;
    private final ContentMapper contentMapper;

    /**
     * 测试接口 - 验证控制器是否正常工作
     */
    @GetMapping("/test")
    @Operation(
        summary = "测试接口", 
        description = "用于验证用户管理控制器是否正常工作"
    )
    public Result<String> test() {
        log.info("用户管理控制器测试接口被调用");
        return Result.success("用户管理控制器工作正常");
    }

    /**
     * 创建用户
     * 
     * <p>创建新用户，系统会自动设置默认值：</p>
     * <ul>
     *   <li>用户角色：默认为"user"（普通用户）</li>
     *   <li>VIP状态：默认为"N"（非VIP）</li>
     *   <li>用户状态：默认为"active"（活跃）</li>
     *   <li>创建时间：自动设置为当前时间</li>
     * </ul>
     * 
     * @param request 用户创建请求，包含用户名、昵称、邮箱、手机号、头像等信息
     * @return 创建成功的用户信息
     */
    @PostMapping
    @Operation(summary = "创建用户", description = "创建新用户，系统会自动设置默认的VIP状态和用户状态")
    public Result<UserResponse> createUser(
            @Parameter(description = "用户创建信息", required = true) @RequestBody @Valid UserCreateRequest request) {
        try {
            log.info("REST请求 - 创建用户: request={}", request);
            
            // 创建用户
            User user = convertToUser(request);
            User result = userService.createUser(user);
            
            UserResponse response = convertToResponse(result);
            return Result.success(response);
            
        } catch (Exception e) {
            log.error("创建用户失败", e);
            return Result.error("创建用户失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID查询用户
     * 
     * <p>根据用户ID查询用户的详细信息，包括：</p>
     * <ul>
     *   <li>基本信息：用户名、昵称、邮箱、手机号、头像</li>
     *   <li>状态信息：用户状态、VIP状态</li>
     *   <li>时间信息：创建时间、更新时间</li>
     * </ul>
     * 
     * @param id 用户ID，必填参数
     * @return 用户详细信息，如果用户不存在则返回错误信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询用户", description = "根据用户ID查询用户的详细信息，包括基本信息、状态信息和时间信息")
    public Result<UserResponse> getUserById(
            @Parameter(description = "用户ID", required = true, example = "123") @PathVariable Long id) {
        try {
            log.info("REST请求 - 根据ID查询用户: id={}", id);
            
            User user = userService.getUserById(id);
            if (user == null) {
                return Result.error("用户不存在");
            }
            
            UserResponse response = convertToResponse(user);
            return Result.success(response);
            
        } catch (Exception e) {
            log.error("根据ID查询用户失败: id={}", id, e);
            return Result.error("查询用户失败: " + e.getMessage());
        }
    }

    /**
     * 根据用户ID查询我发布的所有内容
     * 
     * 查询指定用户发布的所有内容，包括：
     * - t_content表中的内容：长视频、短视频、小说、漫画、文章、音频等
     * - t_social_dynamic表中的动态：文字动态、图片动态、视频动态、分享动态等
     * 
     * @param userId 用户ID，必填参数
     * @param orderBy 排序字段：createTime、updateTime、likeCount、viewCount（默认：createTime）
     * @param orderDirection 排序方向：ASC、DESC（默认：DESC）
     * @param currentPage 当前页码（默认：1）
     * @param pageSize 页面大小（默认：20）
     * @return 用户发布的所有内容列表（包含分页信息）
     */
    @GetMapping("/{userId}/contents")
    @Operation(summary = "查询用户发布的所有内容", 
               description = "根据用户ID查询该用户发布的所有内容，包括t_content表和t_social_dynamic表中的数据，返回分页信息")
    public Result<PageResponse<UserContentResponse>> getUserContents(
            @Parameter(description = "用户ID", required = true, example = "123") @PathVariable Long userId,
            @Parameter(description = "排序字段") @RequestParam(defaultValue = "createTime") String orderBy,
            @Parameter(description = "排序方向") @RequestParam(defaultValue = "DESC") String orderDirection,
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer currentPage,
            @Parameter(description = "页面大小") @RequestParam(defaultValue = "20") Integer pageSize) {
        try {
            log.info("REST请求 - 查询用户发布的所有内容: userId={}, orderBy={}, orderDirection={}, page={}/{}", 
                    userId, orderBy, orderDirection, currentPage, pageSize);
            
            // 查询t_content表中的内容（使用authorId）- 使用直接查询避免索引问题
            LambdaQueryWrapper<Content> contentQuery = new LambdaQueryWrapper<>();
            contentQuery.eq(Content::getAuthorId, userId)
                       .eq(Content::getStatus, "PUBLISHED")
                       .eq(Content::getReviewStatus, "APPROVED")
                       .orderByDesc(Content::getCreateTime); // 先按创建时间降序排序
            List<Content> contents = contentMapper.selectList(contentQuery);
            
            // 查询t_social_dynamic表中的动态（使用userId，在该表中userId就是作者ID）
            Page<SocialDynamic> dynamicPage = new Page<>(currentPage, pageSize * 2);
            IPage<SocialDynamic> dynamicResult = socialDynamicService.selectByUserId(
                    dynamicPage,
                    userId, // userId - t_social_dynamic表使用userId字段作为作者ID
                    "normal", // status - 只查询正常状态的动态
                    null // dynamicType
            );
            List<SocialDynamic> dynamics = dynamicResult.getRecords();
            
            // 转换为统一的响应格式
            List<UserContentResponse> allContents = new ArrayList<>();
            
            // 添加content表数据
            if (contents != null) {
                for (Content content : contents) {
                    UserContentResponse response = convertContentToResponse(content);
                    allContents.add(response);
                }
            }
            
            // 添加social_dynamic表数据
            if (dynamics != null) {
                for (SocialDynamic dynamic : dynamics) {
                    UserContentResponse response = convertDynamicToResponse(dynamic);
                    allContents.add(response);
                }
            }
            
            // 统一排序
            allContents.sort(getComparator(orderBy, orderDirection));
            
            // 分页处理
            int startIndex = (currentPage - 1) * pageSize;
            int endIndex = Math.min(startIndex + pageSize, allContents.size());
            List<UserContentResponse> pagedContents = allContents.subList(startIndex, endIndex);
            
            // 构建分页响应
            PageResponse<UserContentResponse> pageResponse = new PageResponse<>();
            pageResponse.setRecords(pagedContents);
            pageResponse.setTotal((long) allContents.size());
            pageResponse.setCurrentPage(currentPage);
            pageResponse.setPageSize(pageSize);
            pageResponse.setPagination((long) allContents.size(), currentPage, pageSize);
            
            log.info("查询用户内容成功: userId={}, 总数={}, 返回数量={}", userId, allContents.size(), pagedContents.size());
            return Result.success(pageResponse);
            
        } catch (Exception e) {
            log.error("查询用户发布的所有内容失败: userId={}", userId, e);
            return Result.error("查询用户内容失败: " + e.getMessage());
        }
    }

    /**
     * 根据作者ID查询我发布的所有视频
     * 
     * @param authorId 作者ID
     * @return 用户发布的所有视频列表
     */
    @GetMapping("/{authorId}/videos")
    @Operation(summary = "查询用户发布的所有视频", 
               description = "根据作者ID查询该用户在t_content表和t_social_dynamic表中发布的所有视频内容")
    public Result<List<UserContentResponse>> getUserVideos(
            @Parameter(description = "作者ID", required = true, example = "123") @PathVariable Long authorId) {
        try {
            log.info("REST请求 - 查询用户发布的所有视频: authorId={}", authorId);
            
            List<UserContentResponse> allVideos = new ArrayList<>();
            
            // 查询t_content表中的视频内容（VIDEO、SHOTVIDEO、LONGVIDEO）
            // 使用直接查询避免索引问题
            LambdaQueryWrapper<Content> contentQuery = new LambdaQueryWrapper<>();
            contentQuery.eq(Content::getAuthorId, authorId)
                       .eq(Content::getStatus, "PUBLISHED")
                       .eq(Content::getReviewStatus, "APPROVED")
                       .in(Content::getContentType, "VIDEO", "SHOTVIDEO", "LONGVIDEO")
                       .orderByDesc(Content::getCreateTime);
            List<Content> videoContents = contentMapper.selectList(contentQuery);
            
            // 转换为响应格式（已在查询中过滤了视频类型）
            if (videoContents != null) {
                for (Content content : videoContents) {
                    UserContentResponse response = convertContentToResponse(content);
                    allVideos.add(response);
                }
            }
            
            // 查询t_social_dynamic表中的视频动态
            Page<SocialDynamic> page = new Page<>(1, 1000); // 足够大以获取所有数据
            IPage<SocialDynamic> dynamicPage = socialDynamicService.selectByUserId(
                    page,
                    authorId, // userId作为作者ID
                    "normal", // status
                    "video" // dynamicType - 只查询视频类型
            );
            List<SocialDynamic> videoDynamics = dynamicPage.getRecords();
            
            // 转换动态为响应格式
            if (videoDynamics != null) {
                for (SocialDynamic dynamic : videoDynamics) {
                    UserContentResponse response = convertDynamicToResponse(dynamic);
                    allVideos.add(response);
                }
            }
            
            // 按创建时间降序排序
            allVideos.sort(Comparator.comparing(UserContentResponse::getCreateTime, 
                    Comparator.nullsLast(Comparator.naturalOrder())).reversed());
            
            log.info("查询用户视频成功: authorId={}, 视频数量={}", authorId, allVideos.size());
            return Result.success(allVideos);
            
        } catch (Exception e) {
            log.error("查询用户发布的所有视频失败: authorId={}", authorId, e);
            return Result.error("查询用户视频失败: " + e.getMessage());
        }
    }

    /**
     * 编辑用户信息
     * 
     * <p>根据用户ID更新用户信息，支持部分字段更新：</p>
     * <ul>
     *   <li>只更新请求中提供的字段</li>
     *   <li>未提供的字段保持原值不变</li>
     *   <li>支持更新：昵称、邮箱、手机号、头像、VIP状态</li>
     *   <li>更新时间会自动设置为当前时间</li>
     * </ul>
     * 
     * @param id 用户ID，路径参数，必填
     * @param request 用户更新请求，包含需要更新的字段信息
     * @return 更新后的用户信息，如果用户不存在则返回错误信息
     */
    @PutMapping("/{id}")
    @Operation(summary = "编辑用户信息", description = "根据用户ID更新用户信息，支持部分字段更新，只更新请求中提供的字段")
    public Result<UserResponse> updateUser(
            @Parameter(description = "用户ID", required = true, example = "123") @PathVariable @NotNull(message = "用户ID不能为空") Long id,
            @Parameter(description = "用户更新信息", required = true) @RequestBody @Valid UserUpdateRequest request) {
        try {
            log.info("REST请求 - 编辑用户信息: id={}, request={}", id, request);
            
            // 验证用户是否存在
            User existingUser = userService.getUserById(id);
            if (existingUser == null) {
                return Result.error("用户不存在");
            }
            

            
            // 更新用户信息
            User updatedUser = convertToUser(request, existingUser);
            User result = userService.updateUser(updatedUser);
            
            UserResponse response = convertToResponse(result);
            return Result.success(response);
            
        } catch (Exception e) {
            log.error("编辑用户信息失败: id={}", id, e);
            return Result.error("编辑用户信息失败: " + e.getMessage());
        }
    }

    /**
     * 将User实体转换为UserResponse
     * 
     * <p>将数据库实体对象转换为API响应对象，包含以下字段映射：</p>
     * <ul>
     *   <li>基本信息：ID、用户名、昵称、邮箱、手机号、头像</li>
     *   <li>状态信息：用户状态、VIP状态</li>
     *   <li>时间信息：创建时间、更新时间</li>
     * </ul>
     * 
     * @param user 用户实体对象，可以为null
     * @return 用户响应对象，如果输入为null则返回null
     */
    private UserResponse convertToResponse(User user) {
        if (user == null) {
            return null;
        }
        
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setNickname(user.getNickname());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setAvatar(user.getAvatar());
        response.setStatus(user.getStatus());
        response.setIsVip(user.getIsVip());
        response.setCreateTime(user.getCreateTime());
        response.setUpdateTime(user.getUpdateTime());
        
        return response;
    }

    /**
     * 将UserCreateRequest转换为User实体
     * 
     * <p>将创建用户请求对象转换为数据库实体对象，并设置默认值：</p>
     * <ul>
     *   <li>用户角色：默认为"user"（普通用户）</li>
     *   <li>VIP状态：默认为"N"（非VIP）</li>
     *   <li>用户状态：默认为"active"（活跃）</li>
     *   <li>创建时间：由数据库自动设置</li>
     * </ul>
     * 
     * @param request 用户创建请求对象，可以为null
     * @return 用户实体对象，如果输入为null则返回null
     */
    private User convertToUser(UserCreateRequest request) {
        if (request == null) {
            return null;
        }
        
        User user = new User();
        user.setUsername(request.getUsername());
        user.setNickname(request.getNickname());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setAvatar(request.getAvatar());
        user.setRole("user"); // 默认角色为普通用户
        user.setIsVip("N"); // 默认非VIP
        user.setStatus("active"); // 默认状态为活跃
        
        // 设置默认密码（用户后续可以通过登录接口修改密码）
        user.setPasswordHash("$2a$10$default.password.hash.for.registration");
        
        return user;
    }

    /**
     * 将UserUpdateRequest转换为User实体
     * 
     * <p>将更新用户请求对象转换为数据库实体对象，支持部分字段更新：</p>
     * <ul>
     *   <li>只更新请求中提供的字段</li>
     *   <li>未提供的字段保持原值不变</li>
     *   <li>支持更新：昵称、邮箱、手机号、头像、VIP状态</li>
     *   <li>更新时间：由数据库自动设置为当前时间</li>
     * </ul>
     * 
     * @param request 用户更新请求对象，可以为null
     * @param existingUser 现有的用户实体对象
     * @return 更新后的用户实体对象，如果request为null则返回原对象
     */
    private User convertToUser(UserUpdateRequest request, User existingUser) {
        if (request == null) {
            return existingUser;
        }
        
        // 只更新请求中提供的字段，保留原有字段
        if (request.getNickname() != null) {
            existingUser.setNickname(request.getNickname());
        }
        if (request.getEmail() != null) {
            existingUser.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            existingUser.setPhone(request.getPhone());
        }
        if (request.getAvatar() != null) {
            existingUser.setAvatar(request.getAvatar());
        }
        if (request.getIsVip() != null) {
            existingUser.setIsVip(request.getIsVip());
        }
        
        return existingUser;
    }

    /**
     * 删除用户
     * 
     * <p>根据用户ID删除用户，采用逻辑删除方式：</p>
     * <ul>
     *   <li>不会物理删除数据库记录</li>
     *   <li>将deleted字段设置为1</li>
     *   <li>更新时间会自动设置为当前时间</li>
     *   <li>删除后的用户不会出现在查询结果中</li>
     * </ul>
     * 
     * @param id 用户ID，路径参数，必填
     * @return 删除操作结果，如果用户不存在则返回错误信息
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户", description = "根据用户ID删除用户，采用逻辑删除方式，不会物理删除数据库记录")
    public Result<Void> deleteUser(
            @Parameter(description = "用户ID", required = true, example = "123") @PathVariable @NotNull(message = "用户ID不能为空") Long id) {
        try {
            log.info("REST请求 - 删除用户: id={}", id);
            
            // 验证用户是否存在
            User existingUser = userService.getUserById(id);
            if (existingUser == null) {
                return Result.error("用户不存在");
            }
            
            userService.deleteUser(id);
            
            log.info("用户删除成功: id={}", id);
            return Result.success();
            
        } catch (Exception e) {
            log.error("删除用户失败: id={}", id, e);
            return Result.error("删除用户失败: " + e.getMessage());
        }
    }

    /**
     * 分页查询用户列表
     * 
     * <p>分页查询用户列表，支持灵活的条件筛选：</p>
     * <ul>
     *   <li>如果不传查询条件，则查询所有用户</li>
     *   <li>如果传入查询条件，支持以下模糊搜索：</li>
     *   <ul>
     *     <li>用户名（username）</li>
     *     <li>昵称（nickname）</li>
     *     <li>邮箱（email）</li>
     *     <li>手机号（phone）</li>
     *     <li>用户状态（status）精确匹配</li>
     *   </ul>
     *   <li>结果按创建时间倒序排列</li>
     *   <li>返回分页信息和用户列表</li>
     * </ul>
     * 
     * @param condition 查询条件，可选参数，支持模糊搜索用户名、昵称、邮箱、手机号，或精确匹配状态
     * @param currentPage 当前页码，可选参数，默认为1
     * @param pageSize 页面大小，可选参数，默认为20
     * @return 分页用户列表，包含用户信息和分页信息
     */
    @GetMapping("/list")
    @Operation(summary = "分页查询用户列表", description = "分页查询用户列表，支持条件筛选。不传条件查询所有用户，传入条件支持模糊搜索用户名、昵称、邮箱、手机号，或精确匹配状态")
    public Result<PageResponse<UserResponse>> getUserList(
            @Parameter(description = "查询条件", required = false, example = "张三") @RequestParam(required = false) String condition,
            @Parameter(description = "当前页码", required = false, example = "1") @RequestParam(defaultValue = "1") Integer currentPage,
            @Parameter(description = "页面大小", required = false, example = "20") @RequestParam(defaultValue = "20") Integer pageSize) {
        try {
            log.info("REST请求 - 分页查询用户列表: condition={}, page={}, size={}", condition, currentPage, pageSize);
            
            // 查询用户列表
            java.util.List<User> userList = userService.getUserList(currentPage, pageSize, condition);
            
            // 转换为响应对象
            java.util.List<UserResponse> responses = userList.stream()
                    .map(this::convertToResponse)
                    .toList();
            
            // 查询总数
            Long total = userService.countUsers(condition);
            
            // 构建分页响应
            PageResponse<UserResponse> pageResponse = new PageResponse<>();
            pageResponse.setRecords(responses);
            pageResponse.setCurrentPage(currentPage);
            pageResponse.setPageSize(pageSize);
            pageResponse.setTotal(total);
            
            return Result.success(pageResponse);
            
        } catch (Exception e) {
            log.error("分页查询用户列表失败", e);
            return Result.error("分页查询用户列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户VIP状态
     * 
     * <p>查询用户的VIP状态信息，包括：</p>
     * <ul>
     *   <li>VIP状态：Y-是VIP，N-非VIP</li>
     *   <li>VIP过期时间：如果用户是VIP，显示过期时间</li>
     *   <li>VIP剩余天数：计算VIP剩余有效天数</li>
     * </ul>
     * 
     * @param userId 用户ID，路径参数，必填
     * @return VIP状态信息
     */
    @GetMapping("/{userId}/vip-status")
    @Operation(summary = "获取用户VIP状态", description = "查询用户的VIP状态信息，包括VIP状态、过期时间、剩余天数等")
    public Result<Map<String, Object>> getUserVipStatus(
            @Parameter(description = "用户ID", required = true, example = "123") @PathVariable @NotNull(message = "用户ID不能为空") Long userId) {
        try {
            log.info("REST请求 - 获取用户VIP状态: userId={}", userId);
            
            // 查询用户信息
            User user = userService.getUserById(userId);
            if (user == null) {
                return Result.error("用户不存在");
            }
            
            // 构建VIP状态信息
            Map<String, Object> vipStatus = new java.util.HashMap<>();
            vipStatus.put("userId", userId);
            vipStatus.put("isVip", user.getIsVip());
            vipStatus.put("vipExpireTime", user.getVipExpireTime());
            
            // 计算VIP剩余天数
            if ("Y".equals(user.getIsVip()) && user.getVipExpireTime() != null) {
                java.time.LocalDateTime now = java.time.LocalDateTime.now();
                if (user.getVipExpireTime().isAfter(now)) {
                    long daysRemaining = java.time.Duration.between(now, user.getVipExpireTime()).toDays();
                    vipStatus.put("daysRemaining", daysRemaining);
                    vipStatus.put("isExpired", false);
                } else {
                    vipStatus.put("daysRemaining", 0L);
                    vipStatus.put("isExpired", true);
                }
            } else {
                vipStatus.put("daysRemaining", 0L);
                vipStatus.put("isExpired", true);
            }
            
            return Result.success(vipStatus);
            
        } catch (Exception e) {
            log.error("获取用户VIP状态失败: userId={}", userId, e);
            return Result.error("获取用户VIP状态失败: " + e.getMessage());
        }
    }

    // =================== 内容转换相关私有方法 ===================

    /**
     * 将Content实体转换为UserContentResponse
     */
    private UserContentResponse convertContentToResponse(Content content) {
        UserContentResponse response = new UserContentResponse();
        
        response.setId(content.getId());
        response.setTitle(content.getTitle());
        response.setContent(content.getDescription());
        response.setContentType(content.getContentType());
        response.setCoverUrl(content.getCoverUrl());
        response.setContentData(content.getContentData());
        response.setTags(content.getTags());
        response.setStatus(content.getStatus());
        response.setReviewStatus(content.getReviewStatus());
        response.setViewCount(content.getViewCount());
        response.setLikeCount(content.getLikeCount());
        // 动态查询评论数量
        long commentCount = commentService.countTargetComments(content.getId(), "CONTENT");
        response.setCommentCount(commentCount);
        response.setFavoriteCount(content.getFavoriteCount());
        response.setShareCount(content.getShareCount());
        response.setPublishTime(content.getPublishTime());
        response.setCreateTime(content.getCreateTime());
        response.setUpdateTime(content.getUpdateTime());
        response.setDataSource("content");
        
        return response;
    }

    /**
     * 将SocialDynamic实体转换为UserContentResponse
     */
    private UserContentResponse convertDynamicToResponse(SocialDynamic dynamic) {
        UserContentResponse response = new UserContentResponse();
        
        response.setId(dynamic.getId());
        response.setTitle(dynamic.getTitle());
        response.setContent(dynamic.getContent());
        response.setContentType("SOCIAL_DYNAMIC");
        response.setImages(dynamic.getImages());
        response.setVideoUrl(dynamic.getVideoUrl());
        response.setStatus(dynamic.getStatus());
        response.setIsFree(dynamic.getIsFree());
        response.setPrice(dynamic.getPrice());
        response.setLikeCount(dynamic.getLikeCount());
        // 动态查询评论数量
        long commentCount = commentService.countTargetComments(dynamic.getId(), "DYNAMIC");
        response.setCommentCount(commentCount);
        response.setShareCount(dynamic.getShareCount());
        response.setShareTargetType(dynamic.getShareTargetType());
        response.setShareTargetId(dynamic.getShareTargetId());
        response.setShareTargetTitle(dynamic.getShareTargetTitle());
        response.setCreateTime(dynamic.getCreateTime());
        response.setUpdateTime(dynamic.getUpdateTime());
        response.setDataSource("social_dynamic");
        
        // 设置封面图片（从images中提取第一张）
        if (dynamic.getImages() != null && !dynamic.getImages().trim().isEmpty()) {
            response.setCoverUrl(extractFirstImageUrl(dynamic.getImages()));
        }
        
        return response;
    }

    /**
     * 从图片列表中提取第一张图片URL
     */
    private String extractFirstImageUrl(String images) {
        if (images == null || images.trim().isEmpty()) {
            return null;
        }
        
        try {
            // 尝试解析JSON数组格式
            if (images.trim().startsWith("[")) {
                String content = images.trim().substring(1, images.length() - 1);
                String[] imageArray = content.split(",");
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
     * 获取排序比较器
     */
    private Comparator<UserContentResponse> getComparator(String orderBy, String orderDirection) {
        Comparator<UserContentResponse> comparator;
        
        switch (orderBy.toLowerCase()) {
            case "updatetime":
                comparator = Comparator.comparing(UserContentResponse::getUpdateTime, 
                        Comparator.nullsLast(Comparator.naturalOrder()));
                break;
            case "likecount":
                comparator = Comparator.comparing(UserContentResponse::getLikeCount, 
                        Comparator.nullsLast(Comparator.naturalOrder()));
                break;
            case "viewcount":
                comparator = Comparator.comparing(UserContentResponse::getViewCount, 
                        Comparator.nullsLast(Comparator.naturalOrder()));
                break;
            case "createtime":
            default:
                comparator = Comparator.comparing(UserContentResponse::getCreateTime, 
                        Comparator.nullsLast(Comparator.naturalOrder()));
                break;
        }
        
        // 如果是降序，则反转比较器
        if ("DESC".equalsIgnoreCase(orderDirection)) {
            comparator = comparator.reversed();
        }
        
        return comparator;
    }
}
