# 标签管理API接口文档

## 基本信息
- **控制器名称**: TagController
- **基础路径**: `/api/v1/tags`
- **描述**: 基础标签的增删改查和管理功能

## 接口列表

### 1. 标签列表查询

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/tags/list`
- **接口描述**: 支持按类型、状态、使用频率等条件查询标签列表

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| tagType | String | 否 | - | 标签类型 |
| status | String | 否 | - | 标签状态 |
| keyword | String | 否 | - | 关键词搜索 |
| isHot | Boolean | 否 | - | 是否热门 |
| minUsageCount | Long | 否 | - | 最小使用次数 |
| orderBy | String | 否 | usageCount | 排序字段 |
| orderDirection | String | 否 | DESC | 排序方向 |
| currentPage | Integer | 否 | 1 | 当前页码 |
| pageSize | Integer | 否 | 20 | 页面大小 |

#### 请求示例
```http
GET /api/v1/tags/list?tagType=CONTENT&status=ACTIVE&isHot=true&currentPage=1&pageSize=10
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
        "name": "标签名称",
        "description": "标签描述",
        "tagType": "CONTENT",
        "status": "ACTIVE",
        "isHot": true,
        "usageCount": 1500,
        "icon": "https://example.com/icon.png",
        "color": "#FF6B6B",
        "createTime": "2024-01-01T00:00:00",
        "updateTime": "2024-01-01T00:00:00"
      }
    ],
    "total": 200,
    "currentPage": 1,
    "pageSize": 10,
    "totalPages": 20
  }
}
```

#### 响应字段说明

| 字段名 | 类型 | 描述 |
|--------|------|------|
| code | Integer | 响应状态码 |
| message | String | 响应消息 |
| data.records | Array | 标签记录列表 |
| data.total | Integer | 总记录数 |
| data.currentPage | Integer | 当前页码 |
| data.pageSize | Integer | 页面大小 |
| data.totalPages | Integer | 总页数 |

## 标签记录字段说明

| 字段名 | 类型 | 描述 |
|--------|------|------|
| id | Long | 标签ID |
| name | String | 标签名称 |
| description | String | 标签描述 |
| tagType | String | 标签类型 |
| status | String | 标签状态 |
| isHot | Boolean | 是否热门 |
| usageCount | Long | 使用次数 |
| icon | String | 标签图标 |
| color | String | 标签颜色 |
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
5. 标签类型包括：CONTENT（内容标签）、USER（用户标签）、CATEGORY（分类标签）
6. 标签状态包括：ACTIVE（激活）、INACTIVE（非激活）、DELETED（已删除）
7. 排序字段支持：usageCount（使用次数）、createTime（创建时间）、updateTime（更新时间）
8. 通过TagFacadeService可以调用更多标签相关功能
