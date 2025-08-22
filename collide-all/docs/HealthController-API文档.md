# 健康检查API接口文档

## 基本信息
- **控制器名称**: HealthController
- **基础路径**: `/health`
- **描述**: 系统健康检查接口

## 接口列表

### 1. 完整健康检查

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/health`
- **接口描述**: 检查系统整体状态，包括数据库和Redis连接

#### 请求参数
无

#### 请求示例
```http
GET /health
```

#### 响应参数

```json
{
  "status": "UP",
  "timestamp": 1703123456789,
  "application": "Collide-All",
  "database": "UP",
  "redis": "UP"
}
```

#### 响应字段说明

| 字段名 | 类型 | 描述 |
|--------|------|------|
| status | String | 系统状态 |
| timestamp | Long | 时间戳 |
| application | String | 应用名称 |
| database | String | 数据库状态 |
| redis | String | Redis状态 |

### 2. 简单健康检查

#### 接口信息
- **请求方式**: GET
- **接口路径**: `/health/simple`
- **接口描述**: 快速检查系统是否启动

#### 请求参数
无

#### 请求示例
```http
GET /health/simple
```

#### 响应参数

```json
"OK"
```

## 错误码说明

| 错误码 | 描述 |
|--------|------|
| 200 | 成功 |
| 500 | 服务器内部错误 |

## 注意事项

1. 完整健康检查会检查数据库和Redis连接状态
2. 如果数据库或Redis连接失败，会返回相应的错误信息
3. 简单健康检查只返回"OK"字符串，用于快速判断服务是否启动
