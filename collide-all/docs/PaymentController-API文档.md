# 支付管理API接口文档

## 基本信息
- **控制器名称**: PaymentController
- **基础路径**: `/api/v1/payments`
- **描述**: 支付管理相关接口，支持支付宝、微信支付、余额支付等多种支付方式

## 接口列表

### 1. 支付记录列表查询

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/payments/list`
- **接口描述**: 支持多种条件查询和分页的支付记录列表

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| userId | Long | 否 | - | 用户ID |
| orderId | Long | 否 | - | 订单ID |
| status | String | 否 | - | 支付状态：pending、success、failed、cancelled |
| paymentMethod | String | 否 | - | 支付方式：alipay、wechat、balance |
| paymentType | String | 否 | - | 支付类型 |
| keyword | String | 否 | - | 关键词搜索 |
| orderBy | String | 否 | createTime | 排序字段 |
| orderDirection | String | 否 | DESC | 排序方向：ASC、DESC |
| currentPage | Integer | 否 | 1 | 当前页码 |
| pageSize | Integer | 否 | 20 | 页面大小 |

#### 请求示例
```http
GET /api/v1/payments/list?userId=123&status=success&currentPage=1&pageSize=10
```

#### 响应参数

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "paymentNo": "PAY202401010001",
        "orderId": 1001,
        "orderNo": "ORDER202401010001",
        "userId": 123,
        "userNickname": "用户昵称",
        "amount": 99.99,
        "payMethod": "alipay",
        "payChannel": "支付宝",
        "thirdPartyNo": "2024010122001234567890",
        "status": "success",
        "payTime": "2024-01-01T10:30:00",
        "notifyTime": "2024-01-01T10:30:05",
        "createTime": "2024-01-01T10:25:00",
        "updateTime": "2024-01-01T10:30:05",
        "payUrl": "https://openapi.alipay.com/gateway.do?...",
        "statusDesc": "支付成功",
        "cancellable": false
      }
    ],
    "total": 100,
    "currentPage": 1,
    "pageSize": 20,
    "totalPages": 5
  }
}
```

## 支付门面服务接口

### 2. 创建支付订单

#### 接口信息
- **请求方式**: POST
- **接口路径**: `/api/v1/payment/create`
- **接口描述**: 创建支付订单

#### 请求参数

```json
{
  "orderId": 1001,
  "orderNo": "ORDER202401010001",
  "userId": 123,
  "userNickname": "用户昵称",
  "amount": 99.99,
  "payMethod": "alipay",
  "payChannel": "支付宝",
  "notifyUrl": "https://example.com/notify",
  "returnUrl": "https://example.com/return",
  "subject": "商品购买",
  "remark": "备注信息"
}
```

#### 响应参数

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "paymentNo": "PAY202401010001",
    "orderId": 1001,
    "orderNo": "ORDER202401010001",
    "userId": 123,
    "userNickname": "用户昵称",
    "amount": 99.99,
    "payMethod": "alipay",
    "payChannel": "支付宝",
    "status": "pending",
    "createTime": "2024-01-01T10:25:00",
    "updateTime": "2024-01-01T10:25:00",
    "payUrl": "https://openapi.alipay.com/gateway.do?...",
    "statusDesc": "待支付",
    "cancellable": true
  }
}
```

### 3. 查询支付详情

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/payment/{paymentId}`
- **接口描述**: 根据支付ID查询支付详情

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| paymentId | Long | 是 | 支付ID（路径参数） |

#### 请求示例
```http
GET /api/v1/payment/1
```

