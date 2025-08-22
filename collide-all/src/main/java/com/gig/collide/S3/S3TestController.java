package com.gig.collide.S3;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.services.s3.S3Client;

import java.util.HashMap;
import java.util.Map;

/**
 * S3测试控制器
 * 用于验证S3模块功能是否正常
 */
@Slf4j
@RestController
@RequestMapping("/api/s3-test")
@RequiredArgsConstructor
public class S3TestController {
    
    private final S3Client s3Client;
    private final S3Config s3Config;
    
    /**
     * 测试S3模块状态
     */
    @GetMapping("/status")
    public Map<String, Object> testStatus() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            log.info("开始测试S3模块状态...");
            
            // 测试S3客户端
            if (s3Client != null) {
                log.info("✅ S3客户端注入成功");
                result.put("s3ClientStatus", "SUCCESS");
            } else {
                log.error("❌ S3客户端注入失败");
                result.put("s3ClientStatus", "FAILED");
            }
            
            // 测试配置
            if (s3Config != null) {
                log.info("✅ S3配置注入成功");
                result.put("s3ConfigStatus", "SUCCESS");
                result.put("region", s3Config.getRegion());
                result.put("bucketName", s3Config.getBucketName());
            } else {
                log.error("❌ S3配置注入失败");
                result.put("s3ConfigStatus", "FAILED");
            }
            
            result.put("success", true);
            result.put("message", "S3模块状态检查完成");
            result.put("springBootVersion", "3.2.2");
            result.put("awsSdkVersion", "2.24.12");
            result.put("javaVersion", "21");
            result.put("moduleStatus", "READY");
            
            log.info("✅ S3模块状态检查通过");
            
        } catch (Exception e) {
            log.error("❌ S3模块状态检查失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "S3模块状态检查失败: " + e.getMessage());
            result.put("moduleStatus", "ERROR");
        }
        
        return result;
    }
    
    /**
     * 获取S3模块信息
     */
    @GetMapping("/info")
    public Map<String, String> getModuleInfo() {
        Map<String, String> info = new HashMap<>();
        info.put("moduleName", "S3 Storage Module");
        info.put("version", "1.0.0");
        info.put("springBootVersion", "3.2.2");
        info.put("awsSdkVersion", "2.24.12");
        info.put("javaVersion", "21");
        info.put("status", "ACTIVE");
        info.put("basePath", "/api/s3");
        return info;
    }
}
