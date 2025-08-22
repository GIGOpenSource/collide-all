# 社交动态管理API接口文档

## 基本信息
- **控制器名称**: SocialDynamicController
- **基础路径**: `/api/v1/social/dynamics`
- **描述**: 社交动态管理相关接口 - 严格对应接口版

## 接口列表

### 1. 创建动态

#### 接口信息
- **请求方式**: POST
- **接口路径**: `/api/v1/social/dynamics/create`
- **接口描述**: 发布新的社交动态

#### 请求参数

```json
{
  "userId": 123,
  "content": "动态内容",
  "dynamicType": "TEXT",
  "images": ["https://example.com/image1.jpg"],
  "location": "北京",
  "isPublic": true
}
```

#### 响应参数

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "userId": 123,
    "userNickname": "用户昵称",
    "userAvatar": "https://example.com/avatar.jpg",
    "content": "动态内容",
    "dynamicType": "TEXT",
    "images": ["https://example.com/image1.jpg"],
    "location": "北京",
    "isPublic": true,
    "likeCount": 0,
    "commentCount": 0,
    "shareCount": 0,
    "status": "ACTIVE",
    "createTime": "2024-01-01T00:00:00",
    "updateTime": "2024-01-01T00:00:00"
  }
}
```

### 2. 批量创建动态

#### 接口信息
- **请求方式**: POST
- **接口路径**: `/api/v1/social/dynamics/batch-create`
- **接口描述**: 批量发布多个社交动态

#### 请求参数

```json
{
  "dynamics": [
    {
      "userId": 123,
      "content": "动态内容1",
      "dynamicType": "TEXT"
    },
    {
      "userId": 123,
      "content": "动态内容2",
      "dynamicType": "IMAGE"
    }
  ],
  "operatorId": 456
}
```

### 3. 创建分享动态

#### 接口信息
- **请求方式**: POST
- **接口路径**: `/api/v1/social/dynamics/create-share`
- **接口描述**: 创建分享其他内容的动态

#### 请求参数

```json
{
  "userId": 123,
  "content": "分享内容",
  "shareTargetType": "CONTENT",
  "shareTargetId": 789,
  "shareTargetTitle": "分享的目标标题"
}
```

### 4. 更新动态

#### 接口信息
- **请求方式**: PUT
- **接口路径**: `/api/v1/social/dynamics/update`
- **接口描述**: 只允许更新动态内容，其他字段不允许修改

#### 请求参数

```json
{
  "id": 1,
  "userId": 123,
  "content": "更新后的动态内容"
}
```

### 5. 删除动态

#### 接口信息
- **请求方式**: DELETE
- **接口路径**: `/api/v1/social/dynamics/{id}`
- **接口描述**: 逻辑删除动态（设为deleted状态）

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| id | Long | 是 | 动态ID（路径参数） |
| operatorId | Long | 是 | 操作人ID |

#### 请求示例
```http
DELETE /api/v1/social/dynamics/1?operatorId=123
```

### 6. 获取动态详情

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/social/dynamics/{id}`
- **接口描述**: 根据ID获取动态详情

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| id | Long | 是 | - | 动态ID（路径参数） |
| includeDeleted | Boolean | 否 | false | 是否包含已删除的动态 |

#### 请求示例
```http
GET /api/v1/social/dynamics/1?includeDeleted=false
```

### 7. 分页查询动态

#### 接口信息
- **请求方式**: POST
- **接口路径**: `/api/v1/social/dynamics/query`
- **接口描述**: 支持多条件分页查询动态

#### 请求参数

```json
{
  "userId": 123,
  "dynamicType": "TEXT",
  "status": "ACTIVE",
  "keyword": "搜索关键词",
  "currentPage": 1,
  "pageSize": 20,
  "orderBy": "createTime",
  "orderDirection": "DESC"
}
```

