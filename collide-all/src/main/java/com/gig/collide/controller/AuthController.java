package com.gig.collide.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.gig.collide.Apientry.api.common.response.Result;
import com.gig.collide.domain.User;
import com.gig.collide.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
     * 用户登录
     * 
     * @param username 用户名
     * @param password 密码
     * @return 登录结果，包含token和用户信息
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户登录并返回认证token")
    public Result<Map<String, Object>> login(
            @Parameter(description = "用户名", required = true) @RequestParam String username,
            @Parameter(description = "密码", required = true) @RequestParam String password) {
        
        try {
            log.info("用户登录请求: username={}", username);
            
            // 1. 根据用户名查询用户
            User user = userService.getUserByUsername(username);
            if (user == null) {
                log.warn("登录失败: 用户不存在, username={}", username);
                return Result.error("用户名或密码错误");
            }
            
            // 2. 检查用户状态
            if (!"active".equals(user.getStatus())) {
                log.warn("登录失败: 用户状态异常, username={}, status={}", username, user.getStatus());
                return Result.error("用户账号已被禁用");
            }
            
            // 3. 验证密码（这里使用默认密码进行演示）
            // 在实际项目中，应该从数据库获取加密后的密码进行验证
            String defaultPassword = "password123";
            if (!defaultPassword.equals(password)) {
                log.warn("登录失败: 密码错误, username={}", username);
                return Result.error("用户名或密码错误");
            }
            
            // 4. 更新登录信息
            user.updateLoginInfo();
            userService.updateUser(user);
            
            // 5. 使用Sa-Token进行登录
            StpUtil.login(user.getId());
            String token = StpUtil.getTokenValue();
            
            // 6. 构建响应数据
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("token", token);
            responseData.put("tokenName", "Authorization");
            responseData.put("expiresIn", 2592000); // 30天
            responseData.put("userInfo", buildUserInfo(user));
            
            log.info("用户登录成功: username={}, userId={}", username, user.getId());
            return Result.success(responseData);
            
        } catch (Exception e) {
            log.error("登录异常", e);
            return Result.error("登录失败: " + e.getMessage());
        }
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
        return userInfo;
    }
}
