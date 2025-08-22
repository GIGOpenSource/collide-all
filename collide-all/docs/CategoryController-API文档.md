# 分类管理API接口文档

## 基本信息
- **控制器名称**: CategoryController
- **基础路径**: `/api/v1/categories`
- **描述**: 分类的增删改查、层级管理、统计分析等功能

## 接口列表

### 1. 分页查询分类

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/categories/query`
- **接口描述**: 支持按父分类、状态等条件分页查询

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| parentId | Long | 否 | - | 父分类ID |
| status | String | 否 | - | 状态 |
| currentPage | Integer | 否 | 1 | 当前页码 |
| pageSize | Integer | 否 | 20 | 页面大小 |
| orderBy | String | 否 | createTime | 排序字段 |
| orderDirection | String | 否 | DESC | 排序方向 |

#### 请求示例
```http
GET /api/v1/categories/query?parentId=1&status=ACTIVE&currentPage=1&pageSize=10
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
        "name": "分类名称",
        "description": "分类描述",
        "parentId": null,
        "level": 1,
        "sortOrder": 1,
        "status": "ACTIVE",
        "icon": "https://example.com/icon.png",
        "createTime": "2024-01-01T00:00:00",
        "updateTime": "2024-01-01T00:00:00"
      }
    ],
    "total": 100,
    "currentPage": 1,
    "pageSize": 20,
    "totalPages": 5
  }
}
```

### 2. 搜索分类

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/categories/search`
- **接口描述**: 根据关键词搜索分类

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| keyword | String | 是 | - | 搜索关键词 |
| parentId | Long | 否 | - | 父分类ID |
| status | String | 否 | - | 状态 |
| currentPage | Integer | 否 | 1 | 当前页码 |
| pageSize | Integer | 否 | 20 | 页面大小 |
| orderBy | String | 否 | createTime | 排序字段 |
| orderDirection | String | 否 | DESC | 排序方向 |

#### 请求示例
```http
GET /api/v1/categories/search?keyword=小说&currentPage=1&pageSize=10
```

### 3. 获取根分类列表

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/categories/root`
- **接口描述**: 获取所有顶级分类

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| status | String | 否 | - | 状态 |
| currentPage | Integer | 否 | 1 | 当前页码 |
| pageSize | Integer | 否 | 20 | 页面大小 |
| orderBy | String | 否 | sortOrder | 排序字段 |
| orderDirection | String | 否 | ASC | 排序方向 |

#### 请求示例
```http
GET /api/v1/categories/root?status=ACTIVE&currentPage=1&pageSize=10
```

### 4. 获取子分类列表

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/categories/{parentId}/children`
- **接口描述**: 获取指定父分类的子分类

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| parentId | Long | 是 | - | 父分类ID（路径参数） |
| status | String | 否 | - | 状态 |
| currentPage | Integer | 否 | 1 | 当前页码 |
| pageSize | Integer | 否 | 20 | 页面大小 |
| orderBy | String | 否 | sortOrder | 排序字段 |
| orderDirection | String | 否 | ASC | 排序方向 |

#### 请求示例
```http
GET /api/v1/categories/1/children?status=ACTIVE&currentPage=1&pageSize=10
```

### 5. 获取分类树

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/categories/tree`
- **接口描述**: 获取完整的分类层级结构

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| rootId | Long | 否 | - | 根分类ID |
| maxDepth | Integer | 否 | 3 | 最大层级深度 |
| status | String | 否 | - | 状态 |
| orderBy | String | 否 | sortOrder | 排序字段 |
| orderDirection | String | 否 | ASC | 排序方向 |

#### 请求示例
```http
GET /api/v1/categories/tree?rootId=1&maxDepth=3&status=ACTIVE
```

#### 响应参数

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "name": "根分类",
      "description": "根分类描述",
      "parentId": null,
      "level": 1,
      "sortOrder": 1,
      "status": "ACTIVE",
      "children": [
        {
          "id": 2,
          "name": "子分类",
          "description": "子分类描述",
          "parentId": 1,
          "level": 2,
          "sortOrder": 1,
          "status": "ACTIVE",
          "children": []
        }
      ]
    }
  ]
}
```

### 6. 获取分类路径

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/categories/{categoryId}/path`
- **接口描述**: 获取从根分类到指定分类的完整路径

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| categoryId | Long | 是 | - | 分类ID（路径参数） |

#### 请求示例
```http
GET /api/v1/categories/5/path
```

#### 响应参数

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "name": "根分类",
      "description": "根分类描述",
      "parentId": null,
      "level": 1,
      "sortOrder": 1,
      "status": "ACTIVE"
    },
    {
      "id": 5,
      "name": "目标分类",
      "description": "目标分类描述",
      "parentId": 1,
      "level": 2,
      "sortOrder": 1,
      "status": "ACTIVE"
    }
  ]
}
```

### 7. 获取活跃分类

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/categories/active`
- **接口描述**: 获取所有状态为活跃的分类

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| parentId | Long | 否 | - | 父分类ID |
| orderBy | String | 否 | sortOrder | 排序字段 |
| orderDirection | String | 否 | ASC | 排序方向 |

#### 请求示例
```http
GET /api/v1/categories/active?parentId=1
```

### 8. 获取热门分类

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/api/v1/categories/popular`
- **接口描述**: 获取使用频率最高的分类

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| limit | Integer | 否 | 10 | 限制数量 |
| parentId | Long | 否 | - | 父分类ID |

#### 请求示例
```http
GET /api/v1/categories/popular?limit=20&parentId=1
```

## 分类记录字段说明

| 字段名 | 类型 | 描述 |
|--------|------|------|
| id | Long | 分类ID |
| name | String | 分类名称 |
| description | String | 分类描述 |
| parentId | Long | 父分类ID |
| level | Integer | 分类层级 |
| sortOrder | Integer | 排序顺序 |
| status | String | 分类状态 |
| icon | String | 分类图标 |
| children | Array | 子分类列表 |
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
5. 分类状态包括：ACTIVE（活跃）、INACTIVE（非活跃）、DELETED（已删除）
6. 分类树接口返回的是树形结构，包含children字段
7. 分类路径接口返回的是从根到目标分类的完整路径数组
