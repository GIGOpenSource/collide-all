# 消息会话管理API接口文档

## 基本信息
- **控制器名称**: MessageSessionController
- **基础路径**: `/api/v1/message-sessions`
- **描述**: 消息会话管理相关接口

## 接口列表

### 1. 创建会话

#### 接口信息
- **请求方式**: POST
- **接口路径**: `/api/v1/message-sessions`
- **接口描述**: 创建新的消息会话

#### 请求参数

```json
{
  "userId1": 123,
  "userId2": 456,
  "sessionType": "PRIVATE",
  "title": "会话标题",
  "description": "会话描述"
}
```

#### 响应参数

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "sessionId": "SESS_20240101001",
    "userId1": 123,
    "userId2": 456,
    "sessionType": "PRIVATE",
    "title": "会话标题",
    "description": "会话描述",
    "lastMessage": "最后一条消息",
    "lastMessageTime": "2024-01-01T00:00:00",
    "unreadCount": 0,
    "status": "ACTIVE",
    "createTime": "2024-01-01T00:00:00",
    "updateTime": "2024-01-01T00:00:00"
  }
}
```

### 2. 获取会话列表

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/message-sessions`
- **接口描述**: 获取消息会话列表

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| userId | Long | 是 | - | 用户ID |
| sessionType | String | 否 | - | 会话类型 |
| status | String | 否 | - | 会话状态 |
| currentPage | Integer | 否 | 1 | 当前页码 |
| pageSize | Integer | 否 | 20 | 页面大小 |

#### 请求示例
```http
GET /api/v1/message-sessions?userId=123&currentPage=1&pageSize=10
```

### 3. 更新会话

#### 接口信息
- **请求方式**: PUT
- **接口路径**: `/api/v1/message-sessions/{sessionId}`
- **接口描述**: 更新会话信息

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| sessionId | String | 是 | 会话ID（路径参数） |

#### 请求体

```json
{
  "title": "新会话标题",
  "description": "新会话描述",
  "status": "ACTIVE"
}
```

### 4. 归档会话

#### 接口信息
- **请求方式**: PUT
- **接口路径**: `/api/v1/message-sessions/{sessionId}/archive`
- **接口描述**: 归档指定会话

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| sessionId | String | 是 | 会话ID（路径参数） |

#### 请求示例
```http
PUT /api/v1/message-sessions/SESS_20240101001/archive
```

### 5. 分页查询会话

#### 接口信息
- **请求方式**: POST
- **接口路径**: `/api/v1/message-sessions/query`
- **接口描述**: 分页查询会话记录

#### 请求参数

```json
{
  "userId": 123,
  "sessionType": "PRIVATE",
  "status": "ACTIVE",
  "keyword": "搜索关键词",
  "currentPage": 1,
  "pageSize": 20,
  "orderBy": "lastMessageTime",
  "orderDirection": "DESC"
}
```

### 6. 用户会话列表

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/message-sessions/user/{userId}`
- **接口描述**: 获取指定用户的会话列表

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| userId | Long | 是 | - | 用户ID（路径参数） |
| sessionType | String | 否 | - | 会话类型 |
| currentPage | Integer | 否 | 1 | 当前页码 |
| pageSize | Integer | 否 | 20 | 页面大小 |

#### 请求示例
```http
GET /api/v1/message-sessions/user/123?currentPage=1&pageSize=10
```

### 7. 用户活跃会话

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/message-sessions/user/{userId}/active`
- **接口描述**: 获取用户的活跃会话

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| userId | Long | 是 | - | 用户ID（路径参数） |
| limit | Integer | 否 | 10 | 限制数量 |

#### 请求示例
```http
GET /api/v1/message-sessions/user/123/active?limit=20
```

### 8. 用户未读会话

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/message-sessions/user/{userId}/unread`
- **接口描述**: 获取用户的未读会话

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| userId | Long | 是 | - | 用户ID（路径参数） |
| currentPage | Integer | 否 | 1 | 当前页码 |
| pageSize | Integer | 否 | 20 | 页面大小 |

#### 请求示例
```http
GET /api/v1/message-sessions/user/123/unread?currentPage=1&pageSize=10
```

### 9. 用户未读数量

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/message-sessions/user/{userId}/unread/count`
- **接口描述**: 获取用户的未读会话数量

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID（路径参数） |

#### 请求示例
```http
GET /api/v1/message-sessions/user/123/unread/count
```

### 10. 用户会话数量

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/message-sessions/user/{userId}/count`
- **接口描述**: 获取用户的会话数量

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID（路径参数） |
| sessionType | String | 否 | 会话类型 |

#### 请求示例
```http
GET /api/v1/message-sessions/user/123/count?sessionType=PRIVATE
```

### 11. 用户总未读数

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/message-sessions/user/{userId}/unread/total`
- **接口描述**: 获取用户的总未读消息数

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID（路径参数） |