#### 响应参数

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "paymentNo": "PAY202401010001",
    "orderId": 1001,
    "orderNo": "ORDER202401010001",
    "userId": 123,
    "userNickname": "用户昵称",
    "amount": 99.99,
    "payMethod": "alipay",
    "payChannel": "支付宝",
    "thirdPartyNo": "2024010122001234567890",
    "status": "success",
    "payTime": "2024-01-01T10:30:00",
    "notifyTime": "2024-01-01T10:30:05",
    "createTime": "2024-01-01T10:25:00",
    "updateTime": "2024-01-01T10:30:05",
    "payUrl": "https://openapi.alipay.com/gateway.do?...",
    "statusDesc": "支付成功",
    "cancellable": false
  }
}
```

### 4. 根据支付单号查询

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/payment/no/{paymentNo}`
- **接口描述**: 根据支付单号查询支付详情

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| paymentNo | String | 是 | 支付单号（路径参数） |

#### 请求示例
```http
GET /api/v1/payment/no/PAY202401010001
```

#### 响应参数

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "paymentNo": "PAY202401010001",
    "orderId": 1001,
    "orderNo": "ORDER202401010001",
    "userId": 123,
    "userNickname": "用户昵称",
    "amount": 99.99,
    "payMethod": "alipay",
    "payChannel": "支付宝",
    "thirdPartyNo": "2024010122001234567890",
    "status": "success",
    "payTime": "2024-01-01T10:30:00",
    "notifyTime": "2024-01-01T10:30:05",
    "createTime": "2024-01-01T10:25:00",
    "updateTime": "2024-01-01T10:30:05",
    "payUrl": "https://openapi.alipay.com/gateway.do?...",
    "statusDesc": "支付成功",
    "cancellable": false
  }
}
```

### 5. 分页查询支付记录

#### 接口信息
- **请求方式**: POST
- **接口路径**: `/api/v1/payment/query`
- **接口描述**: 分页查询支付记录

#### 请求参数

```json
{
  "paymentNo": "PAY202401010001",
  "orderId": 1001,
  "orderNo": "ORDER202401010001",
  "userId": 123,
  "payMethod": "alipay",
  "status": "success",
  "thirdPartyNo": "2024010122001234567890",
  "minAmount": 10.00,
  "maxAmount": 1000.00,
  "startTime": "2024-01-01T00:00:00",
  "endTime": "2024-12-31T23:59:59",
  "sortBy": "create_time",
  "sortDirection": "desc",
  "currentPage": 0,
  "pageSize": 10
}
```

#### 响应参数

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "paymentNo": "PAY202401010001",
        "orderId": 1001,
        "orderNo": "ORDER202401010001",
        "userId": 123,
        "userNickname": "用户昵称",
        "amount": 99.99,
        "payMethod": "alipay",
        "payChannel": "支付宝",
        "thirdPartyNo": "2024010122001234567890",
        "status": "success",
        "payTime": "2024-01-01T10:30:00",
        "notifyTime": "2024-01-01T10:30:05",
        "createTime": "2024-01-01T10:25:00",
        "updateTime": "2024-01-01T10:30:05",
        "payUrl": "https://openapi.alipay.com/gateway.do?...",
        "statusDesc": "支付成功",
        "cancellable": false
      }
    ],
    "total": 100,
    "currentPage": 0,
    "pageSize": 10,
    "totalPages": 10
  }
}
```

### 6. 支付回调处理

#### 接口信息
- **请求方式**: POST
- **接口路径**: `/api/v1/payment/callback`
- **接口描述**: 处理第三方支付回调

#### 请求参数

```json
{
  "paymentNo": "PAY202401010001",
  "thirdPartyNo": "2024010122001234567890",
  "status": "success",
  "payTime": "2024-01-01T10:30:00",
  "notifyTime": "2024-01-01T10:30:05",
  "payChannel": "支付宝",
  "rawData": {
    "trade_no": "2024010122001234567890",
    "out_trade_no": "PAY202401010001"
  },
  "sign": "支付签名",
  "remark": "支付成功"
}
```

#### 响应参数

```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

### 7. 取消支付

#### 接口信息
- **请求方式**: POST
- **接口路径**: `/api/v1/payment/cancel/{paymentId}`
- **接口描述**: 取消支付订单

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| paymentId | Long | 是 | 支付ID（路径参数） |

#### 请求示例
```http
POST /api/v1/payment/cancel/1
```

#### 响应参数

```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

