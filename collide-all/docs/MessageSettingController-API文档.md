# 消息设置管理API接口文档

## 基本信息
- **控制器名称**: MessageSettingController
- **基础路径**: `/api/v1/message-settings`
- **描述**: 消息设置相关的API接口，管理用户的消息偏好设置和权限控制

## 接口列表

### 1. 消息设置列表查询

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/message-settings/list`
- **接口描述**: 支持按用户、设置类型、状态等条件查询消息设置列表

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| userId | Long | 否 | - | 用户ID |
| settingType | String | 否 | - | 设置类型 |
| status | String | 否 | - | 设置状态 |
| isEnabled | Boolean | 否 | - | 是否启用 |
| keyword | String | 否 | - | 关键词搜索 |
| orderBy | String | 否 | createTime | 排序字段 |
| orderDirection | String | 否 | DESC | 排序方向 |
| currentPage | Integer | 否 | 1 | 当前页码 |
| pageSize | Integer | 否 | 20 | 页面大小 |

#### 请求示例
```http
GET /api/v1/message-settings/list?userId=1&settingType=NOTIFICATION&isEnabled=true&currentPage=1&pageSize=10
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
        "settingType": "NOTIFICATION",
        "settingName": "消息通知",
        "settingValue": "true",
        "status": "ACTIVE",
        "isEnabled": true,
        "description": "是否接收消息通知",
        "createTime": "2024-01-01T00:00:00",
        "updateTime": "2024-01-01T00:00:00"
      }
    ],
    "total": 25,
    "currentPage": 1,
    "pageSize": 10,
    "totalPages": 3
  }
}
```

## 消息设置字段说明

| 字段名 | 类型 | 描述 |
|--------|------|------|
| id | Long | 设置ID |
| userId | Long | 用户ID |
| settingType | String | 设置类型 |
| settingName | String | 设置名称 |
| settingValue | String | 设置值 |
| status | String | 设置状态 |
| isEnabled | Boolean | 是否启用 |
| description | String | 设置描述 |
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
5. 设置类型包括：NOTIFICATION（消息通知）、PRIVACY（隐私设置）、PERMISSION（权限设置）
6. 设置状态包括：ACTIVE（激活）、INACTIVE（非激活）、DELETED（已删除）
7. 排序字段支持：createTime（创建时间）、updateTime（更新时间）
8. 通过MessageSettingFacadeService可以调用更多消息设置相关功能
