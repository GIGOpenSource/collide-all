# 用户兴趣标签管理API接口文档

## 基本信息
- **控制器名称**: UserInterestTagController
- **基础路径**: `/api/v1/user-interest-tags`
- **描述**: 用户与标签的兴趣关系管理、兴趣度分析、个性化推荐等功能

## 接口列表

### 1. 用户兴趣标签列表查询

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/user-interest-tags/list`
- **接口描述**: 支持按用户、标签、兴趣度等条件查询用户兴趣标签列表

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| userId | Long | 否 | - | 用户ID |
| tagId | Long | 否 | - | 标签ID |
| minInterestLevel | BigDecimal | 否 | - | 兴趣度最小值 |
| maxInterestLevel | BigDecimal | 否 | - | 兴趣度最大值 |
| status | String | 否 | - | 关联状态 |
| keyword | String | 否 | - | 关键词搜索 |
| orderBy | String | 否 | interestLevel | 排序字段 |
| orderDirection | String | 否 | DESC | 排序方向 |
| currentPage | Integer | 否 | 1 | 当前页码 |
| pageSize | Integer | 否 | 20 | 页面大小 |

#### 请求示例
```http
GET /api/v1/user-interest-tags/list?userId=1&minInterestLevel=0.5&status=ACTIVE&currentPage=1&pageSize=10
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
        "tagId": 5,
        "tagName": "科幻",
        "tagDescription": "科幻类内容",
        "interestLevel": 0.85,
        "status": "ACTIVE",
        "createTime": "2024-01-01T00:00:00",
        "updateTime": "2024-01-01T00:00:00"
      }
    ],
    "total": 15,
    "currentPage": 1,
    "pageSize": 10,
    "totalPages": 2
  }
}
```

#### 响应字段说明

| 字段名 | 类型 | 描述 |
|--------|------|------|
| code | Integer | 响应状态码 |
| message | String | 响应消息 |
| data.records | Array | 用户兴趣标签记录列表 |
| data.total | Integer | 总记录数 |
| data.currentPage | Integer | 当前页码 |
| data.pageSize | Integer | 页面大小 |
| data.totalPages | Integer | 总页数 |

### 2. 获取用户的兴趣标签统计

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/user-interest-tags/stats/{userId}`
- **接口描述**: 获取指定用户的兴趣标签统计信息

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| userId | Long | 是 | - | 用户ID（路径参数） |

#### 请求示例
```http
GET /api/v1/user-interest-tags/stats/1
```

### 3. 获取标签的热门用户

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/user-interest-tags/tag/{tagId}/users`
- **接口描述**: 获取对指定标签感兴趣的用户列表

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| tagId | Long | 是 | - | 标签ID（路径参数） |
| minInterestLevel | BigDecimal | 否 | - | 最小兴趣度 |
| currentPage | Integer | 否 | 1 | 当前页码 |
| pageSize | Integer | 否 | 20 | 页面大小 |

#### 请求示例
```http
GET /api/v1/user-interest-tags/tag/5/users?minInterestLevel=0.7&currentPage=1&pageSize=20
```

## 用户兴趣标签字段说明

| 字段名 | 类型 | 描述 |
|--------|------|------|
| id | Long | 关联ID |
| userId | Long | 用户ID |
| tagId | Long | 标签ID |
| tagName | String | 标签名称 |
| tagDescription | String | 标签描述 |
| interestLevel | BigDecimal | 兴趣度（0.0-1.0） |
| status | String | 关联状态 |
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
5. 兴趣度范围为0.0到1.0，数值越高表示兴趣越大
6. 关联状态包括：ACTIVE（激活）、INACTIVE（非激活）、DELETED（已删除）
7. 排序字段支持：interestLevel（兴趣度）、createTime（创建时间）、updateTime（更新时间）
8. 关键词搜索支持标签名称和描述的模糊匹配
