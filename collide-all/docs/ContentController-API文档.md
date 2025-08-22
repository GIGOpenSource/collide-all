# 内容管理API接口文档

## 基本信息
- **控制器名称**: ContentController
- **基础路径**: `/api/v1/content/core`
- **描述**: 内容的创建、更新、查询、发布等管理接口（极简版）

## 接口列表

### 1. 内容列表查询

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/content/core/list`
- **接口描述**: 支持按类型、状态、作者等条件查询内容列表

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| contentType | String | 否 | - | 内容类型 |
| status | String | 否 | - | 内容状态 |
| authorId | Long | 否 | - | 作者ID |
| categoryId | Long | 否 | - | 分类ID |
| keyword | String | 否 | - | 关键词搜索 |
| isPublished | Boolean | 否 | - | 是否发布 |
| orderBy | String | 否 | createTime | 排序字段 |
| orderDirection | String | 否 | DESC | 排序方向 |
| currentPage | Integer | 否 | 1 | 当前页码 |
| pageSize | Integer | 否 | 20 | 页面大小 |

#### 请求示例
```http
GET /api/v1/content/core/list?contentType=ARTICLE&status=PUBLISHED&currentPage=1&pageSize=10
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
        "title": "内容标题",
        "content": "内容摘要",
        "contentType": "ARTICLE",
        "status": "PUBLISHED",
        "authorId": 123,
        "authorNickname": "作者昵称",
        "categoryId": 5,
        "categoryName": "分类名称",
        "isPublished": true,
        "viewCount": 1000,
        "likeCount": 50,
        "commentCount": 20,
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
| data.records | Array | 内容记录列表 |
| data.total | Integer | 总记录数 |
| data.currentPage | Integer | 当前页码 |
| data.pageSize | Integer | 页面大小 |
| data.totalPages | Integer | 总页数 |

## 内容记录字段说明

| 字段名 | 类型 | 描述 |
|--------|------|------|
| id | Long | 内容ID |
| title | String | 内容标题 |
| content | String | 内容摘要 |
| contentType | String | 内容类型 |
| status | String | 内容状态 |
| authorId | Long | 作者ID |
| authorNickname | String | 作者昵称 |
| categoryId | Long | 分类ID |
| categoryName | String | 分类名称 |
| isPublished | Boolean | 是否发布 |
| viewCount | Integer | 浏览次数 |
| likeCount | Integer | 点赞次数 |
| commentCount | Integer | 评论次数 |
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
5. 内容类型包括：ARTICLE（文章）、VIDEO（视频）、AUDIO（音频）、IMAGE（图片）
6. 内容状态包括：DRAFT（草稿）、PUBLISHED（已发布）、ARCHIVED（已归档）、DELETED（已删除）
7. 排序字段支持：createTime（创建时间）、updateTime（更新时间）、viewCount（浏览次数）、likeCount（点赞次数）
8. 通过ContentFacadeService可以调用更多内容相关功能
