# 商品管理API接口文档

## 基本信息
- **控制器名称**: GoodsController
- **基础路径**: `/api/v1/goods`
- **描述**: 商品的增删改查、库存管理、统计分析等功能

## 接口列表

### 1. 商品列表查询

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/goods/list`
- **接口描述**: 支持按类型、状态、价格等条件查询商品列表

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| goodsType | String | 否 | - | 商品类型 |
| status | String | 否 | - | 商品状态 |
| categoryId | Long | 否 | - | 分类ID |
| sellerId | Long | 否 | - | 卖家ID |
| keyword | String | 否 | - | 关键词搜索 |
| minPrice | Long | 否 | - | 最小价格 |
| maxPrice | Long | 否 | - | 最大价格 |
| hasStock | Boolean | 否 | - | 是否有库存 |
| orderBy | String | 否 | createTime | 排序字段 |
| orderDirection | String | 否 | DESC | 排序方向 |
| currentPage | Integer | 否 | 1 | 当前页码 |
| pageSize | Integer | 否 | 20 | 页面大小 |

#### 请求示例
```http
GET /api/v1/goods/list?goodsType=DIGITAL&status=ACTIVE&minPrice=100&maxPrice=1000&currentPage=1&pageSize=10
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
        "name": "商品名称",
        "description": "商品描述",
        "goodsType": "DIGITAL",
        "status": "ACTIVE",
        "categoryId": 5,
        "categoryName": "分类名称",
        "sellerId": 123,
        "sellerName": "卖家名称",
        "price": 299,
        "originalPrice": 399,
        "stock": 100,
        "hasStock": true,
        "coverImage": "https://example.com/cover.jpg",
        "images": ["https://example.com/img1.jpg", "https://example.com/img2.jpg"],
        "createTime": "2024-01-01T00:00:00",
        "updateTime": "2024-01-01T00:00:00"
      }
    ],
    "total": 150,
    "currentPage": 1,
    "pageSize": 10,
    "totalPages": 15
  }
}
```

#### 响应字段说明

| 字段名 | 类型 | 描述 |
|--------|------|------|
| code | Integer | 响应状态码 |
| message | String | 响应消息 |
| data.records | Array | 商品记录列表 |
| data.total | Integer | 总记录数 |
| data.currentPage | Integer | 当前页码 |
| data.pageSize | Integer | 页面大小 |
| data.totalPages | Integer | 总页数 |

## 商品记录字段说明

| 字段名 | 类型 | 描述 |
|--------|------|------|
| id | Long | 商品ID |
| name | String | 商品名称 |
| description | String | 商品描述 |
| goodsType | String | 商品类型 |
| status | String | 商品状态 |
| categoryId | Long | 分类ID |
| categoryName | String | 分类名称 |
| sellerId | Long | 卖家ID |
| sellerName | String | 卖家名称 |
| price | Long | 当前价格（分） |
| originalPrice | Long | 原价（分） |
| stock | Integer | 库存数量 |
| hasStock | Boolean | 是否有库存 |
| coverImage | String | 封面图片 |
| images | Array | 商品图片列表 |
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
5. 商品类型包括：DIGITAL（数字商品）、PHYSICAL（实物商品）、SERVICE（服务商品）
6. 商品状态包括：ACTIVE（上架）、INACTIVE（下架）、DELETED（已删除）、DRAFT（草稿）
7. 价格字段以分为单位，避免浮点数精度问题
8. 排序字段支持：createTime（创建时间）、updateTime（更新时间）、price（价格）、stock（库存）
9. 通过GoodsFacadeService可以调用更多商品相关功能