### 8. 根据用户ID查询动态

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/social/dynamics/user/{userId}`
- **接口描述**: 获取指定用户的动态列表

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| userId | Long | 是 | - | 用户ID（路径参数） |
| currentPage | Integer | 否 | 1 | 当前页码 |
| pageSize | Integer | 否 | 20 | 页面大小 |
| status | String | 否 | - | 状态 |
| dynamicType | String | 否 | - | 动态类型 |

#### 请求示例
```http
GET /api/v1/social/dynamics/user/123?currentPage=1&pageSize=10&status=ACTIVE
```

### 9. 根据动态类型查询

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/social/dynamics/type/{dynamicType}`
- **接口描述**: 获取指定类型的动态列表

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| dynamicType | String | 是 | - | 动态类型（路径参数） |
| currentPage | Integer | 否 | 1 | 当前页码 |
| pageSize | Integer | 否 | 20 | 页面大小 |
| status | String | 否 | - | 状态 |

### 10. 根据状态查询动态

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/social/dynamics/status/{status}`
- **接口描述**: 获取指定状态的动态列表

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| status | String | 是 | - | 状态（路径参数） |
| currentPage | Integer | 否 | 1 | 当前页码 |
| pageSize | Integer | 否 | 20 | 页面大小 |

### 11. 获取关注用户的动态流

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/social/dynamics/following/{userId}`
- **接口描述**: 获取用户关注的人发布的动态

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| userId | Long | 是 | - | 用户ID（路径参数） |
| currentPage | Integer | 否 | 1 | 当前页码 |
| pageSize | Integer | 否 | 20 | 页面大小 |
| status | String | 否 | - | 状态 |

### 12. 搜索动态内容

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/social/dynamics/search`
- **接口描述**: 按关键词搜索动态内容

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| keyword | String | 是 | - | 搜索关键词 |
| currentPage | Integer | 否 | 1 | 当前页码 |
| pageSize | Integer | 否 | 20 | 页面大小 |
| status | String | 否 | - | 状态 |

### 13. 获取热门动态

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/social/dynamics/hot`
- **接口描述**: 按互动数排序获取热门动态

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| currentPage | Integer | 否 | 1 | 当前页码 |
| pageSize | Integer | 否 | 20 | 页面大小 |
| status | String | 否 | - | 状态 |
| dynamicType | String | 否 | - | 动态类型 |

### 14. 根据分享目标查询分享动态

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/social/dynamics/share-target`
- **接口描述**: 获取分享指定内容的动态列表

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| shareTargetType | String | 是 | - | 分享目标类型 |
| shareTargetId | Long | 是 | - | 分享目标ID |
| currentPage | Integer | 否 | 1 | 当前页码 |
| pageSize | Integer | 否 | 20 | 页面大小 |
| status | String | 否 | - | 状态 |

### 15. 统计用户动态数量

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/social/dynamics/count/user/{userId}`
- **接口描述**: 统计指定用户的动态数量

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| userId | Long | 是 | - | 用户ID（路径参数） |
| status | String | 否 | - | 状态 |
| dynamicType | String | 否 | - | 动态类型 |

### 16. 统计动态类型数量

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/social/dynamics/count/type/{dynamicType}`
- **接口描述**: 统计指定类型的动态数量

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| dynamicType | String | 是 | - | 动态类型（路径参数） |
| status | String | 否 | - | 状态 |

### 17. 统计时间范围内动态数量

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/social/dynamics/count/time-range`
- **接口描述**: 统计指定时间范围内的动态数量

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| startTime | String | 是 | - | 开始时间 |
| endTime | String | 是 | - | 结束时间 |
| status | String | 否 | - | 状态 |

### 18. 增加点赞数

#### 接口信息
- **请求方式**: POST
- **接口路径**: `/api/v1/social/dynamics/{id}/like`
- **接口描述**: 为指定动态增加点赞数

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| id | Long | 是 | 动态ID（路径参数） |
| operatorId | Long | 是 | 操作人ID |

### 19. 减少点赞数

#### 接口信息
- **请求方式**: DELETE
- **接口路径**: `/api/v1/social/dynamics/{id}/like`
- **接口描述**: 为指定动态减少点赞数

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| id | Long | 是 | 动态ID（路径参数） |
| operatorId | Long | 是 | 操作人ID |

### 20. 增加评论数

#### 接口信息
- **请求方式**: POST
- **接口路径**: `/api/v1/social/dynamics/{id}/comment`
- **接口描述**: 为指定动态增加评论数

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| id | Long | 是 | 动态ID（路径参数） |
| operatorId | Long | 是 | 操作人ID |

### 21. 增加分享数

