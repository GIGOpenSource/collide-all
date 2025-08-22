# Profile客态模块接口文档

## 文档信息
- **文档名称**: Profile客态模块接口文档
- **版本**: 2.0.0
- **更新时间**: 2024-01-31
- **描述**: Profile客态模块相关的API接口文档，基于实际controller实现

---

## 目录
1. [用户基本信息查看](#1-用户基本信息查看)
2. [内容搜索功能](#2-内容搜索功能)
3. [视频搜索功能](#3-视频搜索功能)
4. [关注关系查询](#4-关注关系查询)
5. [统计信息查询](#5-统计信息查询)
6. [通用响应格式](#6-通用响应格式)
7. [错误码说明](#7-错误码说明)
8. [注意事项](#8-注意事项)
9. [接口调用示例](#9-接口调用示例)
10. [功能完整性说明](#10-功能完整性说明)

---

## 1. 用户基本信息查看

### 1.1 获取被查看者基本信息
- **接口路径**: `GET /api/v1/users/{id}`
- **请求方式**: GET
- **功能描述**: 获取被查看者的基本信息，包括VIP状态
- **Controller**: UserController.getUserById()

#### 请求参数
| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| id | Long | 是 | 用户ID（路径参数） |

#### 请求示例
```http
GET /api/v1/users/123
```

#### 响应示例
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 123,
    "username": "zhangsan",
    "nickname": "张三",
    "avatar": "https://example.com/avatar.jpg",
    "email": "zhangsan@example.com",
    "phone": "13800138000",
    "roles": ["user"],
    "status": "active",
    "bio": "个人简介",
    "birthday": "1990-01-01",
    "gender": "male",
    "location": "北京",
    "followerCount": 300,
    "followingCount": 150,
    "contentCount": 25,
    "likeCount": 1000,
    "isVip": "Y",
    "vipExpireTime": "2024-12-31T23:59:59",
    "lastLoginTime": "2024-01-30T10:00:00",
    "loginCount": 150,
    "createTime": "2024-01-01T10:00:00",
    "updateTime": "2024-01-30T10:00:00"
  }
}
```

#### 使用说明
- 此接口无需登录认证，可公开访问
- 返回用户的所有公开信息，包括统计数据
- 如果用户不存在，返回错误信息

---

## 2. 内容搜索功能

### 2.1 根据被查看者ID搜索内容
- **接口路径**: `GET /api/v1/content/core/list`
- **请求方式**: GET
- **功能描述**: 获取被查看者发布的所有内容
- **Controller**: ContentController.listContents()

#### 请求参数
| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| authorId | Long | 是 | - | 被查看者ID |
| contentType | String | 否 | - | 内容类型：VIDEO、NOVEL、COMIC、ARTICLE、AUDIO |
| status | String | 否 | PUBLISHED | 内容状态 |
| orderBy | String | 否 | createTime | 排序字段：createTime、viewCount、likeCount、favoriteCount、shareCount、commentCount、score |
| orderDirection | String | 否 | DESC | 排序方向：ASC、DESC |
| currentPage | Integer | 否 | 1 | 当前页码 |
| pageSize | Integer | 否 | 20 | 页面大小 |

#### 请求示例
```http
GET /api/v1/content/core/list?authorId=123&currentPage=1&pageSize=20
```

#### 响应示例
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "title": "用户发布的内容",
        "description": "内容描述",
        "contentType": "VIDEO",
        "authorId": 123,
        "authorNickname": "张三",
        "authorAvatar": "https://example.com/avatar.jpg",
        "categoryId": 1,
        "categoryName": "娱乐",
        "tags": "娱乐,搞笑",
        "viewCount": 1000,
        "likeCount": 50,
        "commentCount": 20,
        "favoriteCount": 30,
        "shareCount": 10,
        "score": 4.5,
        "status": "PUBLISHED",
        "reviewStatus": "APPROVED",
        "createTime": "2024-01-30T10:30:00",
        "updateTime": "2024-01-30T10:30:00"
      }
    ],
    "total": 25,
    "currentPage": 1,
    "pageSize": 20,
    "totalPages": 2
  }
}
```

#### 使用说明
- authorId参数为必填，用于筛选特定作者的内容
- 支持按内容类型、状态等条件进一步筛选
- 支持多种排序方式，默认按创建时间倒序
- 分页参数有默认值，可根据需要调整

---

## 3. 视频搜索功能

### 3.1 根据被查看者ID搜索视频内容
- **接口路径**: `GET /api/v1/content/core/list`
- **请求方式**: GET
- **功能描述**: 获取被查看者发布的视频内容
- **Controller**: ContentController.listContents()

#### 请求参数
| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| authorId | Long | 是 | - | 被查看者ID |
| contentType | String | 否 | VIDEO | 内容类型（固定为VIDEO） |
| status | String | 否 | PUBLISHED | 内容状态 |
| orderBy | String | 否 | createTime | 排序字段 |
| orderDirection | String | 否 | DESC | 排序方向 |
| currentPage | Integer | 否 | 1 | 当前页码 |
| pageSize | Integer | 否 | 20 | 页面大小 |

#### 请求示例
```http
GET /api/v1/content/core/list?authorId=123&contentType=VIDEO&currentPage=1&pageSize=20
```

#### 响应示例
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "title": "精彩视频内容",
        "description": "视频描述",
        "contentType": "VIDEO",
        "authorId": 123,
        "authorNickname": "张三",
        "authorAvatar": "https://example.com/avatar.jpg",
        "categoryId": 1,
        "categoryName": "娱乐",
        "tags": "娱乐,搞笑",
        "viewCount": 2000,
        "likeCount": 100,
        "commentCount": 30,
        "favoriteCount": 50,
        "shareCount": 20,
        "score": 4.8,
        "status": "PUBLISHED",
        "reviewStatus": "APPROVED",
        "createTime": "2024-01-30T10:30:00",
        "updateTime": "2024-01-30T10:30:00"
      }
    ],
    "total": 15,
    "currentPage": 1,
    "pageSize": 20,
    "totalPages": 1
  }
}
```

#### 使用说明
- 通过设置contentType=VIDEO来筛选视频内容
- 可以结合其他参数进行更精确的筛选
- 视频内容通常包含更多的互动数据

---

## 4. 关注关系查询

### 4.1 查询关注关系
- **接口路径**: `GET /api/v1/follow/check/{followerId}/{followeeId}`
- **请求方式**: GET
- **功能描述**: 查询关注关系
- **Controller**: FollowController.checkFollowStatus()

#### 请求参数
| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| followerId | Long | 是 | 关注者ID（路径参数） |
| followeeId | Long | 是 | 被关注者ID（路径参数） |

#### 请求示例
```http
GET /api/v1/follow/check/456/123
```

#### 响应示例
```json
{
  "code": 200,
  "message": "success",
  "data": true
}
```

#### 使用说明
- 返回true表示已关注，false表示未关注
- 用于检查当前用户是否关注了被查看者
- 支持客态查看，无需登录

### 4.2 获取关注者列表
- **接口路径**: `GET /api/v1/follow/list`
- **请求方式**: GET
- **功能描述**: 查询关注被查看者的用户列表
- **Controller**: FollowController.listFollows()

#### 请求参数
| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| followedId | Long | 是 | - | 被关注者ID |
| status | String | 否 | active | 关注状态 |
| currentPage | Integer | 否 | 1 | 当前页码 |
| pageSize | Integer | 否 | 20 | 页面大小 |

#### 请求示例
```http
GET /api/v1/follow/list?followedId=123&currentPage=1&pageSize=20
```

#### 响应示例
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "followerId": 456,
        "followerNickname": "李四",
        "followerAvatar": "https://example.com/avatar2.jpg",
        "followeeId": 123,
        "followeeNickname": "张三",
        "followeeAvatar": "https://example.com/avatar.jpg",
        "status": "active",
        "createTime": "2024-01-15T10:00:00",
        "updateTime": "2024-01-15T10:00:00"
      }
    ],
    "total": 300,
    "currentPage": 1,
    "pageSize": 20,
    "totalPages": 15
  }
}
```

#### 使用说明
- followedId参数用于指定要查看粉丝列表的用户
- 返回该用户的所有粉丝信息
- 支持分页查询，避免数据量过大

---

## 5. 统计信息查询

### 5.1 获取关注数量
- **接口路径**: `GET /api/v1/follow/count/following/{userId}`
- **请求方式**: GET
- **功能描述**: 获取被查看者关注的数量
- **Controller**: FollowController.getFollowingCount()

#### 请求参数
| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID（路径参数） |

#### 请求示例
```http
GET /api/v1/follow/count/following/123
```

#### 响应示例
```json
{
  "code": 200,
  "message": "success",
  "data": 150
}
```

#### 使用说明
- 返回用户关注的其他用户数量
- 用于展示用户的关注统计
- 数据实时更新

### 5.2 获取粉丝数量
- **接口路径**: `GET /api/v1/follow/count/followers/{userId}`
- **请求方式**: GET
- **功能描述**: 获取被查看者的粉丝数量
- **Controller**: FollowController.getFollowersCount()

#### 请求参数
| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID（路径参数） |

#### 请求示例
```http
GET /api/v1/follow/count/followers/123
```

#### 响应示例
```json
{
  "code": 200,
  "message": "success",
  "data": 300
}
```

#### 使用说明
- 返回关注该用户的用户数量
- 用于展示用户的粉丝统计
- 数据实时更新

### 5.3 获取点赞数量
- **接口路径**: `GET /api/v1/like/received/count/{userId}`
- **请求方式**: GET
- **功能描述**: 获取被查看者获得点赞的数量统计
- **Controller**: LikeController.getUserReceivedLikeCount()

#### 请求参数
| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| userId | Long | 是 | - | 用户ID（路径参数） |
| likeType | String | 否 | - | 点赞类型（可选）：CONTENT、COMMENT、DYNAMIC |

#### 请求示例
```http
GET /api/v1/like/received/count/123?likeType=CONTENT
```

#### 响应示例
```json
{
  "code": 200,
  "message": "success",
  "data": 1250
}
```

#### 使用说明
- 统计用户作为作者的作品被点赞的总数量
- likeType参数可选，不传则统计所有类型的点赞
- 支持按内容类型分别统计

### 5.4 获取VIP状态
- **接口路径**: `GET /api/v1/users/{userId}/vip-status`
- **请求方式**: GET
- **功能描述**: 获取被查看者的VIP状态信息
- **Controller**: UserController.getUserVipStatus()

#### 请求参数
| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID（路径参数） |

#### 请求示例
```http
GET /api/v1/users/123/vip-status
```

#### 响应示例
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "userId": 123,
    "isVip": "Y",
    "vipExpireTime": "2024-12-31T23:59:59",
    "daysRemaining": 45,
    "isExpired": false
  }
}
```

#### 使用说明
- 返回用户的VIP状态详细信息
- 包含VIP过期时间和剩余天数
- 用于展示用户的VIP权益状态

---

## 6. 通用响应格式

### 成功响应
```json
{
  "code": 200,
  "message": "success",
  "data": {
    // 具体数据
  }
}
```

### 分页响应
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      // 数据列表
    ],
    "total": 100,
    "currentPage": 1,
    "pageSize": 20,
    "totalPages": 5
  }
}
```

### 错误响应
```json
{
  "code": 400,
  "message": "错误信息",
  "data": null
}
```

---

## 7. 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未授权 |
| 403 | 禁止访问 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

---

## 8. 注意事项

1. **认证要求**: Profile客态查看功能无需用户登录，可公开访问
2. **分页参数**: 默认页码为1，页面大小为20，最大页面大小为100
3. **时间格式**: 所有时间字段使用ISO 8601格式：`YYYY-MM-DDTHH:mm:ss`
4. **内容类型**: 支持的内容类型包括：VIDEO（视频）、NOVEL（小说）、COMIC（漫画）、ARTICLE（文章）、AUDIO（音频）
5. **权限控制**: 只能查看公开的用户信息和内容，私密内容无法访问
6. **数据安全**: 客态查看无法修改他人数据，确保数据安全性
7. **性能优化**: 所有查询都支持分页，避免大量数据一次性返回
8. **缓存策略**: 统计数据支持缓存，提升查询性能

---

## 9. 接口调用示例

### 完整的Profile客态页面加载流程

1. **获取用户基本信息**
```http
GET /api/v1/users/123
```

2. **获取统计信息**
```http
GET /api/v1/follow/count/following/123
GET /api/v1/follow/count/followers/123
GET /api/v1/like/received/count/123?likeType=CONTENT
GET /api/v1/users/123/vip-status
```

3. **获取内容列表**
```http
GET /api/v1/content/core/list?authorId=123&currentPage=1&pageSize=20
```

4. **获取视频内容**
```http
GET /api/v1/content/core/list?authorId=123&contentType=VIDEO&currentPage=1&pageSize=20
```

5. **检查关注关系**
```http
GET /api/v1/follow/check/456/123
```

6. **获取关注者列表**
```http
GET /api/v1/follow/list?followedId=123&currentPage=1&pageSize=20
```

---

## 10. 功能完整性说明

### ✅ **完全实现的功能**
- **用户基本信息查看**: 通过UserController.getUserById()实现，包括VIP状态查询
- **内容搜索功能**: 通过ContentController.listContents()实现，支持按作者ID筛选
- **视频搜索功能**: 通过ContentController.listContents()实现，支持按内容类型和作者ID筛选
- **关注关系查询**: 通过FollowController.checkFollowStatus()实现，支持关注状态检查
- **关注者列表查询**: 通过FollowController.listFollows()实现，支持分页查询
- **统计信息查询**: 通过FollowController和LikeController实现，包括关注数、粉丝数、点赞数统计
- **VIP状态查询**: 通过UserController.getUserVipStatus()实现，支持VIP状态和权益查询

### 🔗 **API路径对应关系**
- **用户信息**: `GET /api/v1/users/*` - UserController
- **内容管理**: `GET /api/v1/content/core/*` - ContentController
- **关注管理**: `GET /api/v1/follow/*` - FollowController
- **点赞统计**: `GET /api/v1/like/received/*` - LikeController

### 📊 **功能实现率: 100%**
所有Profile客态模块描述的功能都能通过现有API完全实现。

### 💡 **技术实现特点**
1. **公开访问**: 客态查看功能无需登录，支持公开访问
2. **数据安全**: 只能查看公开数据，私密内容无法访问
3. **性能优化**: 支持分页查询，避免大量数据一次性返回
4. **统计聚合**: 支持多种统计数据的快速查询
5. **权限控制**: 客态查看无法修改他人数据，确保系统安全性
6. **缓存支持**: 统计数据支持缓存，提升查询性能
7. **索引优化**: 所有查询都基于优化的数据库索引设计

### 🔍 **实际Controller方法对应**
| 功能 | 接口路径 | Controller方法 | 实现状态 |
|------|----------|----------------|----------|
| 用户基本信息 | `GET /api/v1/users/{id}` | UserController.getUserById() | ✅ 已实现 |
| VIP状态查询 | `GET /api/v1/users/{userId}/vip-status` | UserController.getUserVipStatus() | ✅ 已实现 |
| 内容列表查询 | `GET /api/v1/content/core/list` | ContentController.listContents() | ✅ 已实现 |
| 关注状态检查 | `GET /api/v1/follow/check/{followerId}/{followeeId}` | FollowController.checkFollowStatus() | ✅ 已实现 |
| 关注者列表 | `GET /api/v1/follow/list` | FollowController.listFollows() | ✅ 已实现 |
| 关注数量统计 | `GET /api/v1/follow/count/following/{userId}` | FollowController.getFollowingCount() | ✅ 已实现 |
| 粉丝数量统计 | `GET /api/v1/follow/count/followers/{userId}` | FollowController.getFollowersCount() | ✅ 已实现 |
| 点赞数量统计 | `GET /api/v1/like/received/count/{userId}` | LikeController.getUserReceivedLikeCount() | ✅ 已实现 |

### 📝 **使用建议**
1. **前端集成**: 建议按顺序调用接口，先获取基本信息，再获取统计数据
2. **缓存策略**: 统计数据可以适当缓存，减少重复请求
3. **错误处理**: 所有接口都返回统一的错误格式，便于前端处理
4. **分页优化**: 内容列表建议使用分页，避免一次性加载过多数据
5. **权限控制**: 客态查看无需登录，但建议在需要时检查用户权限
