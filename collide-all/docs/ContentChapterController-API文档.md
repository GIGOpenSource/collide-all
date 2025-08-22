# 内容章节管理API接口文档

## 基本信息
- **控制器名称**: ContentChapterController
- **基础路径**: `/api/v1/content/chapters`
- **描述**: 内容章节的增删改查、排序管理等功能

## 接口列表

### 1. 分页查询章节列表

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/content/chapters/query`
- **接口描述**: 支持按内容ID、状态等条件分页查询章节列表

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| contentId | Long | 否 | - | 内容ID |
| status | String | 否 | - | 章节状态 |
| currentPage | Integer | 否 | 1 | 当前页码 |
| pageSize | Integer | 否 | 20 | 页面大小 |
| orderBy | String | 否 | chapterOrder | 排序字段 |
| orderDirection | String | 否 | ASC | 排序方向 |

#### 请求示例
```http
GET /api/v1/content/chapters/query?contentId=1&status=PUBLISHED&currentPage=1&pageSize=10
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
        "title": "第一章",
        "content": "章节内容",
        "chapterOrder": 1,
        "status": "PUBLISHED",
        "wordCount": 1500,
        "createTime": "2024-01-01T00:00:00",
        "updateTime": "2024-01-01T00:00:00"
      }
    ],
    "total": 20,
    "currentPage": 1,
    "pageSize": 10,
    "totalPages": 2
  }
}
```

#### 响应字段说明

| 字段名 | 类型 | 描述 |
|--------|------|------|
| code | Integer | 响应状态码 |
| message | String | 响应消息 |
| data.records | Array | 章节记录列表 |
| data.total | Integer | 总记录数 |
| data.currentPage | Integer | 当前页码 |
| data.pageSize | Integer | 页面大小 |
| data.totalPages | Integer | 总页数 |

### 2. 获取章节详情

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/content/chapters/{chapterId}`
- **接口描述**: 根据章节ID获取章节详细信息

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| chapterId | Long | 是 | - | 章节ID（路径参数） |

#### 请求示例
```http
GET /api/v1/content/chapters/1
```

### 3. 获取内容的所有章节

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/content/chapters/content/{contentId}`
- **接口描述**: 获取指定内容的所有章节列表

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| contentId | Long | 是 | - | 内容ID（路径参数） |
| status | String | 否 | - | 章节状态 |

#### 请求示例
```http
GET /api/v1/content/chapters/content/1?status=PUBLISHED
```

## 章节记录字段说明

| 字段名 | 类型 | 描述 |
|--------|------|------|
| id | Long | 章节ID |
| contentId | Long | 所属内容ID |
| title | String | 章节标题 |
| content | String | 章节内容 |
| chapterOrder | Integer | 章节顺序 |
| status | String | 章节状态 |
| wordCount | Integer | 字数统计 |
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
5. 章节状态包括：DRAFT（草稿）、PUBLISHED（已发布）、ARCHIVED（已归档）
6. 章节按chapterOrder字段排序，确保阅读顺序正确
