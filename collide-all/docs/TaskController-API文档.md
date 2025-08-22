# 任务管理API接口文档

## 基本信息
- **控制器名称**: TaskController
- **基础路径**: `/api/v1/tasks`
- **描述**: 任务管理相关接口

## 接口列表

### 1. 创建任务

#### 接口信息
- **请求方式**: POST
- **接口路径**: `/api/v1/tasks`
- **接口描述**: 创建新任务

#### 请求参数

```jsonF
{
  "title": "任务标题",
  "description": "任务描述",
  "type": "DAILY",
  "reward": 100,
  "startTime": "2024-01-01T00:00:00",
  "endTime": "2024-12-31T23:59:59",
  "targetValue": 10,
  "targetType": "COUNT"
}
```

#### 响应参数

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "title": "任务标题",
    "description": "任务描述",
    "type": "DAILY",
    "reward": 100,
    "startTime": "2024-01-01T00:00:00",
    "endTime": "2024-12-31T23:59:59",
    "targetValue": 10,
    "targetType": "COUNT",
    "status": "ACTIVE",
    "createTime": "2024-01-01T00:00:00",
    "updateTime": "2024-01-01T00:00:00"
  }
}
```

### 2. 获取任务详情

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/tasks/{id}`
- **接口描述**: 根据任务ID获取详情

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| id | Long | 是 | 任务ID（路径参数） |

#### 请求示例
```http
GET /api/v1/tasks/1
```

### 3. 更新任务

#### 接口信息
- **请求方式**: PUT
- **接口路径**: `/api/v1/tasks/{id}`
- **接口描述**: 更新任务信息

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| id | Long | 是 | 任务ID（路径参数） |

#### 请求体

```json
{
  "title": "新任务标题",
  "description": "新任务描述",
  "reward": 200,
  "targetValue": 20,
  "status": "ACTIVE"
}
```

### 4. 删除任务

#### 接口信息
- **请求方式**: DELETE
- **接口路径**: `/api/v1/tasks/{id}`
- **接口描述**: 删除任务

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| id | Long | 是 | 任务ID（路径参数） |

#### 请求示例
```http
DELETE /api/v1/tasks/1
```

### 5. 分页查询任务

#### 接口信息
- **请求方式**: POST
- **接口路径**: `/api/v1/tasks/query`
- **接口描述**: 分页查询任务列表

#### 请求参数

```json
{
  "title": "任务",
  "type": "DAILY",
  "status": "ACTIVE",
  "currentPage": 1,
  "pageSize": 20,
  "orderBy": "createTime",
  "orderDirection": "DESC"
}
```

### 6. 任务列表

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/tasks`
- **接口描述**: 获取任务列表

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| type | String | 否 | - | 任务类型 |
| status | String | 否 | - | 任务状态 |
| currentPage | Integer | 否 | 1 | 当前页码 |
| pageSize | Integer | 否 | 20 | 页面大小 |

#### 请求示例
```http
GET /api/v1/tasks?type=DAILY&status=ACTIVE&currentPage=1&pageSize=10
```

### 7. 用户任务列表

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/tasks/user/{userId}`
- **接口描述**: 获取用户任务列表

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| userId | Long | 是 | - | 用户ID（路径参数） |
| status | String | 否 | - | 任务状态 |
| currentPage | Integer | 否 | 1 | 当前页码 |
| pageSize | Integer | 否 | 20 | 页面大小 |

#### 请求示例
```http
GET /api/v1/tasks/user/123?status=ACTIVE&currentPage=1&pageSize=10
```

### 8. 接受任务

#### 接口信息
- **请求方式**: POST
- **接口路径**: `/api/v1/tasks/{id}/accept`
- **接口描述**: 用户接受任务

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| id | Long | 是 | 任务ID（路径参数） |

#### 请求体

```json
{
  "userId": 123
}
```

### 9. 完成任务

#### 接口信息
- **请求方式**: POST
- **接口路径**: `/api/v1/tasks/{id}/complete`
- **接口描述**: 用户完成任务

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| id | Long | 是 | 任务ID（路径参数） |

#### 请求体

```json
{
  "userId": 123,
  "progress": 10
}
```

### 10. 放弃任务

#### 接口信息
- **请求方式**: POST
- **接口路径**: `/api/v1/tasks/{id}/abandon`
- **接口描述**: 用户放弃任务

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| id | Long | 是 | 任务ID（路径参数） |

#### 请求体

```json
{
  "userId": 123,
  "reason": "时间不够"
}
```

### 11. 任务进度更新

#### 接口信息
- **请求方式**: PUT
- **接口路径**: `/api/v1/tasks/{id}/progress`
- **接口描述**: 更新任务进度

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| id | Long | 是 | 任务ID（路径参数） |

#### 请求体

```json
{
  "userId": 123,
  "progress": 5,
  "action": "READ_CONTENT"
}
```

### 12. 任务奖励发放

