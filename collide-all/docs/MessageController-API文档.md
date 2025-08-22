# 消息管理API接口文档

## 基本信息
- **控制器名称**: MessageController
- **基础路径**: `/api/v1/messages`
- **描述**: 消息相关的API接口，支持私信、留言板、消息回复等功能

## 接口列表

### 1. 消息列表查询

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/messages/list`
- **接口描述**: 支持按发送者、接收者、类型等条件查询消息列表

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| senderId | Long | 否 | - | 发送者ID |
| receiverId | Long | 否 | - | 接收者ID |
| messageType | String | 否 | - | 消息类型 |
| status | String | 否 | - | 消息状态 |
| isRead | Boolean | 否 | - | 是否已读 |
| keyword | String | 否 | - | 关键词搜索 |
| orderBy | String | 否 | createTime | 排序字段 |
| orderDirection | String | 否 | DESC | 排序方向 |
| currentPage | Integer | 否 | 1 | 当前页码 |
| pageSize | Integer | 否 | 20 | 页面大小 |

#### 请求示例
```http
GET /api/v1/messages/list?senderId=1&receiverId=2&messageType=PRIVATE&currentPage=1&pageSize=10
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
        "senderId": 1,
        "senderNickname": "发送者昵称",
        "senderAvatar": "https://example.com/avatar1.jpg",
        "receiverId": 2,
        "receiverNickname": "接收者昵称",
        "receiverAvatar": "https://example.com/avatar2.jpg",
        "messageType": "PRIVATE",
        "content": "消息内容",
        "status": "ACTIVE",
        "isRead": false,
        "parentMessageId": null,
        "createTime": "2024-01-01T00:00:00",
        "updateTime": "2024-01-01T00:00:00"
      }
    ],
    "total": 120,
    "currentPage": 1,
    "pageSize": 10,
    "totalPages": 12
  }
}
```

#### 响应字段说明

| 字段名 | 类型 | 描述 |
|--------|------|------|
| code | Integer | 响应状态码 |
| message | String | 响应消息 |
| data.records | Array | 消息记录列表 |
| data.total | Integer | 总记录数 |
| data.currentPage | Integer | 当前页码 |
| data.pageSize | Integer | 页面大小 |
| data.totalPages | Integer | 总页数 |

## 消息记录字段说明

| 字段名 | 类型 | 描述 |
|--------|------|------|
| id | Long | 消息ID |
| senderId | Long | 发送者ID |
| senderNickname | String | 发送者昵称 |
| senderAvatar | String | 发送者头像 |
| receiverId | Long | 接收者ID |
| receiverNickname | String | 接收者昵称 |
| receiverAvatar | String | 接收者头像 |
| messageType | String | 消息类型 |
| content | String | 消息内容 |
| status | String | 消息状态 |
| isRead | Boolean | 是否已读 |
| parentMessageId | Long | 父消息ID（回复消息） |
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
5. 消息类型包括：PRIVATE（私信）、PUBLIC（公开消息）、SYSTEM（系统消息）
6. 消息状态包括：ACTIVE（激活）、INACTIVE（非激活）、DELETED（已删除）
7. 排序字段支持：createTime（创建时间）、updateTime（更新时间）
8. 通过MessageFacadeService可以调用更多消息相关功能
