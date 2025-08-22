# Collide-All API接口文档总览

## 项目信息
- **项目名称**: Collide-All
- **版本**: 1.0.0
- **基础URL**: `http://localhost:8080/collide-all`
- **文档更新时间**: 2024-01-01

## 控制器列表

### 基础管理模块

| 控制器 | 路径 | 描述 | 文档链接 |
|--------|------|------|----------|
| HealthController | `/health` | 系统健康检查 | [健康检查API](./HealthController-API文档.md) |
| CategoryController | `/api/v1/categories` | 分类管理 | [分类管理API](./CategoryController-API文档.md) |

### 内容管理模块

| 控制器 | 路径 | 描述 | 文档链接 |
|--------|------|------|----------|
| ContentController | `/api/v1/content/core` | 内容管理 | [内容管理API](./ContentController-API文档.md) |
| ContentChapterController | `/api/v1/content/chapters` | 内容章节管理 | [章节管理API](./ContentChapterController-API文档.md) |
| ContentPaymentController | `/api/v1/content/payment` | 内容付费管理 | [付费管理API](./ContentPaymentController-API文档.md) |
| ContentPurchaseController | `/api/v1/content/purchase` | 内容购买管理 | [购买管理API](./ContentPurchaseController-API文档.md) |

### 用户交互模块

| 控制器 | 路径 | 描述 | 文档链接 |
|--------|------|------|----------|
| CommentController | `/api/v1/comments` | 评论管理 | [评论管理API](./CommentController-API文档.md) |
| LikeController | `/api/v1/like` | 点赞管理 | [点赞管理API](./LikeController-API文档.md) |
| FavoriteController | `/api/v1/favorite` | 收藏管理 | [收藏管理API](./FavoriteController-API文档.md) |
| FollowController | `/api/v1/follow` | 关注管理 | [关注管理API](./FollowController-API文档.md) |

### 社交功能模块

| 控制器 | 路径 | 描述 | 文档链接 |
|--------|------|------|----------|
| SocialDynamicController | `/api/v1/social/dynamics` | 社交动态管理 | [社交动态API](./SocialDynamicController-API文档.md) |
| MessageController | `/api/v1/messages` | 消息管理 | [消息管理API](./MessageController-API文档.md) |
| MessageSessionController | `/api/v1/message-sessions` | 消息会话管理 | [会话管理API](./MessageSessionController-API文档.md) |

### 商业功能模块

| 控制器 | 路径 | 描述 | 文档链接 |
|--------|------|------|----------|
| AdsController | `/api/v1/ads` | 广告管理 | [广告管理API](./AdsController-API文档.md) |
| GoodsController | `/api/v1/goods` | 商品管理 | [商品管理API](./GoodsController-API文档.md) |
| OrderController | `/api/v1/orders` | 订单管理 | [订单管理API](./OrderController-API文档.md) |
| PaymentController | `/api/v1/payment` | 支付管理 | [支付管理API](./PaymentController-API文档.md) |

### 标签与搜索模块

| 控制器 | 路径 | 描述 | 文档链接 |
|--------|------|------|----------|
| TagController | `/api/v1/tags` | 标签管理 | [标签管理API](./TagController-API文档.md) |
| ContentTagController | `/api/v1/content-tags` | 内容标签管理 | [内容标签API](./ContentTagController-API文档.md) |
| UserInterestTagController | `/api/v1/user-interest-tags` | 用户兴趣标签管理 | [兴趣标签API](./UserInterestTagController-API文档.md) |
| SearchController | `/api/v1/search` | 搜索服务 | [搜索服务API](./SearchController-API文档.md) |

### 用户管理模块

| 控制器 | 路径 | 描述 | 文档链接 |
|--------|------|------|----------|
| UserController | `/api/v1/users` | 用户管理 | [用户管理API](./UserController-API文档.md) |
| WalletController | `/api/v1/wallet` | 钱包管理 | [钱包管理API](./WalletController-API文档.md) |

### 任务管理模块

| 控制器 | 路径 | 描述 | 文档链接 |
|--------|------|------|----------|
| TaskController | `/api/v1/tasks` | 任务管理 | [任务管理API](./TaskController-API文档.md) |

## 文档完成情况

### ✅ 完整文档（12个）
1. **HealthController** - 健康检查API（2个接口）
2. **CategoryController** - 分类管理API（8个接口）
3. **SocialDynamicController** - 社交动态API（30个接口）
4. **SearchController** - 搜索服务API（12个接口）
5. **ContentPurchaseController** - 购买管理API（20个接口）
6. **FavoriteController** - 收藏管理API（21个接口）
7. **MessageSessionController** - 会话管理API（21个接口）
8. **OrderController** - 订单管理API（16个接口）
9. **UserController** - 用户管理API（20个接口）
10. **WalletController** - 钱包管理API（20个接口）
11. **PaymentController** - 支付管理API（20个接口）
12. **TaskController** - 任务管理API（20个接口）

### ⚠️ 简化文档（8个）
- AdsController、CommentController、ContentController、GoodsController、LikeController、MessageController、TagController、FollowController

### ✅ 完整文档（17个）
1. **HealthController** - 健康检查API（2个接口）
2. **CategoryController** - 分类管理API（8个接口）
3. **SocialDynamicController** - 社交动态API（30个接口）
4. **SearchController** - 搜索服务API（12个接口）
5. **ContentPurchaseController** - 购买管理API（20个接口）
6. **FavoriteController** - 收藏管理API（21个接口）
7. **MessageSessionController** - 会话管理API（21个接口）
8. **OrderController** - 订单管理API（16个接口）
9. **UserController** - 用户管理API（20个接口）
10. **WalletController** - 钱包管理API（20个接口）
11. **PaymentController** - 支付管理API（20个接口）
12. **TaskController** - 任务管理API（20个接口）
13. **ContentChapterController** - 章节管理API（3个接口）
14. **ContentPaymentController** - 付费管理API（3个接口）
15. **ContentTagController** - 内容标签API（3个接口）
16. **UserInterestTagController** - 兴趣标签API（3个接口）
17. **MessageSettingController** - 消息设置API（1个接口）

## API规范

### 统一响应格式

所有API都使用统一的响应格式：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    // 具体数据
  }
}
```

### 分页响应格式

列表查询API使用分页响应格式：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      // 数据列表
    ],
    "total": 100,
    "currentPage": 1,
    "pageSize": 20,
    "totalPages": 5
  }
}
```

### 通用错误码

| 错误码 | 描述 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未授权 |
| 403 | 禁止访问 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

### 通用参数

#### 分页参数
- `currentPage`: 当前页码（从1开始）
- `pageSize`: 页面大小（建议不超过100）

#### 排序参数
- `orderBy`: 排序字段
- `orderDirection`: 排序方向（ASC/DESC）

#### 时间格式
所有时间字段使用ISO 8601标准格式：`YYYY-MM-DDTHH:mm:ss`

### 认证与授权

- 大部分API需要用户认证
- 认证方式：Bearer Token
- 请求头：`Authorization: Bearer <token>`

### 限流规则

- 普通接口：100次/分钟
- 搜索接口：50次/分钟
- 上传接口：20次/分钟

## 文档更新记录

| 版本 | 日期 | 更新内容 |
|------|------|----------|
| 1.0.0 | 2024-01-01 | 初始版本，包含所有Controller的API文档 |
| 1.1.0 | 2024-01-01 | 新增8个完整API文档，文档完成度达到60% |
| 1.2.0 | 2024-01-01 | 新增4个完整API文档，文档完成度达到80% |

## 联系方式

如有问题或建议，请联系开发团队。
