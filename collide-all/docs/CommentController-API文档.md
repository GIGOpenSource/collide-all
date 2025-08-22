# 评论管理API接口文档

## 基本信息
- **控制器名称**: CommentController
- **基础路径**: `/api/v1/comments`
- **描述**: 评论相关的API接口，支持内容评论和动态评论，包含多级评论和回复功能

## 接口列表

### 1. 评论列表查询

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/comments/list`
- **接口描述**: 支持按类型、目标、用户等条件查询评论列表

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| commentType | String | 否 | - | 评论类型：CONTENT、DYNAMIC |
| targetId | Long | 否 | - | 目标对象ID |
| userId | Long | 否 | - | 用户ID |
| parentId | Long | 否 | - | 父评论ID |
| status | String | 否 | - | 评论状态：NORMAL、HIDDEN、DELETED |
| keyword | String | 否 | - | 关键词搜索 |
| orderBy | String | 否 | createTime | 排序字段 |
| orderDirection | String | 否 | DESC | 排序方向：ASC、DESC |
| currentPage | Integer | 否 | 1 | 当前页码 |
| pageSize | Integer | 否 | 20 | 页面大小 |

#### 请求示例
```http
GET /api/v1/comments/list?commentType=CONTENT&targetId=123&currentPage=1&pageSize=10
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
        "commentType": "CONTENT",
        "targetId": 123,
        "parentCommentId": 0,
        "content": "这是一条评论内容",
        "userId": 456,
        "userNickname": "用户昵称",
        "userAvatar": "https://example.com/avatar.jpg",
        "replyToUserId": null,
        "replyToUserNickname": null,
        "replyToUserAvatar": null,
        "status": "NORMAL",
        "likeCount": 10,
        "replyCount": 5,
        "createTime": "2024-01-01T10:30:00",
        "updateTime": "2024-01-01T10:30:00",
        "children": [],
        "level": 0,
        "commentPath": "1"
      }
    ],
    "total": 100,
    "currentPage": 1,
    "pageSize": 20,
    "totalPages": 5
  }
}
```

## 注意事项

**重要说明**: 当前CommentController已实现评论列表查询接口。其他接口（如创建、更新、删除等）需要通过CommentFacadeService直接调用，或者需要后续在Controller层实现。

## 评论门面服务接口（通过FacadeService调用）

### 2. 创建评论

#### 接口信息
- **请求方式**: POST
- **接口路径**: `/api/v1/comment/create` (通过FacadeService调用)
- **接口描述**: 创建评论，支持根评论和回复评论

#### 请求参数

```json
{
  "commentType": "CONTENT",
  "targetId": 123,
  "parentCommentId": 0,
  "content": "这是一条评论内容",
  "userId": 456,
  "userNickname": "用户昵称",
  "userAvatar": "https://example.com/avatar.jpg",
  "replyToUserId": null,
  "replyToUserNickname": null,
  "replyToUserAvatar": null,
  "status": "NORMAL"
}
```

#### 响应参数

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "commentType": "CONTENT",
    "targetId": 123,
    "parentCommentId": 0,
    "content": "这是一条评论内容",
    "userId": 456,
    "userNickname": "用户昵称",
    "userAvatar": "https://example.com/avatar.jpg",
    "replyToUserId": null,
    "replyToUserNickname": null,
    "replyToUserAvatar": null,
    "status": "NORMAL",
    "likeCount": 0,
    "replyCount": 0,
    "createTime": "2024-01-01T10:30:00",
    "updateTime": "2024-01-01T10:30:00",
    "children": [],
    "level": 0,
    "commentPath": "1"
  }
}
```

### 3. 更新评论

#### 接口信息
- **请求方式**: PUT
- **接口路径**: `/api/v1/comment/update` (通过FacadeService调用)
- **接口描述**: 更新评论内容或状态

#### 请求参数

```json
{
  "id": 1,
  "content": "更新后的评论内容",
  "status": "NORMAL",
  "likeCount": 10,
  "replyCount": 5,
  "operatorId": 456,
  "updateType": "CONTENT"
}
```

