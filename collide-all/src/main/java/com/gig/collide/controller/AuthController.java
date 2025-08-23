package com.gig.collide.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.gig.collide.Apientry.api.common.response.Result;
import com.gig.collide.Apientry.api.auth.request.LoginRequest;
import com.gig.collide.domain.User;
import com.gig.collide.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 * 提供用户登录、登出、token校验等功能
 *
 * @author GIG Team
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "认证管理", description = "用户认证相关接口，包括登录、登出、token校验等")
public class AuthController {

    private final UserService userService;

    /**
     * 用户登录 - 支持form-data参数
     * 
     * @param username 用户名
     * @param password 密码
     * @return 登录结果，包含token和用户信息
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录 (form-data)", description = "用户登录并返回认证token，支持form-data参数")
    public Result<Map<String, Object>> login(
            @Parameter(description = "用户名", required = true) @RequestParam String username,
            @Parameter(description = "密码", required = true) @RequestParam String password) {
        
        return performLogin(username, password);
    }

    /**
     * 用户登录 - 支持JSON请求体
     * 
     * @param loginRequest 登录请求对象
     * @return 登录结果，包含token和用户信息
     */
    @PostMapping(value = "/login", consumes = "application/json")
    @Operation(summary = "用户登录 (JSON)", description = "用户登录并返回认证token，支持JSON请求体")
    public Result<Map<String, Object>> loginWithJson(
            @Valid @RequestBody LoginRequest loginRequest) {
        
        return performLogin(loginRequest.getUsername(), loginRequest.getPassword());
    }

    /**
     * 执行登录逻辑的通用方法
     * 支持用户不存在时自动注册
     * 
     * @param username 用户名
     * @param password 密码
     * @return 登录结果，包含token和用户信息
     */
    private Result<Map<String, Object>> performLogin(String username, String password) {
        try {
            log.info("用户登录请求: username={}", username);
            
            // 1. 根据用户名查询用户
            User user = userService.getUserByUsername(username);
            boolean isNewUser = false;
            
            if (user == null) {
                // 用户不存在，自动注册新用户
                log.info("用户不存在，开始自动注册: username={}", username);
                user = createNewUser(username, password);
                if (user == null) {
                    log.error("自动注册失败: username={}", username);
                    return Result.error("用户注册失败，请稍后重试");
                }
                isNewUser = true;
                log.info("用户自动注册成功: username={}, userId={}", username, user.getId());
            }
            
            // 2. 检查用户状态
            if (!"active".equals(user.getStatus())) {
                log.warn("登录失败: 用户状态异常, username={}, status={}", username, user.getStatus());
                return Result.error("用户账号已被禁用");
            }
            
            // 3. 验证密码 - 统一逻辑：用户输入密码与数据库存储密码比对
            if (!password.equals(user.getPasswordHash())) {
                log.warn("登录失败: 密码错误, username={}, 输入密码=[{}], 存储密码=[{}]", 
                        username, password, user.getPasswordHash());
                return Result.error("用户名或密码错误");
            }
            
            // 4. 更新登录信息（新用户在创建时已设置，老用户需要更新）
            if (!isNewUser) {
                user.updateLoginInfo();
                userService.updateUser(user);
            }
            
            // 5. 使用Sa-Token进行登录
            StpUtil.login(user.getId());
            String token = StpUtil.getTokenValue();
            
            // 6. 构建响应数据
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("token", token);
            responseData.put("tokenName", "Authorization");
            responseData.put("expiresIn", 2592000); // 30天
            responseData.put("userInfo", buildUserInfo(user));
            responseData.put("isNewUser", isNewUser); // 标识是否为新注册用户
            
            log.info("用户登录成功: username={}, userId={}, isNewUser={}", username, user.getId(), isNewUser);
            return Result.success(responseData);
            
        } catch (Exception e) {
            log.error("登录异常", e);
            return Result.error("登录失败: " + e.getMessage());
        }
    }

