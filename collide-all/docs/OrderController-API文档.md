# 订单管理API接口文档

## 基本信息
- **控制器名称**: OrderController
- **基础路径**: `/api/v1/orders`
- **描述**: 订单管理相关接口

## 接口列表

### 1. 创建订单

#### 接口信息
- **请求方式**: POST
- **接口路径**: `/api/v1/orders`
- **接口描述**: 创建新订单

#### 请求参数

```json
{
  "userId": 123,
  "goodsId": 456,
  "goodsType": "DIGITAL",
  "quantity": 1,
  "amount": 1000,
  "currency": "CNY",
  "address": "收货地址",
  "remark": "订单备注"
}
```

#### 响应参数

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "orderNo": "ORD20240101001",
    "userId": 123,
    "userNickname": "用户昵称",
    "goodsId": 456,
    "goodsTitle": "商品标题",
    "goodsType": "DIGITAL",
    "quantity": 1,
    "amount": 1000,
    "currency": "CNY",
    "status": "PENDING",
    "address": "收货地址",
    "remark": "订单备注",
    "createTime": "2024-01-01T00:00:00",
    "updateTime": "2024-01-01T00:00:00"
  }
}
```

### 2. 获取订单详情

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/orders/{id}`
- **接口描述**: 根据订单ID获取详情

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| id | Long | 是 | 订单ID（路径参数） |

#### 请求示例
```http
GET /api/v1/orders/1
```

### 3. 根据订单号获取订单

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/orders/no/{orderNo}`
- **接口描述**: 根据订单号获取订单详情

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| orderNo | String | 是 | 订单号（路径参数） |

#### 请求示例
```http
GET /api/v1/orders/no/ORD20240101001
```

### 4. 取消订单

#### 接口信息
- **请求方式**: POST
- **接口路径**: `/api/v1/orders/{id}/cancel`
- **接口描述**: 取消指定订单

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| id | Long | 是 | 订单ID（路径参数） |

#### 请求示例
```http
POST /api/v1/orders/1/cancel
```

### 5. 批量取消订单

#### 接口信息
- **请求方式**: POST
- **接口路径**: `/api/v1/orders/batch-cancel`
- **接口描述**: 批量取消多个订单

#### 请求参数

```json
{
  "orderIds": [1, 2, 3],
  "reason": "批量取消原因"
}
```

### 6. 分页查询订单

#### 接口信息
- **请求方式**: POST
- **接口路径**: `/api/v1/orders/query`
- **接口描述**: 分页查询订单记录

#### 请求参数

```json
{
  "userId": 123,
  "orderNo": "ORD20240101001",
  "status": "PENDING",
  "goodsType": "DIGITAL",
  "startTime": "2024-01-01T00:00:00",
  "endTime": "2024-12-31T23:59:59",
  "currentPage": 1,
  "pageSize": 20,
  "orderBy": "createTime",
  "orderDirection": "DESC"
}
```

### 7. 用户订单列表

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/orders/user/{userId}`
- **接口描述**: 获取指定用户的订单列表

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| userId | Long | 是 | - | 用户ID（路径参数） |
| status | String | 否 | - | 订单状态 |
| currentPage | Integer | 否 | 1 | 当前页码 |
| pageSize | Integer | 否 | 20 | 页面大小 |

#### 请求示例
```http
GET /api/v1/orders/user/123?status=PENDING&currentPage=1&pageSize=10
```

### 8. 处理订单支付

#### 接口信息
- **请求方式**: POST
- **接口路径**: `/api/v1/orders/{id}/payment/process`
- **接口描述**: 处理订单支付流程

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| id | Long | 是 | 订单ID（路径参数） |

#### 请求体

```json
{
  "paymentMethod": "ALIPAY",
  "paymentAmount": 1000
}
```

