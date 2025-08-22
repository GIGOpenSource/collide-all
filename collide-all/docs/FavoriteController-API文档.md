# 收藏管理API接口文档

## 基本信息
- **控制器名称**: FavoriteController
- **基础路径**: `/api/v1/favorite`
- **描述**: 用户收藏功能的管理接口

## 接口列表

### 1. 添加收藏

#### 接口信息
- **请求方式**: POST
- **接口路径**: `/api/v1/favorite/add`
- **接口描述**: 用户添加收藏

#### 请求参数

```json
{
  "userId": 123,
  "targetId": 456,
  "targetType": "CONTENT",
  "targetTitle": "收藏目标标题",
  "targetImage": "https://example.com/image.jpg",
  "note": "收藏备注"
}
```

#### 响应参数

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "userId": 123,
    "userNickname": "用户昵称",
    "targetId": 456,
    "targetType": "CONTENT",
    "targetTitle": "收藏目标标题",
    "targetImage": "https://example.com/image.jpg",
    "note": "收藏备注",
    "status": "ACTIVE",
    "createTime": "2024-01-01T00:00:00",
    "updateTime": "2024-01-01T00:00:00"
  }
}
```

### 2. 移除收藏

#### 接口信息
- **请求方式**: POST
- **接口路径**: `/api/v1/favorite/remove`
- **接口描述**: 用户移除收藏

#### 请求参数

```json
{
  "userId": 123,
  "targetId": 456,
  "targetType": "CONTENT"
}
```

### 3. 检查收藏状态

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/favorite/check`
- **接口描述**: 检查用户是否已收藏指定目标

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID |
| targetId | Long | 是 | 目标ID |
| targetType | String | 是 | 目标类型 |

#### 请求示例
```http
GET /api/v1/favorite/check?userId=123&targetId=456&targetType=CONTENT
```

### 4. 获取收藏详情

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/favorite/detail`
- **接口描述**: 获取收藏记录详情

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID |
| targetId | Long | 是 | 目标ID |
| targetType | String | 是 | 目标类型 |

#### 请求示例
```http
GET /api/v1/favorite/detail?userId=123&targetId=456&targetType=CONTENT
```

### 5. 分页查询收藏

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/favorite/query`
- **接口描述**: 分页查询用户收藏记录

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| userId | Long | 是 | - | 用户ID |
| targetType | String | 否 | - | 目标类型 |
| status | String | 否 | - | 状态 |
| keyword | String | 否 | - | 关键词搜索 |
| currentPage | Integer | 否 | 1 | 当前页码 |
| pageSize | Integer | 否 | 20 | 页面大小 |
| orderBy | String | 否 | createTime | 排序字段 |
| orderDirection | String | 否 | DESC | 排序方向 |

#### 请求示例
```http
GET /api/v1/favorite/query?userId=123&targetType=CONTENT&currentPage=1&pageSize=10
```

### 6. 用户收藏列表

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/favorite/user`
- **接口描述**: 获取用户的收藏列表

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| userId | Long | 是 | - | 用户ID |
| targetType | String | 否 | - | 目标类型 |
| currentPage | Integer | 否 | 1 | 当前页码 |
| pageSize | Integer | 否 | 20 | 页面大小 |

#### 请求示例
```http
GET /api/v1/favorite/user?userId=123&targetType=CONTENT&currentPage=1&pageSize=10
```

### 7. 目标收藏列表

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/favorite/target`
- **接口描述**: 获取指定目标的收藏用户列表

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| targetId | Long | 是 | - | 目标ID |
| targetType | String | 是 | - | 目标类型 |
| currentPage | Integer | 否 | 1 | 当前页码 |
| pageSize | Integer | 否 | 20 | 页面大小 |

#### 请求示例
```http
GET /api/v1/favorite/target?targetId=456&targetType=CONTENT&currentPage=1&pageSize=10
```

### 8. 用户收藏数量

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/favorite/user/count`
- **接口描述**: 获取用户的收藏数量

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID |
| targetType | String | 否 | 目标类型 |

#### 请求示例
```http
GET /api/v1/favorite/user/count?userId=123&targetType=CONTENT
```

### 9. 目标收藏数量

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/favorite/target/count`
- **接口描述**: 获取指定目标的收藏数量

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| targetId | Long | 是 | 目标ID |
| targetType | String | 是 | 目标类型 |

#### 请求示例
```http
GET /api/v1/favorite/target/count?targetId=456&targetType=CONTENT
```

### 10. 用户收藏统计

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/favorite/user/statistics`
- **接口描述**: 获取用户的收藏统计信息

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID |
| startTime | String | 否 | 开始时间 |
| endTime | String | 否 | 结束时间 |

#### 请求示例
```http
GET /api/v1/favorite/user/statistics?userId=123&startTime=2024-01-01&endTime=2024-12-31
```

### 11. 批量检查收藏状态

#### 接口信息
- **请求方式**: POST
- **接口路径**: `/api/v1/favorite/batch-check`
- **接口描述**: 批量检查多个目标的收藏状态

#### 请求参数

```json
{
  "userId": 123,
  "targets": [
    {
      "targetId": 456,
      "targetType": "CONTENT"
    },
    {
      "targetId": 789,
      "targetType": "USER"
    }
  ]
}
```

### 12. 搜索收藏

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/favorite/search`
- **接口描述**: 搜索用户收藏记录

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| userId | Long | 是 | - | 用户ID |
| keyword | String | 是 | - | 搜索关键词 |
| targetType | String | 否 | - | 目标类型 |
| currentPage | Integer | 否 | 1 | 当前页码 |
| pageSize | Integer | 否 | 20 | 页面大小 |