    /**
     * 创建新用户
     * 
     * @param username 用户名
     * @param password 密码
     * @return 创建的用户对象，失败返回null
     */
    private User createNewUser(String username, String password) {
        try {
            User newUser = new User();
            
            // 基本信息
            newUser.setUsername(username);
            newUser.setNickname(username); // 默认昵称为用户名
            newUser.setPasswordHash(password); // 实际应该加密存储
            
            // 默认状态和角色
            newUser.setStatus("active");
            newUser.setRole("user");
            
            // 默认头像
            newUser.setAvatar("https://example.com/default-avatar.jpg");
            
            // 初始化统计字段
            newUser.setFollowerCount(0L);
            newUser.setFollowingCount(0L);
            newUser.setContentCount(0L);
            newUser.setLikeCount(0L);
            newUser.setInvitedCount(0L);
            
            // VIP相关
            newUser.setIsVip("N");
            
            // 登录信息
            newUser.setLoginCount(1L);
            newUser.setLastLoginTime(java.time.LocalDateTime.now());
            
            // 时间戳
            newUser.setCreateTime(java.time.LocalDateTime.now());
            newUser.setUpdateTime(java.time.LocalDateTime.now());
            
            // 生成邀请码（可选）
            newUser.setInviteCode(generateInviteCode(username));
            
            // 保存到数据库
            User savedUser = userService.createUser(newUser);
            
            log.info("新用户创建成功: username={}, userId={}", username, savedUser.getId());
            return savedUser;
            
        } catch (Exception e) {
            log.error("创建新用户失败: username={}, error={}", username, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 生成邀请码
     * 
     * @param username 用户名
     * @return 邀请码
     */
    private String generateInviteCode(String username) {
        // 简单的邀请码生成逻辑：用户名 + 时间戳后6位
        long timestamp = System.currentTimeMillis();
        return username.toUpperCase() + String.valueOf(timestamp).substring(7);
    }

    /**
     * 用户登出
     * 
     * @return 登出结果
     */
    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "用户登出并清除token")
    public Result<String> logout() {
        try {
            // 检查是否已登录
            if (!StpUtil.isLogin()) {
                return Result.error("用户未登录");
            }
            
            Long userId = StpUtil.getLoginIdAsLong();
            log.info("用户登出: userId={}", userId);
            
            // 使用Sa-Token进行登出
            StpUtil.logout();
            
            return Result.success("登出成功");
            
        } catch (Exception e) {
            log.error("登出异常", e);
            return Result.error("登出失败: " + e.getMessage());
        }
    }

    /**
     * 获取当前用户信息
     * 
     * @return 当前登录用户信息
     */
    @GetMapping("/current-user")
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    public Result<Map<String, Object>> getCurrentUser() {
        try {
            // 检查是否已登录
            if (!StpUtil.isLogin()) {
                return Result.error("用户未登录");
            }
            
            Long userId = StpUtil.getLoginIdAsLong();
            User user = userService.getUserById(userId);

            if (user == null) {
                return Result.error("用户不存在");
            }
            
            Map<String, Object> userInfo = buildUserInfo(user);
            return Result.success(userInfo);
            
        } catch (Exception e) {
            log.error("获取当前用户信息异常", e);
            return Result.error("获取用户信息失败: " + e.getMessage());
        }
    }

    /**
     * 校验token有效性
     * 
     * @return token校验结果
     */
    @GetMapping("/check-token")
    @Operation(summary = "校验token", description = "校验当前token是否有效")
    public Result<Map<String, Object>> checkToken() {
        try {
            boolean isLogin = StpUtil.isLogin();
            
            Map<String, Object> result = new HashMap<>();
            result.put("valid", isLogin);
            
            if (isLogin) {
                Long userId = StpUtil.getLoginIdAsLong();
                result.put("userId", userId);
                result.put("token", StpUtil.getTokenValue());
                result.put("expiresIn", StpUtil.getTokenTimeout());
            }
            
            return Result.success(result);
            
        } catch (Exception e) {
            log.error("校验token异常", e);
            return Result.error("token校验失败: " + e.getMessage());
        }
    }

    /**
     * 刷新token
     * 
     * @return 新的token信息
     */
    @PostMapping("/refresh-token")
    @Operation(summary = "刷新token", description = "刷新当前token")
    public Result<Map<String, Object>> refreshToken() {
        try {
            // 检查是否已登录
            if (!StpUtil.isLogin()) {
                return Result.error("用户未登录");
            }
            
            Long userId = StpUtil.getLoginIdAsLong();
            
            // 使用Sa-Token刷新token
            StpUtil.renewTimeout(2592000); // 延长30天
            String newToken = StpUtil.getTokenValue();
            
            Map<String, Object> result = new HashMap<>();
            result.put("token", newToken);
            result.put("expiresIn", 2592000);
            result.put("userId", userId);
            
            log.info("token刷新成功: userId={}", userId);
            return Result.success(result);
            
        } catch (Exception e) {
            log.error("刷新token异常", e);
            return Result.error("刷新token失败: " + e.getMessage());
        }
    }

    /**
     * 构建用户信息响应
     * 
     * @param user 用户实体
     * @return 用户信息Map
     */
    private Map<String, Object> buildUserInfo(User user) {
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("nickname", user.getNickname());
        userInfo.put("email", user.getEmail());
        userInfo.put("phone", user.getPhone());
        userInfo.put("avatar", user.getAvatar());
        userInfo.put("status", user.getStatus());
        userInfo.put("isVip", user.getIsVip());
        userInfo.put("lastLoginTime", user.getLastLoginTime());
        userInfo.put("loginCount", user.getLoginCount());
        userInfo.put("createTime", user.getCreateTime());
        
        // 添加统计信息
        userInfo.put("followerCount", user.getFollowerCount() != null ? user.getFollowerCount() : 0L);
        userInfo.put("followingCount", user.getFollowingCount() != null ? user.getFollowingCount() : 0L);
        userInfo.put("contentCount", user.getContentCount() != null ? user.getContentCount() : 0L);
        userInfo.put("likeCount", user.getLikeCount() != null ? user.getLikeCount() : 0L);
        
        log.debug("用户统计信息: userId={}, followerCount={}, followingCount={}, contentCount={}, likeCount={}", 
                user.getId(), user.getFollowerCount(), user.getFollowingCount(), user.getContentCount(), user.getLikeCount());
        
        return userInfo;
    }
}