#### 接口信息
- **请求方式**: POST
- **接口路径**: `/api/v1/tasks/{id}/reward`
- **接口描述**: 发放任务奖励

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| id | Long | 是 | 任务ID（路径参数） |

#### 请求体

```json
{
  "userId": 123,
  "rewardType": "COIN",
  "rewardAmount": 100
}
```

### 13. 任务统计

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/tasks/statistics`
- **接口描述**: 获取任务统计信息

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| userId | Long | 否 | 用户ID |
| startTime | String | 否 | 开始时间 |
| endTime | String | 否 | 结束时间 |

#### 请求示例
```http
GET /api/v1/tasks/statistics?userId=123&startTime=2024-01-01&endTime=2024-12-31
```

### 14. 用户任务进度

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/tasks/user/{userId}/progress`
- **接口描述**: 获取用户任务进度

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID（路径参数） |

#### 请求示例
```http
GET /api/v1/tasks/user/123/progress
```

### 15. 任务推荐

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/tasks/recommend/{userId}`
- **接口描述**: 获取推荐任务

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| userId | Long | 是 | - | 用户ID（路径参数） |
| limit | Integer | 否 | 10 | 限制数量 |

#### 请求示例
```http
GET /api/v1/tasks/recommend/123?limit=20
```

### 16. 批量更新任务状态

#### 接口信息
- **请求方式**: PUT
- **接口路径**: `/api/v1/tasks/batch-status`
- **接口描述**: 批量更新任务状态

#### 请求参数

```json
{
  "taskIds": [1, 2, 3],
  "status": "INACTIVE",
  "reason": "批量下线"
}
```

### 17. 任务模板创建

#### 接口信息
- **请求方式**: POST
- **接口路径**: `/api/v1/tasks/template`
- **接口描述**: 创建任务模板

#### 请求参数

```json
{
  "name": "每日签到",
  "description": "每日签到任务模板",
  "type": "DAILY",
  "reward": 10,
  "targetValue": 1,
  "targetType": "COUNT",
  "conditions": {
    "userLevel": 1,
    "vipRequired": false
  }
}
```

### 18. 任务模板列表

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/tasks/templates`
- **接口描述**: 获取任务模板列表

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| type | String | 否 | - | 任务类型 |
| currentPage | Integer | 否 | 1 | 当前页码 |
| pageSize | Integer | 否 | 20 | 页面大小 |

#### 请求示例
```http
GET /api/v1/tasks/templates?type=DAILY&currentPage=1&pageSize=10
```

### 19. 任务完成记录

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/tasks/completion-records/{userId}`
- **接口描述**: 获取用户任务完成记录

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| userId | Long | 是 | - | 用户ID（路径参数） |
| currentPage | Integer | 否 | 1 | 当前页码 |
| pageSize | Integer | 否 | 20 | 页面大小 |

#### 请求示例
```http
GET /api/v1/tasks/completion-records/123?currentPage=1&pageSize=10
```

### 20. 任务重置

#### 接口信息
- **请求方式**: POST
- **接口路径**: `/api/v1/tasks/{id}/reset`
- **接口描述**: 重置任务进度

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| id | Long | 是 | 任务ID（路径参数） |

#### 请求体

```json
{
  "userId": 123,
  "reason": "系统重置"
}
```

## 任务记录字段说明

| 字段名 | 类型 | 描述 |
|--------|------|------|
| id | Long | 任务ID |
| title | String | 任务标题 |
| description | String | 任务描述 |
| type | String | 任务类型 |
| reward | Long | 奖励金额（分） |
| startTime | String | 开始时间 |
| endTime | String | 结束时间 |
| targetValue | Integer | 目标值 |
| targetType | String | 目标类型 |
| status | String | 任务状态 |
| createTime | String | 创建时间 |
| updateTime | String | 更新时间 |

## 用户任务记录字段说明

| 字段名 | 类型 | 描述 |
|--------|------|------|
| id | Long | 用户任务ID |
| userId | Long | 用户ID |
| taskId | Long | 任务ID |
| progress | Integer | 当前进度 |
| status | String | 任务状态 |
| acceptTime | String | 接受时间 |
| completeTime | String | 完成时间 |
| rewardReceived | Boolean | 是否已领取奖励 |
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
4. 奖励金额字段单位为分，需要转换为元显示
5. 任务状态包括：ACTIVE（活跃）、INACTIVE（非活跃）、COMPLETED（已完成）、EXPIRED（已过期）
6. 任务类型包括：DAILY（每日任务）、WEEKLY（每周任务）、MONTHLY（每月任务）、ONE_TIME（一次性任务）
7. 目标类型包括：COUNT（次数）、AMOUNT（金额）、TIME（时长）、DISTANCE（距离）
8. 用户任务状态包括：ACCEPTED（已接受）、IN_PROGRESS（进行中）、COMPLETED（已完成）、ABANDONED（已放弃）
9. 奖励类型包括：COIN（金币）、POINT（积分）、VIP_DAYS（VIP天数）、ITEM（道具）
