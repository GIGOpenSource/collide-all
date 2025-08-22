# UserController API 文档

## 概述

UserController 提供用户相关的增删改查功能，包括创建新用户、查询用户信息、更新用户信息、删除用户（逻辑删除）和分页查询用户列表。

### 基础信息

- **基础路径**: `/api/v1/users`
- **Content-Type**: `application/json`
- **响应格式**: JSON

## API 接口列表

### 1. 创建用户

**接口地址**: `POST /api/v1/users`

**接口描述**: 创建新用户，系统会自动设置默认的VIP状态和用户状态

**请求参数**:

| 参数名 | 类型 | 必填 | 描述 | 示例值 |
|--------|------|------|------|--------|
| username | String | 是 | 用户名 | "zhangsan" |
| nickname | String | 是 | 昵称 | "张三" |
| email | String | 否 | 邮箱地址 | "zhangsan@example.com" |
| phone | String | 否 | 手机号码 | "13800138000" |
| avatar | String | 否 | 头像URL | "https://example.com/avatar.jpg" |

**请求示例**:
```json
{
    "username": "zhangsan",
    "nickname": "张三",
    "email": "zhangsan@example.com",
    "phone": "13800138000",
    "avatar": "https://example.com/avatar.jpg"
}
```

**响应示例**:
```json
{
    "code": 200,
    "message": "操作成功",
    "data": {
        "id": 123,
        "username": "zhangsan",
        "nickname": "张三",
        "email": "zhangsan@example.com",
        "phone": "13800138000",
        "avatar": "https://example.com/avatar.jpg",
        "status": "active",
        "isVip": "N",
        "createTime": "2024-01-01T10:00:00",
        "updateTime": "2024-01-01T10:00:00"
    }
}
```

**默认值设置**:
- 用户角色：默认为"user"（普通用户）
- VIP状态：默认为"N"（非VIP）
- 用户状态：默认为"active"（活跃）
- 创建时间：自动设置为当前时间

### 2. 根据ID查询用户

**接口地址**: `GET /api/v1/users`

**接口描述**: 根据用户ID查询用户的详细信息，包括基本信息、状态信息和时间信息

**请求参数**:

| 参数名 | 类型 | 必填 | 描述 | 示例值 |
|--------|------|------|------|--------|
| id | Long | 是 | 用户ID | 123 |

**请求示例**:
```
GET /api/v1/users?id=123
```

**响应示例**:
```json
{
    "code": 200,
    "message": "操作成功",
    "data": {
        "id": 123,
        "username": "zhangsan",
        "nickname": "张三",
        "email": "zhangsan@example.com",
        "phone": "13800138000",
        "avatar": "https://example.com/avatar.jpg",
        "status": "active",
        "isVip": "N",
        "createTime": "2024-01-01T10:00:00",
        "updateTime": "2024-01-01T10:00:00"
    }
}
```

### 3. 编辑用户信息

**接口地址**: `PUT /api/v1/users/{id}`

**接口描述**: 根据用户ID更新用户信息，支持部分字段更新，只更新请求中提供的字段

**路径参数**:

| 参数名 | 类型 | 必填 | 描述 | 示例值 |
|--------|------|------|------|--------|
| id | Long | 是 | 用户ID | 123 |

**请求参数**:

| 参数名 | 类型 | 必填 | 描述 | 示例值 |
|--------|------|------|------|--------|
| id | Long | 是 | 用户ID | 123 |
| nickname | String | 否 | 昵称 | "新昵称" |
| email | String | 否 | 邮箱地址 | "newemail@example.com" |
| phone | String | 否 | 手机号码 | "13900139000" |
| avatar | String | 否 | 头像URL | "https://example.com/new-avatar.jpg" |
| isVip | String | 否 | VIP状态：Y-是VIP，N-非VIP | "Y" |

**请求示例**:
```json
{
    "id": 123,
    "nickname": "新昵称",
    "isVip": "Y"
}
```

**响应示例**:
```json
{
    "code": 200,
    "message": "操作成功",
    "data": {
        "id": 123,
        "username": "zhangsan",
        "nickname": "新昵称",
        "email": "zhangsan@example.com",
        "phone": "13800138000",
        "avatar": "https://example.com/avatar.jpg",
        "status": "active",
        "isVip": "Y",
        "createTime": "2024-01-01T10:00:00",
        "updateTime": "2024-01-01T11:00:00"
    }
}
```

**更新规则**:
- 只更新请求中提供的字段
- 未提供的字段保持原值不变
- 支持更新：昵称、邮箱、手机号、头像、VIP状态
- 更新时间会自动设置为当前时间

### 4. 删除用户

**接口地址**: `DELETE /api/v1/users/{id}`

**接口描述**: 根据用户ID删除用户，采用逻辑删除方式，不会物理删除数据库记录

**路径参数**:

| 参数名 | 类型 | 必填 | 描述 | 示例值 |
|--------|------|------|------|--------|
| id | Long | 是 | 用户ID | 123 |

**请求示例**:
```
DELETE /api/v1/users/123
```

**响应示例**:
```json
{
    "code": 200,
    "message": "操作成功",
    "data": null
}
```

**删除方式**:
- 不会物理删除数据库记录
- 将deleted字段设置为1
- 更新时间会自动设置为当前时间
- 删除后的用户不会出现在查询结果中

