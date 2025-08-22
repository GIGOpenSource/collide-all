# 关注管理API接口文档

## 基本信息
- **控制器名称**: FollowController
- **基础路径**: `/api/v1/follow`
- **描述**: 关注相关的API接口，支持关注/取消关注、关注关系查询、粉丝管理等功能

## 接口列表

### 1. 关注关系列表查询

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/follow/list`
- **接口描述**: 支持按关注者、被关注者、状态等条件查询关注关系列表

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| followerId | Long | 否 | - | 关注者ID |
| followedId | Long | 否 | - | 被关注者ID |
| status | String | 否 | - | 关注状态 |
| followType | String | 否 | - | 关注类型 |
| isMutual | Boolean | 否 | - | 是否互关 |
| orderBy | String | 否 | createTime | 排序字段 |
| orderDirection | String | 否 | DESC | 排序方向 |
| currentPage | Integer | 否 | 1 | 当前页码 |
| pageSize | Integer | 否 | 20 | 页面大小 |

#### 请求示例
```http
GET /api/v1/follow/list?followerId=1&status=ACTIVE&currentPage=1&pageSize=10
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
        "followerId": 1,
        "followedId": 2,
        "followerNickname": "用户A",
        "followerAvatar": "https://example.com/avatar1.jpg",
        "followedNickname": "用户B",
        "followedAvatar": "https://example.com/avatar2.jpg",
        "status": "ACTIVE",
        "followType": "NORMAL",
        "isMutual": false,
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

### 2. 关注用户

#### 接口信息
- **请求方式**: POST
- **接口路径**: `/api/v1/follow/create`
- **接口描述**: 创建关注关系，支持关注者和被关注者信息冗余存储

#### 请求参数

```json
{
  "followerId": 1,
  "followedId": 2,
  "followerNickname": "用户A",
  "followerAvatar": "https://example.com/avatar1.jpg",
  "followedNickname": "用户B",
  "followedAvatar": "https://example.com/avatar2.jpg",
  "followType": "NORMAL"
}
```

#### 响应参数

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "followerId": 1,
    "followedId": 2,
    "followerNickname": "用户A",
    "followerAvatar": "https://example.com/avatar1.jpg",
    "followedNickname": "用户B",
    "followedAvatar": "https://example.com/avatar2.jpg",
    "status": "ACTIVE",
    "followType": "NORMAL",
    "createTime": "2024-01-01T00:00:00"
  }
}
```

### 3. 取消关注

#### 接口信息
- **请求方式**: POST
- **接口路径**: `/api/v1/follow/unfollow`
- **接口描述**: 取消关注关系，将关注状态更新为cancelled

#### 请求参数

```json
{
  "followerId": 1,
  "followedId": 2
}
```

#### 响应参数

```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

### 4. 检查关注状态

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/follow/check`
- **接口描述**: 查询用户是否已关注目标用户

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| followerId | Long | 是 | 关注者ID |
| followedId | Long | 是 | 被关注者ID |

#### 请求示例
```http
GET /api/v1/follow/check?followerId=1&followedId=2
```

#### 响应参数

```json
{
  "code": 200,
  "message": "success",
  "data": true
}
```

### 5. 获取关注关系详情

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/follow/relation`
- **接口描述**: 获取两个用户之间的关注关系详情

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| followerId | Long | 是 | 关注者ID |
| followedId | Long | 是 | 被关注者ID |

#### 响应参数

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "followerId": 1,
    "followedId": 2,
    "followerNickname": "用户A",
    "followerAvatar": "https://example.com/avatar1.jpg",
    "followedNickname": "用户B",
    "followedAvatar": "https://example.com/avatar2.jpg",
    "status": "ACTIVE",
    "followType": "NORMAL",
    "createTime": "2024-01-01T00:00:00"
  }
}
```

### 6. 获取用户的关注列表

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/follow/following`
- **接口描述**: 查询某用户关注的所有人

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| followerId | Long | 是 | - | 关注者ID |
| currentPage | Integer | 否 | 1 | 当前页码 |
| pageSize | Integer | 否 | 20 | 页面大小 |

#### 响应参数

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [...],
    "total": 50,
    "currentPage": 1,
    "pageSize": 20,
    "totalPages": 5
  }
}
```

### 7. 获取用户的粉丝列表

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/follow/followers`
- **接口描述**: 查询关注某用户的所有人

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| followedId | Long | 是 | - | 被关注者ID |
| currentPage | Integer | 否 | 1 | 当前页码 |
| pageSize | Integer | 否 | 20 | 页面大小 |

