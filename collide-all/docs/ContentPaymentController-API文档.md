# 内容付费管理API接口文档

## 基本信息
- **控制器名称**: ContentPaymentController
- **基础路径**: `/api/v1/content/payment`
- **描述**: 内容付费配置、价格管理、付费策略等功能

## 接口列表

### 1. 分页查询付费配置

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/content/payment/configs`
- **接口描述**: 支持按内容ID、付费类型等条件分页查询付费配置

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| contentId | Long | 否 | - | 内容ID |
| paymentType | String | 否 | - | 付费类型 |
| status | String | 否 | - | 配置状态 |
| currentPage | Integer | 否 | 1 | 当前页码 |
| pageSize | Integer | 否 | 20 | 页面大小 |
| orderBy | String | 否 | createTime | 排序字段 |
| orderDirection | String | 否 | DESC | 排序方向 |

#### 请求示例
```http
GET /api/v1/content/payment/configs?contentId=1&paymentType=CHAPTER&status=ACTIVE&currentPage=1&pageSize=10
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
        "contentId": 1,
        "paymentType": "CHAPTER",
        "price": 0.99,
        "currency": "CNY",
        "status": "ACTIVE",
        "description": "章节付费配置",
        "createTime": "2024-01-01T00:00:00",
        "updateTime": "2024-01-01T00:00:00"
      }
    ],
    "total": 50,
    "currentPage": 1,
    "pageSize": 10,
    "totalPages": 5
  }
}
```

#### 响应字段说明

| 字段名 | 类型 | 描述 |
|--------|------|------|
| code | Integer | 响应状态码 |
| message | String | 响应消息 |
| data.records | Array | 付费配置记录列表 |
| data.total | Integer | 总记录数 |
| data.currentPage | Integer | 当前页码 |
| data.pageSize | Integer | 页面大小 |
| data.totalPages | Integer | 总页数 |

### 2. 获取内容付费配置

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/content/payment/configs/{contentId}`
- **接口描述**: 根据内容ID获取付费配置详情

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| contentId | Long | 是 | - | 内容ID（路径参数） |

#### 请求示例
```http
GET /api/v1/content/payment/configs/1
```

### 3. 获取付费类型列表

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/content/payment/types`
- **接口描述**: 获取所有支持的付费类型

#### 请求示例
```http
GET /api/v1/content/payment/types
```

## 付费配置字段说明

| 字段名 | 类型 | 描述 |
|--------|------|------|
| id | Long | 配置ID |
| contentId | Long | 内容ID |
| paymentType | String | 付费类型 |
| price | BigDecimal | 价格 |
| currency | String | 货币类型 |
| status | String | 配置状态 |
| description | String | 配置描述 |
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
4. 排序方向支持ASC（升序）和DESC（降序）
5. 付费类型包括：CHAPTER（章节付费）、CONTENT（内容付费）、SUBSCRIPTION（订阅付费）
6. 配置状态包括：ACTIVE（激活）、INACTIVE（非激活）、DELETED（已删除）
7. 价格字段使用BigDecimal类型，支持精确计算
