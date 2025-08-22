# 广告管理API接口文档

## 基本信息
- **控制器名称**: AdsController
- **基础路径**: `/api/v1/ads`
- **描述**: 广告管理相关接口

## 接口列表

### 1. 广告列表查询

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/ads/list`
- **接口描述**: 支持按类型、状态、位置等条件查询广告列表

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| adType | String | 否 | - | 广告类型 |
| keyword | String | 否 | - | 关键词搜索 |
| isValid | Boolean | 否 | - | 是否有效 |
| orderBy | String | 否 | createTime | 排序字段 |
| orderDirection | String | 否 | DESC | 排序方向 |
| currentPage | Integer | 否 | 1 | 当前页码 |
| pageSize | Integer | 否 | 20 | 页面大小 |

#### 请求示例
```http
GET /api/v1/ads/list?adType=BANNER&isValid=true&currentPage=1&pageSize=10
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
        "adType": "BANNER",
        "title": "广告标题",
        "content": "广告内容",
        "imageUrl": "https://example.com/image.jpg",
        "linkUrl": "https://example.com",
        "isValid": true,
        "startTime": "2024-01-01T00:00:00",
        "endTime": "2024-12-31T23:59:59",
        "createTime": "2024-01-01T00:00:00",
        "updateTime": "2024-01-01T00:00:00"
      }
    ],
    "total": 100,
    "currentPage": 1,
    "pageSize": 20,
    "totalPages": 5
  }
}
```

#### 响应字段说明

| 字段名 | 类型 | 描述 |
|--------|------|------|
| code | Integer | 响应状态码 |
| message | String | 响应消息 |
| data.records | Array | 广告记录列表 |
| data.total | Integer | 总记录数 |
| data.currentPage | Integer | 当前页码 |
| data.pageSize | Integer | 页面大小 |
| data.totalPages | Integer | 总页数 |

| 字段名 | 类型 | 描述 |
|--------|------|------|
| code | Integer | 响应状态码 |
| message | String | 响应消息 |
| data.records | Array | 广告记录列表 |
| data.total | Integer | 总记录数 |
| data.currentPage | Integer | 当前页码 |
| data.pageSize | Integer | 页面大小 |
| data.totalPages | Integer | 总页数 |

#### 广告记录字段说明

| 字段名 | 类型 | 描述 |
|--------|------|------|
| id | Long | 广告ID |
| adType | String | 广告类型 |
| title | String | 广告标题 |
| content | String | 广告内容 |
| imageUrl | String | 广告图片URL |
| linkUrl | String | 广告链接URL |
| isValid | Boolean | 是否有效 |
| startTime | String | 开始时间 |
| endTime | String | 结束时间 |
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
5. 广告类型包括：BANNER（横幅）、VIDEO（视频）、TEXT（文字）等
