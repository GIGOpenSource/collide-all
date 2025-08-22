package com.gig.collide.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查控制器
 *
 * @author Collide Team
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/health")
public class HealthController {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 健康检查
     */
    @GetMapping
    public Map<String, Object> health() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "UP");
        result.put("timestamp", System.currentTimeMillis());
        result.put("application", "Collide-All");
        
        // 检查数据库连接
        try (Connection connection = dataSource.getConnection()) {
            result.put("database", "UP");
            log.info("数据库连接正常");
        } catch (Exception e) {
            result.put("database", "DOWN");
            result.put("database_error", e.getMessage());
            log.error("数据库连接失败", e);
        }
        
        // 检查Redis连接
        try {
            redisTemplate.opsForValue().get("health_check");
            result.put("redis", "UP");
            log.info("Redis连接正常");
        } catch (Exception e) {
            result.put("redis", "DOWN");
            result.put("redis_error", e.getMessage());
            log.error("Redis连接失败", e);
        }
        
        return result;
    }

    /**
     * 简单健康检查
     */
    @GetMapping("/simple")
    public String simpleHealth() {
        return "OK";
    }
}
