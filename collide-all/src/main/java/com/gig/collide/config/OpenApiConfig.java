package com.gig.collide.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI配置类 - 模块化文档配置
 * 按功能模块分组配置Swagger文档信息
 *
 * @author GIG Team
 * @version 2.0.0 (模块化版本)
 * @since 2024-01-31
 */
@Slf4j
@Configuration
public class OpenApiConfig {

    /**
     * 配置OpenAPI基础信息
     */
    @Bean
    public OpenAPI customOpenAPI() {
        log.info("配置OpenAPI文档基础信息...");
        
        return new OpenAPI()
                .info(new Info()
                        .title("Collide-All API 文档")
                        .description("Collide-All 系统模块化API文档，按功能模块分组展示，提供完整的用户管理、内容管理、订单管理等功能接口")
                        .version("2.0.0")
                        .contact(new Contact()
                                .name("GIG Team")
                                .email("support@gig.com")
                                .url("https://www.gig.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080/collide-all")
                                .description("开发环境服务器"),
                        new Server()
                                .url("https://api.collide.com")
                                .description("生产环境服务器")
                ));
    }

    /**
     * 1. 我的模块 - 个人中心相关功能
     */
    @Bean
    public GroupedOpenApi myModuleApi() {
        log.info("配置我的模块API分组...");
        return GroupedOpenApi.builder()
                .group("01-我的模块")
                .displayName("我的模块")
                .pathsToMatch(
                        "/api/v1/vip/**",           // VIP充值功能
                        "/api/v1/users/**",         // 用户信息管理
                        "/api/v1/inform/**"         // 消息通知管理
                )
                .build();
    }

    /**
     * 2. 发现页播放页模块 - 内容浏览和播放功能
     */
    @Bean
    public GroupedOpenApi discoveryPlayApi() {
        log.info("配置发现页播放页模块API分组...");
        return GroupedOpenApi.builder()
                .group("02-发现页播放页")
                .displayName("发现页播放页")
                .pathsToMatch(
                        "/api/v1/content/core/**",  // 内容查询
                        "/api/v1/content/chapters/**", // 章节管理
                        "/api/v1/content/payment/**",  // 付费内容
                        "/api/v1/content/purchase/**", // 内容购买
                        "/api/v1/favorite/**",      // 收藏功能
                        "/api/v1/search/**"         // 搜索功能
                )
                .build();
    }

    /**
     * 3. 社区接口模块 - 社交互动功能
     */
    @Bean
    public GroupedOpenApi communityApi() {
        log.info("配置社区接口模块API分组...");
        return GroupedOpenApi.builder()
                .group("03-社区接口")
                .displayName("社区接口")
                .pathsToMatch(
                        "/api/v1/follow/**",        // 关注功能
                        "/api/v1/like/**",          // 点赞功能
                        "/api/v1/comments/**",      // 评论功能
                        "/api/v1/social/**"         // 社交动态
                )
                .build();
    }

    /**
     * 4. 消息中心模块 - 消息和通知功能
     */
    @Bean
    public GroupedOpenApi messageApi() {
        log.info("配置消息中心模块API分组...");
        return GroupedOpenApi.builder()
                .group("04-消息中心")
                .displayName("消息中心")
                .pathsToMatch(
                        "/api/v1/messages/**",      // 消息管理
                        "/api/v1/sessions/**",      // 消息会话
                        "/api/v1/message-settings/**" // 消息设置
                )
                .build();
    }

    /**
     * 5. Profile客态模块 - 用户主页查看功能
     */
    @Bean
    public GroupedOpenApi profileApi() {
        log.info("配置Profile客态模块API分组...");
        return GroupedOpenApi.builder()
                .group("05-Profile客态")
                .displayName("Profile客态")
                .pathsToMatch(
                        "/api/v1/users/profile/**", // 用户主页信息
                        "/api/v1/users/*/public/**" // 公开信息查看
                )
                .build();
    }

    /**
     * 6. 商业功能模块 - 订单、支付、商品管理
     */
    @Bean
    public GroupedOpenApi businessApi() {
        log.info("配置商业功能模块API分组...");
        return GroupedOpenApi.builder()
                .group("06-商业功能")
                .displayName("商业功能")
                .pathsToMatch(
                        "/api/v1/orders/**",        // 订单管理
                        "/api/v1/payment/**",       // 支付功能
                        "/api/v1/goods/**",         // 商品管理
                        "/api/v1/wallet/**"         // 钱包功能
                )
                .build();
    }

    /**
     * 7. 内容管理模块 - 内容创建和管理功能
     */
    @Bean
    public GroupedOpenApi contentManagementApi() {
        log.info("配置内容管理模块API分组...");
        return GroupedOpenApi.builder()
                .group("07-内容管理")
                .displayName("内容管理")
                .pathsToMatch(
                        "/api/v1/categories/**",    // 分类管理
                        "/api/v1/tag/**",           // 标签管理
                        "/api/v1/ads/**"            // 广告管理
                )
                .build();
    }

    /**
     * 8. 系统功能模块 - 认证、健康检查等基础功能
     */
    @Bean
    public GroupedOpenApi systemApi() {
        log.info("配置系统功能模块API分组...");
        return GroupedOpenApi.builder()
                .group("08-系统功能")
                .displayName("系统功能")
                .pathsToMatch(
                        "/api/v1/auth/**",          // 认证授权
                        "/api/v1/health/**",        // 健康检查
                        "/api/v1/test/**",          // 测试接口
                        "/api/v1/task/**"           // 任务管理
                )
                .build();
    }

    /**
     * 9. 完整API - 所有接口的完整视图
     */
    @Bean
    public GroupedOpenApi allApi() {
        log.info("配置完整API分组...");
        return GroupedOpenApi.builder()
                .group("99-完整API")
                .displayName("完整API")
                .pathsToMatch("/api/v1/**")
                .build();
    }
}
