package com.gig.collide.config;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import com.gig.collide.Apientry.api.common.response.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 * 处理Sa-Token认证异常和其他业务异常
 *
 * @author GIG Team
 * @since 1.0.0
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理未登录异常
     */
    @ExceptionHandler(NotLoginException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<String> handleNotLoginException(NotLoginException e) {
        log.warn("用户未登录或token无效: {}", e.getMessage());
        
        // 根据异常类型返回不同的错误信息
        String message;
        switch (e.getType()) {
            case NotLoginException.NOT_TOKEN:
                message = "用户未登录";
                break;
            case NotLoginException.INVALID_TOKEN:
                message = "用户token无效";
                break;
            case NotLoginException.TOKEN_TIMEOUT:
                message = "用户token已过期";
                break;
            case NotLoginException.BE_REPLACED:
                message = "用户token已被顶下线";
                break;
            case NotLoginException.KICK_OUT:
                message = "用户token已被踢下线";
                break;
            default:
                message = "用户未登录";
                break;
        }
        
        return Result.error(401, message);
    }

    /**
     * 处理无权限异常
     */
    @ExceptionHandler(NotPermissionException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<String> handleNotPermissionException(NotPermissionException e) {
        log.warn("用户无权限访问: {}", e.getMessage());
        return Result.error(403, "用户无权限访问");
    }

    /**
     * 处理无角色异常
     */
    @ExceptionHandler(NotRoleException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<String> handleNotRoleException(NotRoleException e) {
        log.warn("用户角色不匹配: {}", e.getMessage());
        return Result.error(403, "用户角色不匹配");
    }

    /**
     * 处理其他异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<String> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.error(500, "系统内部错误");
    }
}