### 5. 分页查询用户列表

**接口地址**: `GET /api/v1/users/list`

**接口描述**: 分页查询用户列表，支持条件筛选。不传条件查询所有用户，传入条件支持模糊搜索用户名、昵称、邮箱、手机号，或精确匹配状态

**请求参数**:

| 参数名 | 类型 | 必填 | 描述 | 示例值 |
|--------|------|------|------|--------|
| condition | String | 否 | 查询条件，支持模糊搜索用户名、昵称、邮箱、手机号，或精确匹配状态 | "张三" |
| currentPage | Integer | 否 | 当前页码，默认为1 | 1 |
| pageSize | Integer | 否 | 页面大小，默认为20 | 20 |

**请求示例**:
```
GET /api/v1/users/list?condition=张三&currentPage=1&pageSize=20
```

**响应示例**:
```json
{
    "code": 200,
    "message": "操作成功",
    "data": {
        "records": [
            {
                "id": 123,
                "username": "zhangsan",
                "nickname": "张三",
                "email": "zhangsan@example.com",
                "phone": "13800138000",
                "avatar": "https://example.com/avatar.jpg",
                "status": "active",
                "isVip": "N",
                "createTime": "2024-01-01T10:00:00",
                "updateTime": "2024-01-01T10:00:00"
            }
        ],
        "currentPage": 1,
        "pageSize": 20,
        "total": 100
    }
}
```

**查询规则**:
- 如果不传查询条件，则查询所有用户
- 如果传入查询条件，支持以下模糊搜索：
  - 用户名（username）
  - 昵称（nickname）
  - 邮箱（email）
  - 手机号（phone）
  - 用户状态（status）精确匹配
- 结果按创建时间倒序排列
- 返回分页信息和用户列表

## 错误码说明

| 错误码 | 描述 | 示例 |
|--------|------|------|
| 200 | 操作成功 | 正常响应 |
| 400 | 请求参数错误 | 参数验证失败 |
| 404 | 用户不存在 | 查询的用户ID不存在 |
| 500 | 服务器内部错误 | 数据库连接异常等 |

## 数据模型

### UserCreateRequest（用户创建请求）

```json
{
    "username": "string",     // 用户名（必填）
    "nickname": "string",     // 昵称（必填）
    "email": "string",        // 邮箱地址（可选）
    "phone": "string",        // 手机号码（可选）
    "avatar": "string"        // 头像URL（可选）
}
```

### UserUpdateRequest（用户更新请求）

```json
{
    "id": "long",             // 用户ID（必填）
    "nickname": "string",     // 昵称（可选）
    "email": "string",        // 邮箱地址（可选）
    "phone": "string",        // 手机号码（可选）
    "avatar": "string",       // 头像URL（可选）
    "isVip": "string"         // VIP状态：Y-是VIP，N-非VIP（可选）
}
```

### UserResponse（用户响应）

```json
{
    "id": "long",                     // 用户ID
    "username": "string",             // 用户名
    "nickname": "string",             // 昵称
    "avatar": "string",               // 头像URL
    "email": "string",                // 邮箱地址
    "phone": "string",                // 手机号码
    "roles": ["string"],              // 用户角色列表
    "status": "string",               // 用户状态
    "bio": "string",                  // 个人简介
    "birthday": "date",               // 生日
    "gender": "string",               // 性别
    "location": "string",             // 所在地
    "followerCount": "long",          // 粉丝数量
    "followingCount": "long",         // 关注数量
    "contentCount": "long",           // 内容数量
    "likeCount": "long",              // 获赞数量
    "vipExpireTime": "datetime",      // VIP过期时间
    "isVip": "string",                // VIP状态：Y-是VIP，N-非VIP
    "lastLoginTime": "datetime",      // 最后登录时间
    "loginCount": "long",             // 登录次数
    "inviteCode": "string",           // 邀请码
    "inviterId": "long",              // 邀请人ID
    "invitedCount": "long",           // 已邀请人数
    "walletBalance": "decimal",       // 钱包余额
    "walletFrozen": "decimal",        // 冻结金额
    "walletStatus": "string",         // 钱包状态
    "createTime": "datetime",         // 创建时间
    "updateTime": "datetime"          // 更新时间
}
```

### PageResponse（分页响应）

```json
{
    "records": ["object"],    // 数据列表
    "currentPage": "int",     // 当前页码
    "pageSize": "int",        // 页面大小
    "total": "long"           // 总记录数
}
```

## 注意事项

1. **VIP状态格式**: 使用"Y"表示是VIP，"N"表示非VIP
2. **逻辑删除**: 删除操作采用逻辑删除，不会物理删除数据库记录
3. **部分更新**: 更新操作支持部分字段更新，只更新请求中提供的字段
4. **分页查询**: 支持灵活的条件筛选，不传条件查询所有用户
5. **时间格式**: 时间字段使用ISO 8601格式（YYYY-MM-DDTHH:mm:ss）
6. **参数验证**: 所有必填参数都有相应的验证规则
7. **错误处理**: 所有接口都有完整的异常处理和错误信息返回

## 版本信息

- **版本**: 1.0.0
- **作者**: GIG Team
- **更新时间**: 2024-01-01