### 8. 同步支付状态

#### 接口信息
- **请求方式**: POST
- **接口路径**: `/api/v1/payment/sync/{paymentNo}`
- **接口描述**: 同步第三方支付状态

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| paymentNo | String | 是 | 支付单号（路径参数） |

#### 请求示例
```http
POST /api/v1/payment/sync/PAY202401010001
```

#### 响应参数

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "paymentNo": "PAY202401010001",
    "orderId": 1001,
    "orderNo": "ORDER202401010001",
    "userId": 123,
    "userNickname": "用户昵称",
    "amount": 99.99,
    "payMethod": "alipay",
    "payChannel": "支付宝",
    "thirdPartyNo": "2024010122001234567890",
    "status": "success",
    "payTime": "2024-01-01T10:30:00",
    "notifyTime": "2024-01-01T10:30:05",
    "createTime": "2024-01-01T10:25:00",
    "updateTime": "2024-01-01T10:30:05",
    "payUrl": "https://openapi.alipay.com/gateway.do?...",
    "statusDesc": "支付成功",
    "cancellable": false
  }
}
```

### 9. 获取用户支付记录

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/payment/user/{userId}`
- **接口描述**: 获取用户支付记录

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| userId | Long | 是 | - | 用户ID（路径参数） |
| limit | Integer | 否 | 10 | 返回记录数量限制 |

#### 请求示例
```http
GET /api/v1/payment/user/123?limit=20
```

#### 响应参数

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "paymentNo": "PAY202401010001",
      "orderId": 1001,
      "orderNo": "ORDER202401010001",
      "userId": 123,
      "userNickname": "用户昵称",
      "amount": 99.99,
      "payMethod": "alipay",
      "payChannel": "支付宝",
      "thirdPartyNo": "2024010122001234567890",
      "status": "success",
      "payTime": "2024-01-01T10:30:00",
      "notifyTime": "2024-01-01T10:30:05",
      "createTime": "2024-01-01T10:25:00",
      "updateTime": "2024-01-01T10:30:05",
      "payUrl": "https://openapi.alipay.com/gateway.do?...",
      "statusDesc": "支付成功",
      "cancellable": false
    }
  ]
}
```

### 10. 根据订单ID查询支付记录

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/payment/order/{orderId}`
- **接口描述**: 根据订单ID查询支付记录

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| orderId | Long | 是 | 订单ID（路径参数） |

#### 请求示例
```http
GET /api/v1/payment/order/1001
```

#### 响应参数

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "paymentNo": "PAY202401010001",
      "orderId": 1001,
      "orderNo": "ORDER202401010001",
      "userId": 123,
      "userNickname": "用户昵称",
      "amount": 99.99,
      "payMethod": "alipay",
      "payChannel": "支付宝",
      "thirdPartyNo": "2024010122001234567890",
      "status": "success",
      "payTime": "2024-01-01T10:30:00",
      "notifyTime": "2024-01-01T10:30:05",
      "createTime": "2024-01-01T10:25:00",
      "updateTime": "2024-01-01T10:30:05",
      "payUrl": "https://openapi.alipay.com/gateway.do?...",
      "statusDesc": "支付成功",
      "cancellable": false
    }
  ]
}
```

### 11. 验证支付状态

#### 接口信息
- **请求方式**: POST
- **接口路径**: `/api/v1/payment/verify/{paymentNo}`
- **接口描述**: 验证支付状态

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| paymentNo | String | 是 | 支付单号（路径参数） |

#### 请求示例
```http
POST /api/v1/payment/verify/PAY202401010001
```

#### 响应参数

```json
{
  "code": 200,
  "message": "success",
  "data": true
}
```

### 12. 获取支付统计信息

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/payment/statistics/{userId}`
- **接口描述**: 获取用户支付统计信息

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID（路径参数） |

#### 请求示例
```http
GET /api/v1/payment/statistics/123
```