### 9. 用户订单统计

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/orders/statistics/user/{userId}`
- **接口描述**: 获取用户的订单统计信息

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID（路径参数） |
| startTime | String | 否 | 开始时间 |
| endTime | String | 否 | 结束时间 |

#### 请求示例
```http
GET /api/v1/orders/statistics/user/123?startTime=2024-01-01&endTime=2024-12-31
```

### 10. 模拟支付

#### 接口信息
- **请求方式**: POST
- **接口路径**: `/api/v1/orders/{id}/mock-payment`
- **接口描述**: 模拟订单支付（测试用）

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| id | Long | 是 | 订单ID（路径参数） |

#### 请求体

```json
{
  "paymentMethod": "MOCK",
  "paymentAmount": 1000,
  "success": true
}
```

### 11. 商品类型订单查询

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/orders/goods-type/{goodsType}`
- **接口描述**: 根据商品类型查询订单

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| goodsType | String | 是 | - | 商品类型（路径参数） |
| currentPage | Integer | 否 | 1 | 当前页码 |
| pageSize | Integer | 否 | 20 | 页面大小 |

#### 请求示例
```http
GET /api/v1/orders/goods-type/DIGITAL?currentPage=1&pageSize=10
```

### 12. 卖家订单查询

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/orders/seller/{sellerId}`
- **接口描述**: 根据卖家ID查询订单

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| sellerId | Long | 是 | - | 卖家ID（路径参数） |
| currentPage | Integer | 否 | 1 | 当前页码 |
| pageSize | Integer | 否 | 20 | 页面大小 |

#### 请求示例
```http
GET /api/v1/orders/seller/456?currentPage=1&pageSize=10
```

### 13. 搜索订单

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/orders/search`
- **接口描述**: 搜索订单记录

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| keyword | String | 是 | - | 搜索关键词 |
| currentPage | Integer | 否 | 1 | 当前页码 |
| pageSize | Integer | 否 | 20 | 页面大小 |

#### 请求示例
```http
GET /api/v1/orders/search?keyword=ORD20240101001&currentPage=1&pageSize=10
```

### 14. 时间范围订单查询

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/orders/time-range`
- **接口描述**: 根据时间范围查询订单

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| startTime | String | 是 | - | 开始时间 |
| endTime | String | 是 | - | 结束时间 |
| status | String | 否 | - | 订单状态 |
| currentPage | Integer | 否 | 1 | 当前页码 |
| pageSize | Integer | 否 | 20 | 页面大小 |

#### 请求示例
```http
GET /api/v1/orders/time-range?startTime=2024-01-01T00:00:00&endTime=2024-12-31T23:59:59&currentPage=1&pageSize=10
```

### 15. 确认订单支付

#### 接口信息
- **请求方式**: POST
- **接口路径**: `/api/v1/orders/{id}/payment/confirm`
- **接口描述**: 确认订单支付

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| id | Long | 是 | 订单ID（路径参数） |

#### 请求体

```json
{
  "paymentId": "PAY_20240101001",
  "paymentTime": "2024-01-01T00:00:00",
  "transactionId": "TXN_20240101001"
}
```

### 16. 支付回调处理

#### 接口信息
- **请求方式**: POST
- **接口路径**: `/api/v1/orders/payment/callback`
- **接口描述**: 处理第三方支付回调

#### 请求参数

```json
{
  "orderNo": "ORD20240101001",
  "paymentId": "PAY_20240101001",
  "amount": 1000,
  "status": "SUCCESS",
  "signature": "支付签名",
  "timestamp": "2024-01-01T00:00:00"
}
```

## 订单记录字段说明

| 字段名 | 类型 | 描述 |
|--------|------|------|
| id | Long | 订单ID |
| orderNo | String | 订单号 |
| userId | Long | 用户ID |
| userNickname | String | 用户昵称 |
| goodsId | Long | 商品ID |
| goodsTitle | String | 商品标题 |
| goodsType | String | 商品类型 |
| quantity | Integer | 数量 |
| amount | Long | 订单金额（分） |
| currency | String | 货币类型 |
| status | String | 订单状态 |
| address | String | 收货地址 |
| remark | String | 订单备注 |
| paymentMethod | String | 支付方式 |
| paymentTime | String | 支付时间 |
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
5. 订单状态包括：PENDING（待支付）、PAID（已支付）、CANCELLED（已取消）、COMPLETED（已完成）、REFUNDED（已退款）
6. 商品类型包括：DIGITAL（数字商品）、PHYSICAL（实物商品）、VIRTUAL（虚拟商品）
7. 支付方式包括：ALIPAY（支付宝）、WECHAT（微信支付）、BANK（银行卡）、MOCK（模拟支付）
8. 订单号格式：ORD_YYYYMMDDHHMMSS
