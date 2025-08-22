# 内容购买管理API接口文档

## 基本信息
- **控制器名称**: ContentPurchaseController
- **基础路径**: `/api/v1/content/purchase`
- **描述**: 用户内容购买记录的管理、查询和统计接口（极简版）

## 接口列表

### 1. 获取购买记录

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/content/purchase/{id}`
- **接口描述**: 根据购买记录ID获取详情

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| id | Long | 是 | 购买记录ID（路径参数） |

#### 请求示例
```http
GET /api/v1/content/purchase/123
```

#### 响应参数

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 123,
    "userId": 456,
    "userNickname": "用户昵称",
    "contentId": 789,
    "contentTitle": "内容标题",
    "contentType": "NOVEL",
    "orderId": 101,
    "orderNo": "ORD20240101001",
    "amount": 1000,
    "currency": "CNY",
    "status": "ACTIVE",
    "isValid": true,
    "purchaseTime": "2024-01-01T00:00:00",
    "expireTime": "2025-01-01T00:00:00",
    "accessCount": 10,
    "lastAccessTime": "2024-01-01T00:00:00",
    "createTime": "2024-01-01T00:00:00",
    "updateTime": "2024-01-01T00:00:00"
  }
}
```

### 2. 删除购买记录

#### 接口信息
- **请求方式**: DELETE
- **接口路径**: `/api/v1/content/purchase/{id}`
- **接口描述**: 逻辑删除指定的购买记录

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| id | Long | 是 | 购买记录ID（路径参数） |

#### 请求示例
```http
DELETE /api/v1/content/purchase/123
```

### 3. 万能条件查询购买记录

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/content/purchase/query`
- **接口描述**: 根据多种条件查询购买记录列表，替代所有具体查询API

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| userId | Long | 否 | - | 用户ID |
| contentId | Long | 否 | - | 内容ID |
| contentType | String | 否 | - | 内容类型 |
| orderId | Long | 否 | - | 订单ID |
| orderNo | String | 否 | - | 订单号 |
| status | String | 否 | - | 状态 |
| isValid | Boolean | 否 | - | 是否有效 |
| minAmount | Long | 否 | - | 最小金额 |
| maxAmount | Long | 否 | - | 最大金额 |
| orderBy | String | 否 | createTime | 排序字段 |
| orderDirection | String | 否 | DESC | 排序方向 |
| currentPage | Integer | 否 | - | 当前页码 |
| pageSize | Integer | 否 | - | 页面大小 |

#### 请求示例
```http
GET /api/v1/content/purchase/query?userId=456&contentType=NOVEL&currentPage=1&pageSize=10
```

### 4. 推荐购买记录查询

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/content/purchase/recommended`
- **接口描述**: 获取推荐的购买记录

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| strategy | String | 是 | 推荐策略：RECENT、HIGH_VALUE、MOST_ACCESSED、POPULAR |
| limit | Integer | 否 | 限制数量 |

#### 请求示例
```http
GET /api/v1/content/purchase/recommended?strategy=POPULAR&limit=10
```

### 5. 过期状态查询

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/content/purchase/expire-status`
- **接口描述**: 查询购买记录的过期状态

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| userId | Long | 否 | 用户ID |
| contentId | Long | 否 | 内容ID |
| daysToExpire | Integer | 否 | 即将过期的天数 |

#### 请求示例
```http
GET /api/v1/content/purchase/expire-status?userId=456&daysToExpire=7
```

### 6. 检查访问权限

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/content/purchase/check-access`
- **接口描述**: 检查用户是否有权限访问指定内容

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID |
| contentId | Long | 是 | 内容ID |

#### 请求示例
```http
GET /api/v1/content/purchase/check-access?userId=456&contentId=789
```

### 7. 更新购买记录状态

#### 接口信息
- **请求方式**: PUT
- **接口路径**: `/api/v1/content/purchase/{purchaseId}/status`
- **接口描述**: 更新指定购买记录的状态

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| purchaseId | Long | 是 | 购买记录ID（路径参数） |
| status | String | 是 | 新状态 |

#### 请求示例
```http
PUT /api/v1/content/purchase/123/status
Content-Type: application/json

{
  "status": "EXPIRED"
}
```

### 8. 批量更新状态

#### 接口信息
- **请求方式**: PUT
- **接口路径**: `/api/v1/content/purchase/batch-status`
- **接口描述**: 批量更新多个购买记录的状态

#### 请求参数

```json
{
  "purchaseIds": [123, 124, 125],
  "status": "EXPIRED",
  "reason": "批量过期处理"
}
```

