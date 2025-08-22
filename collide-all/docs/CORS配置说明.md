# CORS 跨域配置说明

## 概述

本项目已配置 CORS（跨域资源共享），允许多域名访问 API 接口。

## 配置内容

### 1. ApplicationConfig 配置

在 `ApplicationConfig` 类中添加了以下配置：

- **允许所有域名访问**: `allowedOriginPatterns("*")`
- **允许的请求方法**: GET, POST, PUT, DELETE, OPTIONS, HEAD, PATCH
- **允许所有请求头**: `allowedHeaders("*")`
- **允许发送凭证**: `allowCredentials(true)`
- **预检请求缓存**: 3600秒

### 2. CorsFilter 过滤器

创建了专门的 CORS 过滤器，确保所有请求都包含正确的跨域响应头：

```java
response.setHeader("Access-Control-Allow-Origin", "*");
response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD, PATCH");
response.setHeader("Access-Control-Allow-Headers", "*");
response.setHeader("Access-Control-Allow-Credentials", "true");
response.setHeader("Access-Control-Max-Age", "3600");
```

## 支持的域名

- ✅ 允许所有域名访问（`*`）
- ✅ 支持本地开发环境
- ✅ 支持生产环境
- ✅ 支持移动端应用
- ✅ 支持第三方集成

## 支持的请求方法

- GET - 获取数据
- POST - 创建数据
- PUT - 更新数据
- DELETE - 删除数据
- OPTIONS - 预检请求
- HEAD - 获取响应头
- PATCH - 部分更新

## 支持的请求头

- 允许所有自定义请求头
- 支持 Content-Type
- 支持 Authorization
- 支持其他自定义头部

## 测试方法

### 1. 使用浏览器开发者工具

```javascript
// 在浏览器控制台中测试
fetch('http://localhost:8080/collide-all/api/v1/ads/list', {
    method: 'GET',
    headers: {
        'Content-Type': 'application/json'
    }
})
.then(response => response.json())
.then(data => console.log(data));
```

### 2. 使用 curl 命令

```bash
# 测试预检请求
curl -X OPTIONS http://localhost:8080/collide-all/api/v1/ads/list \
  -H "Origin: http://example.com" \
  -H "Access-Control-Request-Method: GET" \
  -H "Access-Control-Request-Headers: Content-Type" \
  -v

# 测试实际请求
curl -X GET http://localhost:8080/collide-all/api/v1/ads/list \
  -H "Origin: http://example.com" \
  -H "Content-Type: application/json" \
  -v
```

### 3. 使用 Postman

在 Postman 中设置请求时，确保：
- 请求 URL 正确
- 请求方法正确
- 请求头包含必要的认证信息

## 注意事项

1. **安全性**: 当前配置允许所有域名访问，生产环境建议限制特定域名
2. **性能**: 预检请求缓存时间为1小时，减少重复预检
3. **兼容性**: 支持现代浏览器和移动端应用
4. **调试**: 可在浏览器开发者工具的 Network 标签中查看 CORS 相关信息

## 常见问题

### Q: 为什么仍然出现跨域错误？
A: 检查以下几点：
- 确保服务器已启动
- 检查请求 URL 是否正确
- 确认请求方法是否在允许列表中
- 检查请求头是否合规

### Q: 如何限制特定域名访问？
A: 修改配置中的 `allowedOriginPatterns`：
```java
.allowedOriginPatterns("https://yourdomain.com", "https://app.yourdomain.com")
```

### Q: 如何处理预检请求？
A: 过滤器已自动处理 OPTIONS 预检请求，返回 200 状态码。

## 更新日志

### v1.0.0 (2023-12-21)
- 初始 CORS 配置
- 支持所有域名访问
- 添加 CORS 过滤器
- 完整的跨域支持