#### 响应参数

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "commentType": "CONTENT",
    "targetId": 123,
    "parentCommentId": 0,
    "content": "更新后的评论内容",
    "userId": 456,
    "userNickname": "用户昵称",
    "userAvatar": "https://example.com/avatar.jpg",
    "replyToUserId": null,
    "replyToUserNickname": null,
    "replyToUserAvatar": null,
    "status": "NORMAL",
    "likeCount": 10,
    "replyCount": 5,
    "createTime": "2024-01-01T10:30:00",
    "updateTime": "2024-01-01T11:00:00",
    "children": [],
    "level": 0,
    "commentPath": "1"
  }
}
```

### 4. 删除评论

#### 接口信息
- **请求方式**: DELETE
- **接口路径**: `/api/v1/comment/{commentId}` (通过FacadeService调用)
- **接口描述**: 删除评论（逻辑删除）

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| commentId | Long | 是 | 评论ID（路径参数） |
| userId | Long | 是 | 操作用户ID（查询参数） |

#### 请求示例
```http
DELETE /api/v1/comment/1?userId=456
```

#### 响应参数

```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

### 5. 获取评论详情

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/comment/{commentId}`
- **接口描述**: 根据ID获取评论详情

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| commentId | Long | 是 | 评论ID（路径参数） |

#### 请求示例
```http
GET /api/v1/comment/1
```

#### 响应参数

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "commentType": "CONTENT",
    "targetId": 123,
    "parentCommentId": 0,
    "content": "这是一条评论内容",
    "userId": 456,
    "userNickname": "用户昵称",
    "userAvatar": "https://example.com/avatar.jpg",
    "replyToUserId": null,
    "replyToUserNickname": null,
    "replyToUserAvatar": null,
    "status": "NORMAL",
    "likeCount": 10,
    "replyCount": 5,
    "createTime": "2024-01-01T10:30:00",
    "updateTime": "2024-01-01T10:30:00",
    "children": [],
    "level": 0,
    "commentPath": "1"
  }
}
```

### 6. 获取目标对象评论列表

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/comment/target/{targetId}`
- **接口描述**: 获取指定内容或动态的评论列表

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| targetId | Long | 是 | - | 目标对象ID（路径参数） |
| commentType | String | 是 | - | 评论类型：CONTENT、DYNAMIC |
| parentCommentId | Long | 否 | 0 | 父评论ID，0表示获取根评论 |
| currentPage | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 页面大小 |

#### 请求示例
```http
GET /api/v1/comment/target/123?commentType=CONTENT&parentCommentId=0&currentPage=1&pageSize=10
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
        "commentType": "CONTENT",
        "targetId": 123,
        "parentCommentId": 0,
        "content": "这是一条评论内容",
        "userId": 456,
        "userNickname": "用户昵称",
        "userAvatar": "https://example.com/avatar.jpg",
        "replyToUserId": null,
        "replyToUserNickname": null,
        "replyToUserAvatar": null,
        "status": "NORMAL",
        "likeCount": 10,
        "replyCount": 5,
        "createTime": "2024-01-01T10:30:00",
        "updateTime": "2024-01-01T10:30:00",
        "children": [],
        "level": 0,
        "commentPath": "1"
      }
    ],
    "total": 50,
    "currentPage": 1,
    "pageSize": 10,
    "totalPages": 5
  }
}
```

### 7. 获取评论回复列表

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/comment/replies/{parentCommentId}`
- **接口描述**: 获取指定评论的回复列表

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| parentCommentId | Long | 是 | - | 父评论ID（路径参数） |
| currentPage | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 页面大小 |

#### 请求示例
```http
GET /api/v1/comment/replies/1?currentPage=1&pageSize=10
```

#### 响应参数

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 2,
        "commentType": "CONTENT",
        "targetId": 123,
        "parentCommentId": 1,
        "content": "这是一条回复内容",
        "userId": 789,
        "userNickname": "回复用户",
        "userAvatar": "https://example.com/avatar2.jpg",
        "replyToUserId": 456,
        "replyToUserNickname": "用户昵称",
        "replyToUserAvatar": "https://example.com/avatar.jpg",
        "status": "NORMAL",
        "likeCount": 2,
        "replyCount": 0,
        "createTime": "2024-01-01T11:00:00",
        "updateTime": "2024-01-01T11:00:00",
        "children": [],
        "level": 1,
        "commentPath": "1.2"
      }
    ],
    "total": 5,
    "currentPage": 1,
    "pageSize": 10,
    "totalPages": 1
  }
}
```

### 8. 获取评论树

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/comment/tree/{targetId}`
- **接口描述**: 获取带层级结构的评论树

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| targetId | Long | 是 | - | 目标对象ID（路径参数） |
| commentType | String | 是 | - | 评论类型：CONTENT、DYNAMIC |
| maxDepth | Integer | 否 | 3 | 最大层级深度 |
| currentPage | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 页面大小 |

