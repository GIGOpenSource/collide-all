# 搜索服务API接口文档

## 基本信息
- **控制器名称**: SearchController
- **基础路径**: `/api/v1/search`
- **描述**: 搜索服务相关接口

## 接口列表

### 1. 执行搜索

#### 接口信息
- **请求方式**: POST
- **接口路径**: `/api/v1/search`
- **接口描述**: 根据关键词和搜索类型执行搜索，支持分页和排序

#### 请求参数

```json
{
  "keyword": "搜索关键词",
  "searchType": "MIXED",
  "userId": 123,
  "currentPage": 1,
  "pageSize": 20,
  "orderBy": "relevance",
  "orderDirection": "DESC",
  "filters": {
    "categoryId": 1,
    "dateRange": "7d",
    "contentType": "NOVEL"
  }
}
```

#### 响应参数

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "keyword": "搜索关键词",
    "searchType": "MIXED",
    "totalCount": 100,
    "currentPage": 1,
    "pageSize": 20,
    "totalPages": 5,
    "results": {
      "contents": [
        {
          "id": 1,
          "title": "内容标题",
          "description": "内容描述",
          "contentType": "NOVEL",
          "authorName": "作者名称",
          "relevanceScore": 0.95
        }
      ],
      "users": [
        {
          "id": 123,
          "nickname": "用户昵称",
          "avatar": "https://example.com/avatar.jpg",
          "relevanceScore": 0.85
        }
      ],
      "tags": [
        {
          "id": 1,
          "name": "标签名称",
          "relevanceScore": 0.75
        }
      ]
    },
    "searchTime": 150,
    "suggestions": ["建议1", "建议2"]
  }
}
```

### 2. 获取搜索历史

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/search/history`
- **接口描述**: 分页获取用户的搜索历史记录

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| userId | Long | 是 | - | 用户ID |
| currentPage | Integer | 否 | 1 | 当前页码 |
| pageSize | Integer | 否 | 20 | 页面大小 |
| searchType | String | 否 | - | 搜索类型 |
| startTime | String | 否 | - | 开始时间 |
| endTime | String | 否 | - | 结束时间 |

#### 请求示例
```http
GET /api/v1/search/history?userId=123&currentPage=1&pageSize=10
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
        "userId": 123,
        "keyword": "搜索关键词",
        "searchType": "MIXED",
        "searchTime": "2024-01-01T00:00:00",
        "resultCount": 50
      }
    ],
    "total": 100,
    "currentPage": 1,
    "pageSize": 20,
    "totalPages": 5
  }
}
```

### 3. 清空搜索历史

#### 接口信息
- **请求方式**: DELETE
- **接口路径**: `/api/v1/search/history/user/{userId}`
- **接口描述**: 清空指定用户的所有搜索历史记录

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID（路径参数） |

#### 请求示例
```http
DELETE /api/v1/search/history/user/123
```

### 4. 删除指定搜索历史

#### 接口信息
- **请求方式**: DELETE
- **接口路径**: `/api/v1/search/history/{historyId}`
- **接口描述**: 删除指定的搜索历史记录

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| historyId | Long | 是 | 搜索历史ID（路径参数） |

#### 请求示例
```http
DELETE /api/v1/search/history/1
```

### 5. 获取热门搜索

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/search/hot`
- **接口描述**: 获取当前热门搜索关键词列表

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| limit | Integer | 否 | 10 | 返回数量限制 |

#### 请求示例
```http
GET /api/v1/search/hot?limit=20
```

#### 响应参数

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "keyword": "热门关键词",
      "searchCount": 1000,
      "trend": "UP",
      "searchType": "MIXED"
    }
  ]
}
```

### 6. 根据类型获取热搜

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/search/hot/type/{searchType}`
- **接口描述**: 根据搜索类型获取热门关键词

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| searchType | String | 是 | - | 搜索类型（路径参数） |
| limit | Integer | 否 | 10 | 返回数量限制 |

#### 请求示例
```http
GET /api/v1/search/hot/type/CONTENT?limit=15
```

### 7. 获取搜索建议

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/search/suggestions`
- **接口描述**: 根据输入关键词获取搜索建议和自动补全

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| keyword | String | 是 | - | 搜索关键词 |
| limit | Integer | 否 | 10 | 返回数量限制 |

#### 请求示例
```http
GET /api/v1/search/suggestions?keyword=小说&limit=10
```

#### 响应参数

```json
{
  "code": 200,
  "message": "success",
  "data": ["小说推荐", "小说排行榜", "小说下载"]
}
```

### 8. 获取用户搜索偏好

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/search/preferences/{userId}`
- **接口描述**: 分析并获取用户的搜索偏好和习惯

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID（路径参数） |

#### 请求示例
```http
GET /api/v1/search/preferences/123
```

#### 响应参数

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "keyword": "偏好关键词",
      "searchCount": 50,
      "lastSearchTime": "2024-01-01T00:00:00",
      "searchType": "CONTENT"
    }
  ]
}
```

### 9. 根据标签搜索混合内容

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/search/tag/{tag}/mixed`
- **接口描述**: 根据标签搜索混合类型的内容

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| tag | String | 是 | - | 标签名称（路径参数） |
| currentPage | Integer | 否 | 1 | 当前页码 |
| pageSize | Integer | 否 | 20 | 页面大小 |

#### 请求示例
```http
GET /api/v1/search/tag/科幻/mixed?currentPage=1&pageSize=10
```

### 10. 根据标签搜索用户

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/search/tag/{tag}/users`
- **接口描述**: 根据标签搜索相关用户

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| tag | String | 是 | - | 标签名称（路径参数） |
| currentPage | Integer | 否 | 1 | 当前页码 |
| pageSize | Integer | 否 | 20 | 页面大小 |

#### 请求示例
```http
GET /api/v1/search/tag/作家/users?currentPage=1&pageSize=10
```

### 11. 根据标签搜索内容

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/search/tag/{tag}/contents`
- **接口描述**: 根据标签搜索相关内容

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| tag | String | 是 | - | 标签名称（路径参数） |
| currentPage | Integer | 否 | 1 | 当前页码 |
| pageSize | Integer | 否 | 20 | 页面大小 |
| contentType | String | 否 | - | 内容类型 |

#### 请求示例
```http
GET /api/v1/search/tag/科幻/contents?currentPage=1&pageSize=10&contentType=NOVEL
```

## 搜索记录字段说明

| 字段名 | 类型 | 描述 |
|--------|------|------|
| id | Long | 搜索历史ID |
| userId | Long | 用户ID |
| keyword | String | 搜索关键词 |
| searchType | String | 搜索类型 |
| searchTime | String | 搜索时间 |
| resultCount | Integer | 结果数量 |

## 热门搜索字段说明

| 字段名 | 类型 | 描述 |
|--------|------|------|
| keyword | String | 搜索关键词 |
| searchCount | Integer | 搜索次数 |
| trend | String | 趋势（UP/DOWN/STABLE） |
| searchType | String | 搜索类型 |

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
4. 搜索类型包括：MIXED（混合）、CONTENT（内容）、USER（用户）、TAG（标签）
5. 内容类型包括：NOVEL（小说）、COMIC（漫画）、VIDEO（视频）、AUDIO（音频）
6. 搜索建议基于用户历史搜索和热门搜索生成
7. 搜索结果按相关性分数排序
