# 点赞管理API接口文档

## 基本信息
- **控制器名称**: LikeController
- **基础路径**: `/api/v1/like`
- **描述**: 点赞相关的API接口，支持点赞查询、状态管理等功能

## 接口列表

### 1. 点赞列表查询

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/like/list`
- **接口描述**: 支持按用户、目标、类型等条件查询点赞列表

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| userId | Long | 否 | - | 用户ID |
| targetId | Long | 否 | - | 目标ID |
| likeType | String | 否 | - | 点赞类型 |
| targetType | String | 否 | - | 目标类型 |
| status | String | 否 | - | 点赞状态 |
| orderBy | String | 否 | createTime | 排序字段 |
| orderDirection | String | 否 | DESC | 排序方向 |
| currentPage | Integer | 否 | 1 | 当前页码 |
| pageSize | Integer | 否 | 20 | 页面大小 |

#### 请求示例
```http
GET /api/v1/like/list?userId=1&likeType=LIKE&targetType=CONTENT&currentPage=1&pageSize=10
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
        "userId": 1,
        "userNickname": "用户昵称",
        "userAvatar": "https://example.com/avatar.jpg",
        "targetId": 123,
        "targetType": "CONTENT",
        "targetTitle": "目标标题",
        "likeType": "LIKE",
        "status": "ACTIVE",
        "createTime": "2024-01-01T00:00:00",
        "updateTime": "2024-01-01T00:00:00"
      }
    ],
    "total": 80,
    "currentPage": 1,
    "pageSize": 10,
    "totalPages": 8
  }
}
```

#### 响应字段说明

| 字段名 | 类型 | 描述 |
|--------|------|------|
| code | Integer | 响应状态码 |
| message | String | 响应消息 |
| data.records | Array | 点赞记录列表 |
| data.total | Integer | 总记录数 |
| data.currentPage | Integer | 当前页码 |
| data.pageSize | Integer | 页面大小 |
| data.totalPages | Integer | 总页数 |

## 点赞记录字段说明

| 字段名 | 类型 | 描述 |
|--------|------|------|
| id | Long | 点赞ID |
| userId | Long | 用户ID |
| userNickname | String | 用户昵称 |
| userAvatar | String | 用户头像 |
| targetId | Long | 目标对象ID |
| targetType | String | 目标对象类型 |
| targetTitle | String | 目标对象标题 |
| likeType | String | 点赞类型 |
| status | String | 点赞状态 |
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
5. 点赞类型包括：LIKE（点赞）、DISLIKE（点踩）、LOVE（爱心）
6. 目标类型包括：CONTENT（内容）、COMMENT（评论）、DYNAMIC（动态）
7. 点赞状态包括：ACTIVE（激活）、INACTIVE（非激活）、DELETED（已删除）
8. 排序字段支持：createTime（创建时间）、updateTime（更新时间）
9. 通过LikeFacadeService可以调用更多点赞相关功能
