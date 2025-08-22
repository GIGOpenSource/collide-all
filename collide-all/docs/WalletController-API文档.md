# 钱包管理API接口文档

## 基本信息
- **控制器名称**: WalletController
- **基础路径**: `/api/v1/wallet`
- **描述**: 用户钱包管理相关接口

## 接口列表

### 1. 获取钱包信息

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/wallet/{userId}`
- **接口描述**: 获取用户钱包信息

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID（路径参数） |

#### 请求示例
```http
GET /api/v1/wallet/123
```

#### 响应参数

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "userId": 123,
    "balance": 10000,
    "currency": "CNY",
    "status": "ACTIVE",
    "createTime": "2024-01-01T00:00:00",
    "updateTime": "2024-01-01T00:00:00"
  }
}
```

### 2. 充值

#### 接口信息
- **请求方式**: POST
- **接口路径**: `/api/v1/wallet/{userId}/recharge`
- **接口描述**: 钱包充值

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID（路径参数） |

#### 请求体

```json
{
  "amount": 1000,
  "paymentMethod": "ALIPAY",
  "orderNo": "RECHARGE_20240101001"
}
```

### 3. 提现

#### 接口信息
- **请求方式**: POST
- **接口路径**: `/api/v1/wallet/{userId}/withdraw`
- **接口描述**: 钱包提现

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID（路径参数） |

#### 请求体

```json
{
  "amount": 500,
  "bankCard": "6222021234567890123",
  "bankName": "中国银行",
  "realName": "张三"
}
```

### 4. 转账

#### 接口信息
- **请求方式**: POST
- **接口路径**: `/api/v1/wallet/{userId}/transfer`
- **接口描述**: 钱包转账

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID（路径参数） |

#### 请求体

```json
{
  "targetUserId": 456,
  "amount": 100,
  "remark": "转账备注"
}
```

### 5. 消费

#### 接口信息
- **请求方式**: POST
- **接口路径**: `/api/v1/wallet/{userId}/consume`
- **接口描述**: 钱包消费

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID（路径参数） |

#### 请求体

```json
{
  "amount": 50,
  "orderNo": "ORDER_20240101001",
  "description": "购买商品"
}
```

### 6. 退款

#### 接口信息
- **请求方式**: POST
- **接口路径**: `/api/v1/wallet/{userId}/refund`
- **接口描述**: 钱包退款

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID（路径参数） |

#### 请求体

```json
{
  "amount": 50,
  "orderNo": "ORDER_20240101001",
  "reason": "商品退款"
}
```

### 7. 交易记录

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/wallet/{userId}/transactions`
- **接口描述**: 获取钱包交易记录

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| userId | Long | 是 | - | 用户ID（路径参数） |
| type | String | 否 | - | 交易类型 |
| currentPage | Integer | 否 | 1 | 当前页码 |
| pageSize | Integer | 否 | 20 | 页面大小 |

#### 请求示例
```http
GET /api/v1/wallet/123/transactions?type=RECHARGE&currentPage=1&pageSize=10
```

### 8. 交易详情

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/wallet/transaction/{transactionId}`
- **接口描述**: 获取交易详情

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| transactionId | Long | 是 | 交易ID（路径参数） |

#### 请求示例
```http
GET /api/v1/wallet/transaction/1
```

### 9. 钱包统计

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/wallet/{userId}/statistics`
- **接口描述**: 获取钱包统计信息

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID（路径参数） |
| startTime | String | 否 | 开始时间 |
| endTime | String | 否 | 结束时间 |

#### 请求示例
```http
GET /api/v1/wallet/123/statistics?startTime=2024-01-01&endTime=2024-12-31
```

### 10. 冻结余额

#### 接口信息
- **请求方式**: POST
- **接口路径**: `/api/v1/wallet/{userId}/freeze`
- **接口描述**: 冻结钱包余额

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID（路径参数） |

#### 请求体

```json
{
  "amount": 100,
  "reason": "订单冻结",
  "orderNo": "ORDER_20240101001"
}
```

### 11. 解冻余额

#### 接口信息
- **请求方式**: POST
- **接口路径**: `/api/v1/wallet/{userId}/unfreeze`
- **接口描述**: 解冻钱包余额

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID（路径参数） |

#### 请求体

```json
{
  "amount": 100,
  "reason": "订单完成",
  "orderNo": "ORDER_20240101001"
}
```

### 12. 设置支付密码

#### 接口信息
- **请求方式**: POST
- **接口路径**: `/api/v1/wallet/{userId}/payment-password`
- **接口描述**: 设置支付密码

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID（路径参数） |

#### 请求体

```json
{
  "paymentPassword": "123456",
  "confirmPassword": "123456"
}
```

### 13. 验证支付密码

#### 接口信息
- **请求方式**: POST
- **接口路径**: `/api/v1/wallet/{userId}/verify-payment-password`
- **接口描述**: 验证支付密码

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID（路径参数） |

#### 请求体

```json
{
  "paymentPassword": "123456"
}
```

### 14. 绑定银行卡

#### 接口信息
- **请求方式**: POST
- **接口路径**: `/api/v1/wallet/{userId}/bind-bank-card`
- **接口描述**: 绑定银行卡

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID（路径参数） |

#### 请求体

```json
{
  "bankCard": "6222021234567890123",
  "bankName": "中国银行",
  "realName": "张三",
  "idCard": "身份证号"
}
```

### 15. 银行卡列表

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/wallet/{userId}/bank-cards`
- **接口描述**: 获取用户银行卡列表

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID（路径参数） |