#### 响应参数

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "paymentNo": "PAY202401010001",
    "orderId": 1001,
    "orderNo": "ORDER202401010001",
    "userId": 123,
    "userNickname": "用户昵称",
    "amount": 99.99,
    "payMethod": "alipay",
    "payChannel": "支付宝",
    "thirdPartyNo": "2024010122001234567890",
    "status": "success",
    "payTime": "2024-01-01T10:30:00",
    "notifyTime": "2024-01-01T10:30:05",
    "createTime": "2024-01-01T10:25:00",
    "updateTime": "2024-01-01T10:30:05",
    "payUrl": "https://openapi.alipay.com/gateway.do?...",
    "statusDesc": "支付成功",
    "cancellable": false
  }
}
```

## 数据库表结构

### t_payment 支付记录表

| 字段名 | 类型 | 描述 |
|--------|------|------|
| id | bigint | 支付ID（主键） |
| payment_no | varchar(50) | 支付单号（唯一） |
| order_id | bigint | 订单ID |
| order_no | varchar(50) | 订单号（冗余） |
| user_id | bigint | 用户ID |
| user_nickname | varchar(100) | 用户昵称（冗余） |
| amount | decimal(10,2) | 支付金额 |
| pay_method | varchar(20) | 支付方式：alipay、wechat、balance |
| pay_channel | varchar(50) | 支付渠道 |
| third_party_no | varchar(100) | 第三方支付单号 |
| status | varchar(20) | 支付状态：pending、success、failed、cancelled |
| settlement_status | varchar(20) | 到账状态：pending、success、failed、processing |
| pay_time | datetime | 支付完成时间 |
| notify_time | datetime | 回调通知时间 |
| create_time | timestamp | 创建时间 |
| update_time | timestamp | 更新时间 |

## 字段说明

### 支付方式 (payMethod)
- `alipay`: 支付宝支付
- `wechat`: 微信支付
- `balance`: 余额支付

### 支付状态 (status)
- `pending`: 待支付
- `success`: 支付成功
- `failed`: 支付失败
- `cancelled`: 已取消

### 到账状态 (settlementStatus)
- `pending`: 待到账
- `success`: 已到账
- `failed`: 到账失败
- `processing`: 处理中

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
2. 分页参数currentPage从0开始（Spring Data JPA标准）
3. pageSize建议不超过100
4. 金额字段单位为元，精确到分
5. 支付单号格式：PAY + YYYYMMDD + 6位序号
6. 第三方支付单号由支付平台返回
7. 支付回调需要验证签名确保安全性
8. 支付状态变更需要记录操作日志
9. 支持支付超时自动取消机制
10. 支付统计包含总金额、成功次数、失败次数等信息

## 业务逻辑说明

### 支付流程
1. **创建支付订单**: 用户选择商品后创建支付订单
2. **生成支付链接**: 根据支付方式生成对应的支付链接或二维码
3. **用户支付**: 用户在第三方平台完成支付
4. **回调处理**: 第三方平台回调通知支付结果
5. **状态更新**: 更新支付状态并处理后续业务逻辑

### 支付状态流转
- `pending` → `success`: 支付成功
- `pending` → `failed`: 支付失败
- `pending` → `cancelled`: 用户取消支付
- `pending` → `timeout`: 支付超时

### 安全考虑
1. **签名验证**: 所有回调请求必须验证签名
2. **金额校验**: 回调时必须校验支付金额
3. **幂等性**: 同一支付单号只能处理一次
4. **日志记录**: 所有支付操作都要记录详细日志

### 性能优化
1. **缓存机制**: 支付状态查询使用缓存
2. **异步处理**: 支付回调异步处理
3. **数据库索引**: 关键字段建立索引
4. **分页查询**: 大量数据使用分页查询

## 开发建议

1. **测试环境**: 使用沙箱环境进行支付测试
2. **监控告警**: 设置支付失败率监控
3. **数据备份**: 定期备份支付数据
4. **版本兼容**: 保持API版本兼容性