#### 请求示例
```http
GET /api/v1/comment/tree/123?commentType=CONTENT&maxDepth=3&currentPage=1&pageSize=10
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
        "commentType": "CONTENT",
        "targetId": 123,
        "parentCommentId": 0,
        "content": "这是一条根评论",
        "userId": 456,
        "userNickname": "用户昵称",
        "userAvatar": "https://example.com/avatar.jpg",
        "replyToUserId": null,
        "replyToUserNickname": null,
        "replyToUserAvatar": null,
        "status": "NORMAL",
        "likeCount": 10,
        "replyCount": 2,
        "createTime": "2024-01-01T10:30:00",
        "updateTime": "2024-01-01T10:30:00",
        "children": [
          {
            "id": 2,
            "commentType": "CONTENT",
            "targetId": 123,
            "parentCommentId": 1,
            "content": "这是一条回复",
            "userId": 789,
            "userNickname": "回复用户",
            "userAvatar": "https://example.com/avatar2.jpg",
            "replyToUserId": 456,
            "replyToUserNickname": "用户昵称",
            "replyToUserAvatar": "https://example.com/avatar.jpg",
            "status": "NORMAL",
            "likeCount": 2,
            "replyCount": 0,
            "createTime": "2024-01-01T11:00:00",
            "updateTime": "2024-01-01T11:00:00",
            "children": [],
            "level": 1,
            "commentPath": "1.2"
          }
        ],
        "level": 0,
        "commentPath": "1"
      }
    ],
    "total": 10,
    "currentPage": 1,
    "pageSize": 10,
    "totalPages": 1
  }
}
```

### 9. 获取用户评论列表

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/comment/user/{userId}`
- **接口描述**: 获取用户的评论列表

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| userId | Long | 是 | - | 用户ID（路径参数） |
| commentType | String | 否 | - | 评论类型（可选） |
| currentPage | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 页面大小 |

#### 请求示例
```http
GET /api/v1/comment/user/456?commentType=CONTENT&currentPage=1&pageSize=10
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
        "commentType": "CONTENT",
        "targetId": 123,
        "parentCommentId": 0,
        "content": "用户发布的评论",
        "userId": 456,
        "userNickname": "用户昵称",
        "userAvatar": "https://example.com/avatar.jpg",
        "replyToUserId": null,
        "replyToUserNickname": null,
        "replyToUserAvatar": null,
        "status": "NORMAL",
        "likeCount": 10,
        "replyCount": 5,
        "createTime": "2024-01-01T10:30:00",
        "updateTime": "2024-01-01T10:30:00",
        "children": [],
        "level": 0,
        "commentPath": "1"
      }
    ],
    "total": 25,
    "currentPage": 1,
    "pageSize": 10,
    "totalPages": 3
  }
}
```

### 10. 获取用户收到的回复

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/comment/user/replies/{userId}`
- **接口描述**: 获取用户收到的回复列表

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| userId | Long | 是 | - | 用户ID（路径参数） |
| currentPage | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 页面大小 |

#### 请求示例
```http
GET /api/v1/comment/user/replies/456?currentPage=1&pageSize=10
```

#### 响应参数

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 2,
        "commentType": "CONTENT",
        "targetId": 123,
        "parentCommentId": 1,
        "content": "回复用户的内容",
        "userId": 789,
        "userNickname": "回复用户",
        "userAvatar": "https://example.com/avatar2.jpg",
        "replyToUserId": 456,
        "replyToUserNickname": "用户昵称",
        "replyToUserAvatar": "https://example.com/avatar.jpg",
        "status": "NORMAL",
        "likeCount": 2,
        "replyCount": 0,
        "createTime": "2024-01-01T11:00:00",
        "updateTime": "2024-01-01T11:00:00",
        "children": [],
        "level": 1,
        "commentPath": "1.2"
      }
    ],
    "total": 8,
    "currentPage": 1,
    "pageSize": 10,
    "totalPages": 1
  }
}
```

### 11. 增加评论点赞数

#### 接口信息
- **请求方式**: POST
- **接口路径**: `/api/v1/comment/like/{commentId}`
- **接口描述**: 增加评论点赞数

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| commentId | Long | 是 | - | 评论ID（路径参数） |
| increment | Integer | 否 | 1 | 增加数量 |

#### 请求示例
```http
POST /api/v1/comment/like/1?increment=1
```

#### 响应参数

```json
{
  "code": 200,
  "message": "success",
  "data": 11
}
```

### 12. 增加回复数

#### 接口信息
- **请求方式**: POST
- **接口路径**: `/api/v1/comment/reply/{commentId}`
- **接口描述**: 增加评论回复数

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| commentId | Long | 是 | - | 评论ID（路径参数） |
| increment | Integer | 否 | 1 | 增加数量 |

#### 请求示例
```http
POST /api/v1/comment/reply/1?increment=1
```

#### 响应参数

```json
{
  "code": 200,
  "message": "success",
  "data": 6
}
```

### 13. 统计目标对象评论数

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/comment/count/target/{targetId}`
- **接口描述**: 统计目标对象的评论数量

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| targetId | Long | 是 | 目标对象ID（路径参数） |
| commentType | String | 是 | 评论类型：CONTENT、DYNAMIC |

