package com.gig.collide.controller;

import com.gig.collide.Apientry.api.common.response.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 测试控制器
 * 用于验证认证和异常处理
 *
 * @author GIG Team
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/test")
public class TestController {

    /**
     * 测试需要登录的接口
     */
    @GetMapping("/auth")
    public Result<String> testAuth() {
        log.info("测试认证接口被调用");
        return Result.success("认证成功");
    }

    /**
     * 测试不需要登录的接口
     */
    @GetMapping("/public")
    public Result<String> testPublic() {
        log.info("测试公开接口被调用");
        return Result.success("公开接口访问成功");
    }
}
