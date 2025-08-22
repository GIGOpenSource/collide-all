# 内容标签管理API接口文档

## 基本信息
- **控制器名称**: ContentTagController
- **基础路径**: `/api/v1/content-tags`
- **描述**: 内容与标签的关联管理、标签分类、内容标签化等功能

## 接口列表

### 1. 分页查询内容标签关联

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/content-tags/query`
- **接口描述**: 支持按内容ID、标签ID等条件分页查询内容标签关联

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| contentId | Long | 否 | - | 内容ID |
| tagId | Long | 否 | - | 标签ID |
| status | String | 否 | - | 关联状态 |
| currentPage | Integer | 否 | 1 | 当前页码 |
| pageSize | Integer | 否 | 20 | 页面大小 |
| orderBy | String | 否 | createTime | 排序字段 |
| orderDirection | String | 否 | DESC | 排序方向 |

#### 请求示例
```http
GET /api/v1/content-tags/query?contentId=1&status=ACTIVE&currentPage=1&pageSize=10
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
        "tagId": 5,
        "tagName": "科幻",
        "tagDescription": "科幻类内容",
        "status": "ACTIVE",
        "createTime": "2024-01-01T00:00:00",
        "updateTime": "2024-01-01T00:00:00"
      }
    ],
    "total": 25,
    "currentPage": 1,
    "pageSize": 10,
    "totalPages": 3
  }
}
```

#### 响应字段说明

| 字段名 | 类型 | 描述 |
|--------|------|------|
| code | Integer | 响应状态码 |
| message | String | 响应消息 |
| data.records | Array | 内容标签关联记录列表 |
| data.total | Integer | 总记录数 |
| data.currentPage | Integer | 当前页码 |
| data.pageSize | Integer | 页面大小 |
| data.totalPages | Integer | 总页数 |

### 2. 获取内容的标签列表

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/content-tags/content/{contentId}`
- **接口描述**: 获取指定内容的所有标签

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| contentId | Long | 是 | - | 内容ID（路径参数） |
| status | String | 否 | - | 标签状态 |

#### 请求示例
```http
GET /api/v1/content-tags/content/1?status=ACTIVE
```

### 3. 获取标签的内容列表

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/content-tags/tag/{tagId}`
- **接口描述**: 获取指定标签下的所有内容

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| tagId | Long | 是 | - | 标签ID（路径参数） |
| status | String | 否 | - | 内容状态 |
| currentPage | Integer | 否 | 1 | 当前页码 |
| pageSize | Integer | 否 | 20 | 页面大小 |

#### 请求示例
```http
GET /api/v1/content-tags/tag/5?status=PUBLISHED&currentPage=1&pageSize=20
```

## 内容标签关联字段说明

| 字段名 | 类型 | 描述 |
|--------|------|------|
| id | Long | 关联ID |
| contentId | Long | 内容ID |
| tagId | Long | 标签ID |
| tagName | String | 标签名称 |
| tagDescription | String | 标签描述 |
| status | String | 关联状态 |
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
5. 关联状态包括：ACTIVE（激活）、INACTIVE（非激活）、DELETED（已删除）
6. 内容状态包括：DRAFT（草稿）、PUBLISHED（已发布）、ARCHIVED（已归档）
7. 标签状态包括：ACTIVE（活跃）、INACTIVE（非活跃）、DELETED（已删除）