#### 请求示例
```http
GET /api/v1/wallet/123/bank-cards
```

### 16. 解绑银行卡

#### 接口信息
- **请求方式**: DELETE
- **接口路径**: `/api/v1/wallet/{userId}/bank-card/{cardId}`
- **接口描述**: 解绑银行卡

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID（路径参数） |
| cardId | Long | 是 | 银行卡ID（路径参数） |

#### 请求示例
```http
DELETE /api/v1/wallet/123/bank-card/1
```

### 17. 钱包流水

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/wallet/{userId}/flow`
- **接口描述**: 获取钱包流水

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| userId | Long | 是 | - | 用户ID（路径参数） |
| currentPage | Integer | 否 | 1 | 当前页码 |
| pageSize | Integer | 否 | 20 | 页面大小 |

#### 请求示例
```http
GET /api/v1/wallet/123/flow?currentPage=1&pageSize=10
```

### 18. 钱包余额变动

#### 接口信息
- **请求方式**: POST
- **接口路径**: `/api/v1/wallet/{userId}/balance-change`
- **接口描述**: 钱包余额变动

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID（路径参数） |

#### 请求体

```json
{
  "amount": 100,
  "type": "INCREASE",
  "reason": "系统奖励",
  "remark": "活动奖励"
}
```

### 19. 钱包安全设置

#### 接口信息
- **请求方式**: PUT
- **接口路径**: `/api/v1/wallet/{userId}/security`
- **接口描述**: 更新钱包安全设置

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID（路径参数） |

#### 请求体

```json
{
  "enablePaymentPassword": true,
  "enableSmsVerification": true,
  "maxDailyAmount": 10000
}
```

### 20. 钱包状态查询

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/wallet/{userId}/status`
- **接口描述**: 查询钱包状态

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID（路径参数） |

#### 请求示例
```http
GET /api/v1/wallet/123/status
```

## 钱包记录字段说明

| 字段名 | 类型 | 描述 |
|--------|------|------|
| id | Long | 钱包ID |
| userId | Long | 用户ID |
| balance | Long | 余额（分） |
| frozenBalance | Long | 冻结余额（分） |
| currency | String | 货币类型 |
| status | String | 钱包状态 |
| createTime | String | 创建时间 |
| updateTime | String | 更新时间 |

## 交易记录字段说明

| 字段名 | 类型 | 描述 |
|--------|------|------|
| id | Long | 交易ID |
| userId | Long | 用户ID |
| type | String | 交易类型 |
| amount | Long | 交易金额（分） |
| balance | Long | 交易后余额（分） |
| orderNo | String | 关联订单号 |
| description | String | 交易描述 |
| status | String | 交易状态 |
| createTime | String | 创建时间 |

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
5. 钱包状态包括：ACTIVE（活跃）、FROZEN（冻结）、CLOSED（关闭）
6. 交易类型包括：RECHARGE（充值）、WITHDRAW（提现）、TRANSFER（转账）、CONSUME（消费）、REFUND（退款）
7. 交易状态包括：PENDING（处理中）、SUCCESS（成功）、FAILED（失败）
8. 支付方式包括：ALIPAY（支付宝）、WECHAT（微信支付）、BANK（银行卡）
9. 货币类型包括：CNY（人民币）、USD（美元）、EUR（欧元）