#### 请求示例
```http
GET /api/v1/favorite/search?userId=123&keyword=小说&currentPage=1&pageSize=10
```

### 13. 热门收藏

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/favorite/popular`
- **接口描述**: 获取热门收藏目标

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| targetType | String | 否 | - | 目标类型 |
| limit | Integer | 否 | 10 | 限制数量 |
| timeRange | String | 否 | 7d | 时间范围 |

#### 请求示例
```http
GET /api/v1/favorite/popular?targetType=CONTENT&limit=20&timeRange=30d
```

### 14. 清理收藏

#### 接口信息
- **请求方式**: POST
- **接口路径**: `/api/v1/favorite/clean`
- **接口描述**: 清理无效的收藏记录

#### 请求参数

```json
{
  "userId": 123,
  "targetType": "CONTENT",
  "cleanType": "INVALID_TARGET"
}
```

### 15. 更新用户信息

#### 接口信息
- **请求方式**: PUT
- **接口路径**: `/api/v1/favorite/user/info`
- **接口描述**: 更新收藏记录中的用户信息

#### 请求参数

```json
{
  "userId": 123,
  "userNickname": "新昵称",
  "userAvatar": "https://example.com/new-avatar.jpg"
}
```

### 16. 更新目标信息

#### 接口信息
- **请求方式**: PUT
- **接口路径**: `/api/v1/favorite/target/info`
- **接口描述**: 更新收藏记录中的目标信息

#### 请求参数

```json
{
  "targetId": 456,
  "targetType": "CONTENT",
  "targetTitle": "新标题",
  "targetImage": "https://example.com/new-image.jpg"
}
```

### 17. 作者收藏查询

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/favorite/author`
- **接口描述**: 查询用户收藏的作者

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| userId | Long | 是 | - | 用户ID |
| currentPage | Integer | 否 | 1 | 当前页码 |
| pageSize | Integer | 否 | 20 | 页面大小 |

#### 请求示例
```http
GET /api/v1/favorite/author?userId=123&currentPage=1&pageSize=10
```

### 18. 检查收藏关系

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/favorite/relation/exists`
- **接口描述**: 检查收藏关系是否存在

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID |
| targetId | Long | 是 | 目标ID |
| targetType | String | 是 | 目标类型 |

#### 请求示例
```http
GET /api/v1/favorite/relation/exists?userId=123&targetId=456&targetType=CONTENT
```

### 19. 重新激活收藏

#### 接口信息
- **请求方式**: POST
- **接口路径**: `/api/v1/favorite/reactivate`
- **接口描述**: 重新激活已删除的收藏

#### 请求参数

```json
{
  "userId": 123,
  "targetId": 456,
  "targetType": "CONTENT"
}
```

### 20. 验证收藏

#### 接口信息
- **请求方式**: POST
- **接口路径**: `/api/v1/favorite/validate`
- **接口描述**: 验证收藏记录的有效性

#### 请求参数

```json
{
  "userId": 123,
  "targetId": 456,
  "targetType": "CONTENT"
}
```

### 21. 检查是否可以收藏

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/favorite/can-favorite`
- **接口描述**: 检查用户是否可以收藏指定目标

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID |
| targetId | Long | 是 | 目标ID |
| targetType | String | 是 | 目标类型 |

#### 请求示例
```http
GET /api/v1/favorite/can-favorite?userId=123&targetId=456&targetType=CONTENT
```

## 收藏记录字段说明

| 字段名 | 类型 | 描述 |
|--------|------|------|
| id | Long | 收藏记录ID |
| userId | Long | 用户ID |
| userNickname | String | 用户昵称 |
| targetId | Long | 目标ID |
| targetType | String | 目标类型 |
| targetTitle | String | 目标标题 |
| targetImage | String | 目标图片 |
| note | String | 收藏备注 |
| status | String | 收藏状态 |
| createTime | String | 创建时间 |
| updateTime | String | 更新时间 |

## 错误码说明

| 错误码 | 描述 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未授权 |
| 403 | 禁止访问 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

## 注意事项

1. 所有时间字段格式为ISO 8601标准格式
2. 分页参数currentPage从1开始
3. pageSize建议不超过100
4. 收藏状态包括：ACTIVE（活跃）、DELETED（已删除）、INVALID（无效）
5. 目标类型包括：CONTENT（内容）、USER（用户）、GOODS（商品）、TAG（标签）
6. 时间范围格式：7d（7天）、30d（30天）、90d（90天）
7. 清理类型包括：INVALID_TARGET（无效目标）、DUPLICATE（重复）、EXPIRED（过期）
