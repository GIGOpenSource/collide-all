package com.gig.collide.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security配置类
 * 禁用默认的安全配置，使用Sa-Token进行认证
 *
 * @author GIG Team
 * @since 1.0.0
 */
@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * 配置SecurityFilterChain
     * 禁用默认的安全配置，允许所有请求通过
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.info("配置Spring Security...");
        
        http
            // 禁用CSRF保护
            .csrf(AbstractHttpConfigurer::disable)
            // 禁用默认登录页面
            .formLogin(AbstractHttpConfigurer::disable)
            // 禁用HTTP Basic认证
            .httpBasic(AbstractHttpConfigurer::disable)
            // 禁用默认登出
            .logout(AbstractHttpConfigurer::disable)
            // 允许所有请求
            .authorizeHttpRequests(authz -> authz
                .anyRequest().permitAll()
            );
        
        log.info("Spring Security配置完成");
        return http.build();
    }
}
