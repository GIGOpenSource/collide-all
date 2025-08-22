# 🔐 Token注释文档

## 📋 概述

本文档说明项目中的Token校验机制，以及开发环境和生产环境的不同配置。

## 🏗️ 项目Token架构

### 技术栈
- **认证框架**: Sa-Token
- **Token类型**: JWT (JSON Web Token)
- **Token格式**: UUID
- **请求头**: `Authorization: Bearer <token>`

### 核心组件
1. **SaTokenConfig**: Token拦截器配置
2. **AuthController**: 登录/注册接口
3. **GlobalExceptionHandler**: Token异常处理
4. **application.yml**: Token配置参数

## 🔧 当前配置状态

### 开发环境 (当前状态)
- ✅ **Token校验已禁用**
- ✅ 所有接口无需登录即可访问
- ✅ 便于开发测试和调试
- ⚠️ **仅适用于开发环境**

### 生产环境 (需要启用)
- ❌ **Token校验已禁用** (需要启用)
- ❌ 需要登录验证的接口无法正常使用
- ❌ 存在安全风险

## 📁 相关文件

### 1. Token配置类
```
src/main/java/com/gig/collide/config/SaTokenConfig.java
```

**当前状态**: Token校验已注释掉
**生产环境**: 需要取消注释，启用token校验

### 2. Token配置文件
```
src/main/resources/application.yml
```

**关键配置**:
```yaml
sa-token:
  token-name: Authorization          # 请求头名称
  timeout: 2592000                  # Token有效期 (30天)
  activity-timeout: -1              # 活动超时时间
  is-concurrent: true               # 是否允许同一账号并发登录
  is-share: false                   # 是否共享token
  token-style: uuid                 # Token风格
  is-log: false                     # 是否打印日志
```

### 3. 登录接口
```
src/main/java/com/gig/collide/controller/AuthController.java
```

**接口地址**: `POST /api/v1/auth/login`
**返回格式**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "550e8400-e29b-41d4-a716-446655440000",
    "tokenName": "Authorization",
    "expiresIn": 2592000,
    "userInfo": { ... }
  }
}
```

### 4. 异常处理
```
src/main/java/com/gig/collide/config/GlobalExceptionHandler.java
```

**处理异常类型**:
- `NotLoginException.NOT_TOKEN`: 用户未登录
- `NotLoginException.INVALID_TOKEN`: 用户token无效
- `NotLoginException.TOKEN_TIMEOUT`: 用户token已过期
- `NotLoginException.BE_REPLACED`: 用户token已被顶下线
- `NotLoginException.KICK_OUT`: 用户token已被踢下线

## 🚀 环境切换指南

### 开发环境 → 生产环境

1. **启用Token校验**
   ```java
   // 在 SaTokenConfig.java 中取消注释
   registry.addInterceptor(new SaInterceptor(handle -> {
       StpUtil.checkLogin();
   }))
   ```

2. **验证配置**
   - 重启应用
   - 测试登录接口
   - 验证需要权限的接口

3. **检查日志**
   ```
   Sa-Token拦截器配置完成
   ```

### 生产环境 → 开发环境

1. **禁用Token校验**
   ```java
   // 在 SaTokenConfig.java 中注释掉
   /*
   registry.addInterceptor(new SaInterceptor(handle -> {
       StpUtil.checkLogin();
   }))
   */
   ```

2. **验证配置**
   - 重启应用
   - 测试无需登录的接口访问

3. **检查日志**
   ```
   ⚠️ 开发环境：Token校验已禁用，生产环境请启用！
   Sa-Token拦截器配置完成（开发环境：已禁用）
   ```

## 🔍 Token使用说明

### 1. 获取Token
```bash
# 登录获取token
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }'
```

### 2. 使用Token
```bash
# 在请求头中使用token
curl -X GET http://localhost:8080/api/v1/users/123 \
  -H "Authorization: Bearer 550e8400-e29b-41d4-a716-446655440000"
```

### 3. Token刷新
```bash
# 刷新token
curl -X POST http://localhost:8080/api/v1/auth/refresh-token \
  -H "Authorization: Bearer 550e8400-e29b-41d4-a716-446655440000"
```

## ⚠️ 安全注意事项

### 开发环境
- ✅ 可以禁用Token校验，便于开发
- ⚠️ 不要在生产环境使用此配置
- ⚠️ 注意保护敏感数据

### 生产环境
- ✅ 必须启用Token校验
- ✅ 使用HTTPS传输
- ✅ 定期更换Token密钥
- ✅ 监控Token使用情况

## 🐛 常见问题

### 1. Token验证失败
**问题**: `用户token无效`
**解决**: 
- 检查token格式是否正确
- 确认token是否过期
- 验证请求头格式

### 2. 接口访问被拒绝
**问题**: `用户未登录`
**解决**:
- 确认是否已登录
- 检查token是否有效
- 验证接口权限配置

### 3. Token过期
**问题**: `用户token已过期`
**解决**:
- 重新登录获取新token
- 使用refresh-token接口刷新

## 📝 更新日志

### v1.0.0 (2024-01-20)
- 初始Token架构设计
- 集成Sa-Token框架
- 配置开发环境禁用Token校验

### 待更新
- 生产环境Token校验启用
- Token安全策略优化
- 监控和日志完善

---

**注意**: 本文档会随着项目发展持续更新，请定期查看最新版本。