### 8. 获取用户关注数量

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/follow/following/count`
- **接口描述**: 统计用户关注的人数

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| followerId | Long | 是 | 关注者ID |

#### 响应参数

```json
{
  "code": 200,
  "message": "success",
  "data": 25
}
```

### 9. 获取用户粉丝数量

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/follow/followers/count`
- **接口描述**: 统计关注某用户的人数

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| followedId | Long | 是 | 被关注者ID |

### 10. 获取用户关注统计信息

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/follow/statistics`
- **接口描述**: 包含关注数和粉丝数

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID |

#### 响应参数

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "following_count": 25,
    "followers_count": 30
  }
}
```

### 11. 批量检查关注状态

#### 接口信息
- **请求方式**: POST
- **接口路径**: `/api/v1/follow/batch-check`
- **接口描述**: 检查用户对多个目标用户的关注状态

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| followerId | Long | 是 | 关注者ID |
| followedIds | List<Long> | 是 | 被关注者ID列表 |

#### 请求示例
```http
POST /api/v1/follow/batch-check?followerId=1
Content-Type: application/json

[2, 3, 4, 5]
```

#### 响应参数

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "2": true,
    "3": false,
    "4": true,
    "5": false
  }
}
```

### 12. 获取互相关注的好友

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/follow/mutual`
- **接口描述**: 查询双向关注关系

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| userId | Long | 是 | - | 用户ID |
| currentPage | Integer | 否 | 1 | 当前页码 |
| pageSize | Integer | 否 | 20 | 页面大小 |

### 13. 根据昵称搜索关注关系

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/follow/search`
- **接口描述**: 根据关注者或被关注者昵称进行模糊搜索

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| followerId | Long | 否 | - | 关注者ID（可选） |
| followedId | Long | 否 | - | 被关注者ID（可选） |
| nicknameKeyword | String | 是 | - | 昵称关键词 |
| currentPage | Integer | 否 | 1 | 当前页码 |
| pageSize | Integer | 否 | 20 | 页面大小 |

### 14. 更新用户信息（冗余字段同步）

#### 接口信息
- **请求方式**: PUT
- **接口路径**: `/api/v1/follow/user/info`
- **接口描述**: 当用户信息变更时，同步更新关注表中的冗余信息

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID |
| nickname | String | 是 | 新昵称 |
| avatar | String | 是 | 新头像 |

### 15. 重新激活已取消的关注关系

#### 接口信息
- **请求方式**: POST
- **接口路径**: `/api/v1/follow/reactivate`
- **接口描述**: 将cancelled状态的关注重新设置为active

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| followerId | Long | 是 | 关注者ID |
| followedId | Long | 是 | 被关注者ID |

### 16. 验证关注请求参数

#### 接口信息
- **请求方式**: POST
- **接口路径**: `/api/v1/follow/validate`
- **接口描述**: 校验请求参数的有效性

#### 请求参数

```json
{
  "followerId": 1,
  "followedId": 2,
  "followerNickname": "用户A",
  "followedNickname": "用户B"
}
```

### 17. 检查是否可以关注

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/follow/can-follow`
- **接口描述**: 检查业务规则是否允许关注

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| followerId | Long | 是 | 关注者ID |
| followedId | Long | 是 | 被关注者ID |

### 18. 清理已取消的关注记录

#### 接口信息
- **请求方式**: DELETE
- **接口路径**: `/api/v1/follow/cleanup`
- **接口描述**: 物理删除cancelled状态的记录（可选功能）

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| days | Integer | 否 | 30 | 保留天数 |

## 关注关系字段说明

| 字段名 | 类型 | 描述 |
|--------|------|------|
| id | Long | 关注关系ID |
| followerId | Long | 关注者ID |
| followedId | Long | 被关注者ID |
| followerNickname | String | 关注者昵称 |
| followerAvatar | String | 关注者头像 |
| followedNickname | String | 被关注者昵称 |
| followedAvatar | String | 被关注者头像 |
| status | String | 关注状态 |
| followType | String | 关注类型 |
| isMutual | Boolean | 是否互关 |
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
5. 关注状态包括：ACTIVE（激活）、INACTIVE（非激活）、CANCELLED（已取消）、DELETED（已删除）
6. 关注类型包括：NORMAL（普通关注）、CLOSE（特别关注）、MUTUAL（互相关注）
7. 排序字段支持：createTime（创建时间）、updateTime（更新时间）
8. 所有接口都通过FollowFacadeService提供完整的业务逻辑支持
9. 支持用户信息冗余存储，提高查询性能
10. 提供完整的关注生命周期管理功能