#### 请求示例
```http
GET /api/v1/comment/count/target/123?commentType=CONTENT
```

#### 响应参数

```json
{
  "code": 200,
  "message": "success",
  "data": 50
}
```

### 14. 统计用户评论数

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/comment/count/user/{userId}`
- **接口描述**: 统计用户的评论数量

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID（路径参数） |
| commentType | String | 否 | 评论类型（可选） |

#### 请求示例
```http
GET /api/v1/comment/count/user/456?commentType=CONTENT
```

#### 响应参数

```json
{
  "code": 200,
  "message": "success",
  "data": 25
}
```

### 15. 搜索评论

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/comment/search`
- **接口描述**: 根据评论内容搜索

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| keyword | String | 是 | - | 搜索关键词 |
| commentType | String | 否 | - | 评论类型（可选） |
| targetId | Long | 否 | - | 目标对象ID（可选） |
| currentPage | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 页面大小 |

#### 请求示例
```http
GET /api/v1/comment/search?keyword=评论&commentType=CONTENT&currentPage=1&pageSize=10
```

### 16. 获取热门评论

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/comment/popular/{targetId}`
- **接口描述**: 根据点赞数排序获取热门评论

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| targetId | Long | 是 | - | 目标对象ID（路径参数） |
| commentType | String | 是 | - | 评论类型：CONTENT、DYNAMIC |
| timeRange | Integer | 否 | 7 | 时间范围（天） |
| currentPage | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 页面大小 |

#### 请求示例
```http
GET /api/v1/comment/popular/123?commentType=CONTENT&timeRange=7&currentPage=1&pageSize=10
```

### 17. 获取最新评论

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/comment/latest`
- **接口描述**: 获取最新评论列表

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| targetId | Long | 否 | - | 目标对象ID（可选） |
| commentType | String | 否 | - | 评论类型（可选） |
| currentPage | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 页面大小 |

#### 请求示例
```http
GET /api/v1/comment/latest?commentType=CONTENT&currentPage=1&pageSize=10
```

### 18. 根据点赞数范围查询评论

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/comment/like-range`
- **接口描述**: 根据点赞数范围查询评论

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| minLikeCount | Integer | 是 | - | 最小点赞数 |
| maxLikeCount | Integer | 是 | - | 最大点赞数 |
| commentType | String | 否 | - | 评论类型（可选） |
| targetId | Long | 否 | - | 目标对象ID（可选） |
| currentPage | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 页面大小 |

#### 请求示例
```http
GET /api/v1/comment/like-range?minLikeCount=10&maxLikeCount=100&commentType=CONTENT&currentPage=1&pageSize=10
```

### 19. 根据时间范围查询评论

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/comment/time-range`
- **接口描述**: 根据时间范围查询评论

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| startTime | String | 是 | - | 开始时间（ISO 8601格式） |
| endTime | String | 是 | - | 结束时间（ISO 8601格式） |
| commentType | String | 否 | - | 评论类型（可选） |
| targetId | Long | 否 | - | 目标对象ID（可选） |
| currentPage | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 页面大小 |

#### 请求示例
```http
GET /api/v1/comment/time-range?startTime=2024-01-01T00:00:00&endTime=2024-12-31T23:59:59&commentType=CONTENT&currentPage=1&pageSize=10
```

### 20. 获取评论统计信息

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/comment/statistics`
- **接口描述**: 获取评论统计信息

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| targetId | Long | 否 | 目标对象ID（可选） |
| commentType | String | 否 | 评论类型（可选） |
| userId | Long | 否 | 用户ID（可选） |
| startTime | String | 否 | 开始时间（可选） |
| endTime | String | 否 | 结束时间（可选） |

#### 请求示例
```http
GET /api/v1/comment/statistics?targetId=123&commentType=CONTENT&startTime=2024-01-01T00:00:00&endTime=2024-12-31T23:59:59
```

#### 响应参数

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "totalComments": 100,
    "totalLikes": 500,
    "totalReplies": 200,
    "avgLikeCount": 5.0,
    "avgReplyCount": 2.0,
    "popularComments": 10,
    "activeUsers": 50
  }
}
```