#### 请求示例
```http
GET /api/v1/message-sessions/user/123/unread/total
```

### 12. 更新最后消息

#### 接口信息
- **请求方式**: PUT
- **接口路径**: `/api/v1/message-sessions/last-message`
- **接口描述**: 更新会话的最后消息

#### 请求参数

```json
{
  "sessionId": "SESS_20240101001",
  "lastMessage": "新的最后消息",
  "lastMessageTime": "2024-01-01T00:00:00",
  "senderId": 123
}
```

### 13. 增加未读数

#### 接口信息
- **请求方式**: PUT
- **接口路径**: `/api/v1/message-sessions/unread/increment`
- **接口描述**: 增加会话的未读消息数

#### 请求参数

```json
{
  "sessionId": "SESS_20240101001",
  "userId": 123,
  "increment": 1
}
```

### 14. 清除未读

#### 接口信息
- **请求方式**: PUT
- **接口路径**: `/api/v1/message-sessions/unread/clear`
- **接口描述**: 清除会话的未读消息

#### 请求参数

```json
{
  "sessionId": "SESS_20240101001",
  "userId": 123
}
```

### 15. 新消息通知

#### 接口信息
- **请求方式**: POST
- **接口路径**: `/api/v1/message-sessions/new-message`
- **接口描述**: 处理新消息通知

#### 请求参数

```json
{
  "sessionId": "SESS_20240101001",
  "senderId": 123,
  "receiverId": 456,
  "message": "新消息内容",
  "messageTime": "2024-01-01T00:00:00"
}
```

### 16. 清空会话

#### 接口信息
- **请求方式**: DELETE
- **接口路径**: `/api/v1/message-sessions/empty`
- **接口描述**: 清空指定会话的消息

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| sessionId | String | 是 | 会话ID |
| userId | Long | 是 | 用户ID |

#### 请求示例
```http
DELETE /api/v1/message-sessions/empty?sessionId=SESS_20240101001&userId=123
```

### 17. 删除已归档会话

#### 接口信息
- **请求方式**: DELETE
- **接口路径**: `/api/v1/message-sessions/archived`
- **接口描述**: 删除已归档的会话

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID |
| beforeTime | String | 否 | 截止时间 |

#### 请求示例
```http
DELETE /api/v1/message-sessions/archived?userId=123&beforeTime=2024-01-01T00:00:00
```

### 18. 批量归档

#### 接口信息
- **请求方式**: PUT
- **接口路径**: `/api/v1/message-sessions/batch/archive`
- **接口描述**: 批量归档会话

#### 请求参数

```json
{
  "sessionIds": ["SESS_20240101001", "SESS_20240101002"],
  "userId": 123
}
```

### 19. 批量取消归档

#### 接口信息
- **请求方式**: PUT
- **接口路径**: `/api/v1/message-sessions/batch/unarchive`
- **接口描述**: 批量取消归档会话

#### 请求参数

```json
{
  "sessionIds": ["SESS_20240101001", "SESS_20240101002"],
  "userId": 123
}
```

### 20. 重建索引

#### 接口信息
- **请求方式**: POST
- **接口路径**: `/api/v1/message-sessions/rebuild-index`
- **接口描述**: 重建会话索引

#### 请求参数

```json
{
  "userId": 123,
  "force": false
}
```

### 21. 系统健康检查

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/message-sessions/health`
- **接口描述**: 检查消息会话系统运行状态

#### 请求参数
无

#### 响应参数

```json
{
  "code": 200,
  "message": "success",
  "data": "系统运行正常"
}
```

## 会话记录字段说明

| 字段名 | 类型 | 描述 |
|--------|------|------|
| id | Long | 会话记录ID |
| sessionId | String | 会话ID |
| userId1 | Long | 用户1 ID |
| userId2 | Long | 用户2 ID |
| sessionType | String | 会话类型 |
| title | String | 会话标题 |
| description | String | 会话描述 |
| lastMessage | String | 最后一条消息 |
| lastMessageTime | String | 最后消息时间 |
| unreadCount | Integer | 未读消息数 |
| status | String | 会话状态 |
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
4. 会话状态包括：ACTIVE（活跃）、ARCHIVED（已归档）、DELETED（已删除）
5. 会话类型包括：PRIVATE（私聊）、GROUP（群聊）、SYSTEM（系统消息）
6. 会话ID格式：SESS_YYYYMMDDHHMMSS
7. 未读消息数会自动累加，需要手动清除