#### 接口信息
- **请求方式**: POST
- **接口路径**: `/api/v1/social/dynamics/{id}/share`
- **接口描述**: 为指定动态增加分享数

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| id | Long | 是 | 动态ID（路径参数） |
| operatorId | Long | 是 | 操作人ID |

### 22. 批量更新统计数据

#### 接口信息
- **请求方式**: PUT
- **接口路径**: `/api/v1/social/dynamics/{id}/statistics`
- **接口描述**: 批量更新动态的统计数据

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| id | Long | 是 | 动态ID（路径参数） |
| likeCount | Long | 否 | 点赞数 |
| commentCount | Long | 否 | 评论数 |
| shareCount | Long | 否 | 分享数 |
| operatorId | Long | 是 | 操作人ID |

### 23. 更新动态状态

#### 接口信息
- **请求方式**: PUT
- **接口路径**: `/api/v1/social/dynamics/{id}/status`
- **接口描述**: 更新指定动态的状态

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| id | Long | 是 | 动态ID（路径参数） |
| status | String | 是 | 新状态 |
| operatorId | Long | 是 | 操作人ID |

### 24. 批量更新动态状态

#### 接口信息
- **请求方式**: PUT
- **接口路径**: `/api/v1/social/dynamics/batch-status`
- **接口描述**: 批量更新多个动态的状态

#### 请求参数

```json
{
  "dynamicIds": [1, 2, 3],
  "status": "ACTIVE",
  "operatorId": 123
}
```

### 25. 更新用户冗余信息

#### 接口信息
- **请求方式**: PUT
- **接口路径**: `/api/v1/social/dynamics/user/{userId}/info`
- **接口描述**: 同步更新动态中的用户信息

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID（路径参数） |
| userNickname | String | 否 | 用户昵称 |
| userAvatar | String | 否 | 用户头像 |
| operatorId | Long | 是 | 操作人ID |

### 26. 数据清理

#### 接口信息
- **请求方式**: DELETE
- **接口路径**: `/api/v1/social/dynamics/cleanup`
- **接口描述**: 物理删除指定状态的历史动态

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| status | String | 是 | 状态 |
| beforeTime | String | 是 | 截止时间 |
| limit | Integer | 否 | 限制数量 |
| operatorId | Long | 是 | 操作人ID |

### 27. 查询最新动态

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/social/dynamics/latest`
- **接口描述**: 获取最新发布的动态列表

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| limit | Integer | 否 | 10 | 限制数量 |
| status | String | 否 | - | 状态 |

### 28. 查询用户最新动态

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/social/dynamics/user/{userId}/latest`
- **接口描述**: 获取指定用户最新发布的动态列表

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| userId | Long | 是 | - | 用户ID（路径参数） |
| limit | Integer | 否 | 10 | 限制数量 |
| status | String | 否 | - | 状态 |

### 29. 查询分享动态列表

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/social/dynamics/share/{shareTargetType}`
- **接口描述**: 获取指定类型的分享动态列表

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| shareTargetType | String | 是 | - | 目标类型（路径参数） |
| limit | Integer | 否 | 10 | 限制数量 |
| status | String | 否 | - | 状态 |

### 30. 系统健康检查

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/social/dynamics/health`
- **接口描述**: 检查社交动态系统运行状态

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

## 动态记录字段说明

| 字段名 | 类型 | 描述 |
|--------|------|------|
| id | Long | 动态ID |
| userId | Long | 用户ID |
| userNickname | String | 用户昵称 |
| userAvatar | String | 用户头像 |
| content | String | 动态内容 |
| dynamicType | String | 动态类型 |
| images | Array | 图片列表 |
| location | String | 位置信息 |
| isPublic | Boolean | 是否公开 |
| likeCount | Integer | 点赞数 |
| commentCount | Integer | 评论数 |
| shareCount | Integer | 分享数 |
| status | String | 动态状态 |
| shareTargetType | String | 分享目标类型 |
| shareTargetId | Long | 分享目标ID |
| shareTargetTitle | String | 分享目标标题 |
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
5. 动态类型包括：TEXT（文本）、IMAGE（图片）、VIDEO（视频）、SHARE（分享）
6. 动态状态包括：ACTIVE（活跃）、DELETED（已删除）、HIDDEN（隐藏）
7. 分享目标类型包括：CONTENT（内容）、USER（用户）、GOODS（商品）