## 数据库表结构

### t_comment 评论表

| 字段名 | 类型 | 描述 |
|--------|------|------|
| id | bigint | 评论ID（主键） |
| comment_type | varchar(20) | 评论类型：CONTENT、DYNAMIC |
| target_id | bigint | 目标对象ID |
| parent_comment_id | bigint | 父评论ID，0表示根评论 |
| content | text | 评论内容 |
| user_id | bigint | 评论用户ID |
| user_nickname | varchar(100) | 用户昵称（冗余） |
| user_avatar | varchar(500) | 用户头像（冗余） |
| reply_to_user_id | bigint | 回复目标用户ID |
| reply_to_user_nickname | varchar(100) | 回复目标用户昵称（冗余） |
| reply_to_user_avatar | varchar(500) | 回复目标用户头像（冗余） |
| status | varchar(20) | 状态：NORMAL、HIDDEN、DELETED |
| like_count | int | 点赞数（冗余统计） |
| reply_count | int | 回复数（冗余统计） |
| create_time | timestamp | 创建时间 |
| update_time | timestamp | 更新时间 |

## 字段说明

### 评论类型 (commentType)
- `CONTENT`: 内容评论
- `DYNAMIC`: 动态评论

### 评论状态 (status)
- `NORMAL`: 正常
- `HIDDEN`: 隐藏
- `DELETED`: 已删除

### 更新类型 (updateType)
- `CONTENT`: 更新内容
- `STATUS`: 更新状态
- `STATS`: 更新统计

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
5. 评论支持多级嵌套，最大深度建议不超过3层
6. 用户信息采用冗余存储，避免频繁连表查询
7. 点赞数和回复数采用冗余统计，提高查询性能
8. 评论删除采用逻辑删除，保留数据完整性

## 业务逻辑说明

### 评论流程
1. **创建评论**: 用户发布评论，支持根评论和回复评论
2. **内容审核**: 评论内容需要进行敏感词过滤
3. **通知机制**: 被回复用户会收到通知
4. **统计更新**: 自动更新相关统计数据

### 评论状态流转
- `NORMAL` → `HIDDEN`: 管理员隐藏评论
- `NORMAL` → `DELETED`: 用户或管理员删除评论
- `HIDDEN` → `NORMAL`: 管理员恢复评论
- `DELETED` → `NORMAL`: 管理员恢复评论

### 权限控制
1. **创建权限**: 所有注册用户都可以创建评论
2. **修改权限**: 只有评论作者可以修改自己的评论
3. **删除权限**: 评论作者和管理员可以删除评论
4. **管理权限**: 只有管理员可以隐藏/恢复评论

### 性能优化
1. **冗余字段**: 用户信息冗余存储，避免连表查询
2. **统计缓存**: 点赞数和回复数冗余统计
3. **分页查询**: 大量数据使用分页查询
4. **索引优化**: 关键字段建立索引

## 开发建议

1. **内容过滤**: 实现敏感词过滤机制
2. **通知系统**: 集成消息通知功能
3. **缓存策略**: 热门评论使用缓存
4. **监控告警**: 设置评论异常监控

## 实现状态说明

### 已实现的Controller接口
- ✅ `/api/v1/comments/list` - 评论列表查询（已完整实现）

### 待实现的Controller接口
以下接口目前只存在于CommentFacadeService中，需要在Controller层实现：
- ❌ 创建评论
- ❌ 更新评论  
- ❌ 删除评论
- ❌ 获取评论详情
- ❌ 获取目标对象评论列表
- ❌ 获取评论回复列表
- ❌ 获取评论树
- ❌ 获取用户评论列表
- ❌ 获取用户收到的回复
- ❌ 增加评论点赞数
- ❌ 增加回复数
- ❌ 统计目标对象评论数
- ❌ 统计用户评论数
- ❌ 搜索评论
- ❌ 获取热门评论
- ❌ 获取最新评论
- ❌ 根据点赞数范围查询评论
- ❌ 根据时间范围查询评论
- ❌ 获取评论统计信息

### 建议
1. 优先实现基础的CRUD操作接口
2. 逐步添加高级查询和统计功能
3. 确保接口路径与现有规范保持一致
4. 添加适当的权限控制和参数验证