### 9. 统计信息查询

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/content/purchase/stats`
- **接口描述**: 获取购买记录统计信息

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| userId | Long | 否 | 用户ID |
| contentType | String | 否 | 内容类型 |
| startTime | String | 否 | 开始时间 |
| endTime | String | 否 | 结束时间 |

#### 请求示例
```http
GET /api/v1/content/purchase/stats?userId=456&contentType=NOVEL
```

### 10. 完成购买

#### 接口信息
- **请求方式**: POST
- **接口路径**: `/api/v1/content/purchase/complete`
- **接口描述**: 完成购买流程

#### 请求参数

```json
{
  "userId": 456,
  "contentId": 789,
  "orderId": 101,
  "orderNo": "ORD20240101001",
  "amount": 1000,
  "currency": "CNY",
  "purchaseTime": "2024-01-01T00:00:00",
  "expireTime": "2025-01-01T00:00:00"
}
```

### 11. 退款处理

#### 接口信息
- **请求方式**: POST
- **接口路径**: `/api/v1/content/purchase/{purchaseId}/refund`
- **接口描述**: 处理购买记录退款

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| purchaseId | Long | 是 | 购买记录ID（路径参数） |
| refundAmount | Long | 是 | 退款金额 |
| refundReason | String | 是 | 退款原因 |

#### 请求示例
```http
POST /api/v1/content/purchase/123/refund
Content-Type: application/json

{
  "refundAmount": 1000,
  "refundReason": "用户申请退款"
}
```

### 12. 访问记录

#### 接口信息
- **请求方式**: POST
- **接口路径**: `/api/v1/content/purchase/access`
- **接口描述**: 记录用户访问内容

#### 请求参数

```json
{
  "userId": 456,
  "contentId": 789,
  "accessTime": "2024-01-01T00:00:00",
  "accessType": "READ"
}
```

### 13. 用户购买记录查询

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/content/purchase/user/{userId}`
- **接口描述**: 获取指定用户的购买记录

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| userId | Long | 是 | - | 用户ID（路径参数） |
| currentPage | Integer | 否 | 1 | 当前页码 |
| pageSize | Integer | 否 | 20 | 页面大小 |
| status | String | 否 | - | 状态 |

#### 请求示例
```http
GET /api/v1/content/purchase/user/456?currentPage=1&pageSize=10&status=ACTIVE
```

### 14. 用户有效购买记录

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/content/purchase/user/{userId}/valid`
- **接口描述**: 获取用户的有效购买记录

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| userId | Long | 是 | - | 用户ID（路径参数） |
| currentPage | Integer | 否 | 1 | 当前页码 |
| pageSize | Integer | 否 | 20 | 页面大小 |

### 15. 内容购买记录查询

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/content/purchase/content/{contentId}`
- **接口描述**: 获取指定内容的购买记录

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| contentId | Long | 是 | - | 内容ID（路径参数） |
| currentPage | Integer | 否 | 1 | 当前页码 |
| pageSize | Integer | 否 | 20 | 页面大小 |

### 16. 订单购买记录查询

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/content/purchase/order/{orderId}`
- **接口描述**: 根据订单ID查询购买记录

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| orderId | Long | 是 | 订单ID（路径参数） |

### 17. 订单号购买记录查询

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/content/purchase/order-no/{orderNo}`
- **接口描述**: 根据订单号查询购买记录

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| orderNo | String | 是 | 订单号（路径参数） |

### 18. 用户内容类型购买记录

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/content/purchase/user/{userId}/content-type/{contentType}`
- **接口描述**: 获取用户指定内容类型的购买记录

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| userId | Long | 是 | - | 用户ID（路径参数） |
| contentType | String | 是 | - | 内容类型（路径参数） |
| currentPage | Integer | 否 | 1 | 当前页码 |
| pageSize | Integer | 否 | 20 | 页面大小 |

### 19. 用户折扣统计

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/content/purchase/user/{userId}/discount-stats`
- **接口描述**: 获取用户的折扣购买统计

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID（路径参数） |
| startTime | String | 否 | 开始时间 |
| endTime | String | 否 | 结束时间 |

### 20. 过期购买记录查询

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/content/purchase/expired`
- **接口描述**: 查询已过期的购买记录

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| currentPage | Integer | 否 | 1 | 当前页码 |
| pageSize | Integer | 否 | 20 | 页面大小 |
| daysExpired | Integer | 否 | - | 过期天数 |

#### 请求示例
```http
GET /api/v1/content/purchase/expired?currentPage=1&pageSize=10&daysExpired=30
```

## 购买记录字段说明

| 字段名 | 类型 | 描述 |
|--------|------|------|
| id | Long | 购买记录ID |
| userId | Long | 用户ID |
| userNickname | String | 用户昵称 |
| contentId | Long | 内容ID |
| contentTitle | String | 内容标题 |
| contentType | String | 内容类型 |
| orderId | Long | 订单ID |
| orderNo | String | 订单号 |
| amount | Long | 购买金额（分） |
| currency | String | 货币类型 |
| status | String | 购买状态 |
| isValid | Boolean | 是否有效 |
| purchaseTime | String | 购买时间 |
| expireTime | String | 过期时间 |
| accessCount | Integer | 访问次数 |
| lastAccessTime | String | 最后访问时间 |
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
4. 金额字段单位为分，需要转换为元显示
5. 购买状态包括：ACTIVE（有效）、EXPIRED（过期）、REFUNDED（已退款）、CANCELLED（已取消）
6. 内容类型包括：NOVEL（小说）、COMIC（漫画）、VIDEO（视频）、AUDIO（音频）
7. 推荐策略包括：RECENT（最近）、HIGH_VALUE（高价值）、MOST_ACCESSED（最常访问）、POPULAR（热门）
