package com.gig.collide.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * CORS 跨域过滤器
 * 确保所有请求都能正确处理跨域访问
 *
 * @author Collide Team
 * @since 1.0.0
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorsFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletResponse response = (HttpServletResponse) res;
        HttpServletRequest request = (HttpServletRequest) req;
        
        // 设置允许所有域名访问
        response.setHeader("Access-Control-Allow-Origin", "*");
        
        // 设置允许的请求方法
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD, PATCH");
        
        // 设置允许的请求头
        response.setHeader("Access-Control-Allow-Headers", "*");
        
        // 注意：当 Access-Control-Allow-Origin 为 * 时，不能设置 Access-Control-Allow-Credentials 为 true
        // response.setHeader("Access-Control-Allow-Credentials", "true");
        
        // 设置预检请求缓存时间
        response.setHeader("Access-Control-Max-Age", "3600");
        
        // 设置暴露的响应头
        response.setHeader("Access-Control-Expose-Headers", "Content-Length, Access-Control-Allow-Origin, Access-Control-Allow-Headers, Cache-Control, Content-Language, Content-Type, Expires, Last-Modified, Pragma");
        
        // 处理预检请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }
        
        chain.doFilter(req, res);
    }

    @Override
    public void init(FilterConfig filterConfig) {
        log.info("CORS 过滤器初始化完成");
    }

    @Override
    public void destroy() {
        log.info("CORS 过滤器销毁");
    }
}
