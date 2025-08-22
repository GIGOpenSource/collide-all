package com.gig.collide.config;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token配置类
 * 配置全局拦截器，验证用户登录状态
 * 
 * 开发环境说明：
 * - 当前已注释掉token校验逻辑，便于开发测试
 * - 生产环境需要取消注释，启用token校验
 * - 详见Token注释文档
 *
 * @author GIG Team
 * @since 1.0.0
 */
@Slf4j
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    /**
     * 注册Sa-Token拦截器
     * 拦截需要登录验证的接口
     * 
     * 开发环境：已注释掉token校验
     * 生产环境：需要取消注释，启用token校验
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        log.info("配置Sa-Token拦截器...");
        log.warn("⚠️ 开发环境：Token校验已禁用，生产环境请启用！");
        
        // =================== 开发环境：Token校验已禁用 ===================
        // 生产环境需要取消注释以下代码，启用token校验
        
        /*
        // 注册Sa-Token拦截器，拦截需要登录验证的接口
        registry.addInterceptor(new SaInterceptor(handle -> {
            try {
                // 检查用户是否已登录
                StpUtil.checkLogin();
            } catch (NotLoginException e) {
                // 记录日志但不重新抛出，让全局异常处理器处理
                log.warn("用户未登录或token无效: {}", e.getMessage());
                throw e;
            }
        }))
                .addPathPatterns(
                        // 需要登录验证的接口路径
                        //"/api/v1/users/**",
                        "/api/v1/content/**", 
                        "/api/v1/xxwf/**",        // 添加XXWF接口保护
                        "/api/v1/orders/**",
                        "/api/v1/payment/**",
                        "/api/v1/message/**",
                        "/api/v1/search/**",
                        "/api/v1/favorite/**",
                        "/api/v1/follow/**",
                        "/api/v1/like/**",
                        "/api/v1/comment/**",
                        "/api/v1/tag/**",
                        "/api/v1/social/**",
                        "/api/v1/task/**",
                        "/api/v1/goods/**",
                        "/api/v1/category/**",
                        "/api/v1/ads/**",
                        "/api/v1/test/auth"
                )
                .excludePathPatterns(
                        // 不需要登录验证的接口路径
                        "/api/v1/auth/login",
                        "/api/v1/auth/register",
                        "/api/v1/health/**",
                        "/api/v1/test/public",
                        "/doc.html",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/webjars/**",
                        "/favicon.ico",
                        // API文档相关路径
                        "/api-docs/**",
                        "/api-docs.html",
                        "/static/**"
                );
        */
        
        log.info("Sa-Token拦截器配置完成（开发环境：已禁用）");
    }
}
