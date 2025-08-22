# S3 存储模块 API 文档

## 📋 概述

S3存储模块为collide-all项目提供了完整的文件上传下载功能，基于AWS S3云存储服务，支持文件上传、下载、删除、预签名URL生成等功能。

## 🚀 快速开始

### 基础信息
- **服务地址**: `http://localhost:8080/collide-all`
- **API前缀**: `/api/s3`
- **支持格式**: JSON, Multipart/Form-Data
- **认证方式**: 无需认证（可根据需要添加）

---

## 📚 API接口列表

### 1. 文件上传接口

#### 接口信息
- **接口地址**: `POST /api/s3/upload`
- **功能描述**: 上传文件到S3云存储
- **Content-Type**: `multipart/form-data`

#### 请求参数
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| file | MultipartFile | 是 | 要上传的文件 |
| path | String | 否 | 存储路径（可选） |

#### 请求示例
```bash
curl -X POST \
  http://localhost:8080/collide-all/api/s3/upload \
  -H 'Content-Type: multipart/form-data' \
  -F 'file=@/path/to/your/file.jpg' \
  -F 'path=images/'
```

#### 响应示例
```json
{
  "success": true,
  "objectKey": "images/1703123456789.jpg",
  "etag": "\"abc123def456\"",
  "size": 1024000,
  "url": "https://your-bucket.s3.us-east-1.amazonaws.com/images/1703123456789.jpg"
}
```

---

### 2. 文件下载接口

#### 接口信息
- **接口地址**: `GET /api/s3/download/{objectKey}`
- **功能描述**: 直接下载文件
- **Content-Type**: `application/octet-stream`

#### 请求参数
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| objectKey | String | 是 | 文件在S3中的键名 |

#### 请求示例
```bash
curl -X GET \
  http://localhost:8080/collide-all/api/s3/download/images/1703123456789.jpg
```

---

### 3. 预签名URL生成接口

#### 接口信息
- **接口地址**: `GET /api/s3/presigned-url/{objectKey}`
- **功能描述**: 生成预签名下载URL
- **Content-Type**: `application/json`

#### 请求参数
| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| objectKey | String | 是 | - | 文件在S3中的键名 |
| expiration | Long | 否 | 3600 | 过期时间（秒） |

#### 请求示例
```bash
curl -X GET \
  "http://localhost:8080/collide-all/api/s3/presigned-url/images/1703123456789.jpg?expiration=7200"
```

#### 响应示例
```
https://your-bucket.s3.us-east-1.amazonaws.com/images/1703123456789.jpg?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=...
```

---

### 4. 文件删除接口

#### 接口信息
- **接口地址**: `DELETE /api/s3/{objectKey}`
- **功能描述**: 删除单个文件
- **Content-Type**: `application/json`

#### 请求参数
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| objectKey | String | 是 | 文件在S3中的键名 |

#### 请求示例
```bash
curl -X DELETE \
  http://localhost:8080/collide-all/api/s3/images/1703123456789.jpg
```

#### 响应示例
```json
true
```

---

### 5. 批量删除接口

#### 接口信息
- **接口地址**: `DELETE /api/s3/batch`
- **功能描述**: 批量删除多个文件
- **Content-Type**: `application/json`

#### 请求参数
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| body | Array<String> | 是 | 要删除的文件键名数组 |

#### 请求示例
```bash
curl -X DELETE \
  http://localhost:8080/collide-all/api/s3/batch \
  -H 'Content-Type: application/json' \
  -d '[
    "images/file1.jpg",
    "images/file2.jpg",
    "images/file3.jpg"
  ]'
```

#### 响应示例
```json
{
  "success": true,
  "deletedCount": 3,
  "deletedKeys": [
    "images/file1.jpg",
    "images/file2.jpg",
    "images/file3.jpg"
  ]
}
```

---

### 6. 文件信息查询接口

#### 接口信息
- **接口地址**: `GET /api/s3/info/{objectKey}`
- **功能描述**: 获取文件详细信息
- **Content-Type**: `application/json`

#### 请求参数
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| objectKey | String | 是 | 文件在S3中的键名 |

#### 请求示例
```bash
curl -X GET \
  http://localhost:8080/collide-all/api/s3/info/images/1703123456789.jpg
```

#### 响应示例
```json
{
  "objectKey": "images/1703123456789.jpg",
  "size": 1024000,
  "contentType": "image/jpeg",
  "lastModified": "2024-01-15T10:30:00Z",
  "etag": "\"abc123def456\""
}
```

---

### 7. 文件存在性检查接口

#### 接口信息
- **接口地址**: `GET /api/s3/exists/{objectKey}`
- **功能描述**: 检查文件是否存在
- **Content-Type**: `application/json`

#### 请求参数
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| objectKey | String | 是 | 文件在S3中的键名 |

#### 请求示例
```bash
curl -X GET \
  http://localhost:8080/collide-all/api/s3/exists/images/1703123456789.jpg
```

#### 响应示例
```json
true
```

---

## 🔧 测试接口

### 1. 模块状态检查

#### 接口信息
- **接口地址**: `GET /api/s3-test/status`
- **功能描述**: 检查S3模块状态

#### 请求示例
```bash
curl -X GET \
  http://localhost:8080/collide-all/api/s3-test/status
```

#### 响应示例
```json
{
  "success": true,
  "message": "S3模块状态检查完成",
  "s3ClientStatus": "SUCCESS",
  "s3ConfigStatus": "SUCCESS",
  "region": "us-east-1",
  "bucketName": "your-bucket-name",
  "springBootVersion": "3.2.2",
  "awsSdkVersion": "2.24.12",
  "javaVersion": "21",
  "moduleStatus": "READY"
}
```

### 2. 模块信息查询

#### 接口信息
- **接口地址**: `GET /api/s3-test/info`
- **功能描述**: 获取S3模块信息

#### 请求示例
```bash
curl -X GET \
  http://localhost:8080/collide-all/api/s3-test/info
```

#### 响应示例
```json
{
  "moduleName": "S3 Storage Module",
  "version": "1.0.0",
  "springBootVersion": "3.2.2",
  "awsSdkVersion": "2.24.12",
  "javaVersion": "21",
  "status": "ACTIVE",
  "basePath": "/api/s3"
}
```

---

## ⚙️ 配置说明

### 环境变量配置
```bash
# AWS访问密钥
export AWS_ACCESS_KEY_ID="your-access-key-id"
export AWS_SECRET_ACCESS_KEY="your-secret-access-key"

# AWS区域和存储桶
export AWS_REGION="us-east-1"
export S3_BUCKET_NAME="your-bucket-name"
```

### 配置文件配置
```yaml
aws:
  s3:
    access-key-id: ${AWS_ACCESS_KEY_ID:your-access-key-id}
    secret-access-key: ${AWS_SECRET_ACCESS_KEY:your-secret-access-key}
    region: ${AWS_REGION:us-east-1}
    bucket-name: ${S3_BUCKET_NAME:your-bucket-name}
    anti-hotlink:
      enabled: true
      allowed-domains:
        - "localhost"
        - "127.0.0.1"
        - "*.yourdomain.com"
      signature-expiration: 3600
```

---

## 🔒 安全特性

### 1. 防盗链保护
- 自动检查请求的Referer头
- 支持域名白名单配置
- 可配置重定向URL

### 2. 预签名URL
- 临时访问链接
- 可设置过期时间
- 提高安全性

### 3. 文件类型验证
- 支持多种文件格式
- 可配置文件大小限制
- 防止恶意文件上传

---

## 📊 错误码说明

| HTTP状态码 | 说明 | 解决方案 |
|------------|------|----------|
| 200 | 请求成功 | - |
| 400 | 请求参数错误 | 检查参数格式 |
| 403 | 访问被拒绝（防盗链） | 检查Referer头 |
| 404 | 文件不存在 | 检查文件路径 |
| 413 | 文件过大 | 检查文件大小 |
| 415 | 不支持的文件类型 | 检查文件格式 |
| 500 | 服务器内部错误 | 检查服务器日志 |

---

## 🎯 使用示例

### JavaScript前端调用示例
```javascript
// 文件上传
async function uploadFile(file, path = '') {
  const formData = new FormData();
  formData.append('file', file);
  if (path) {
    formData.append('path', path);
  }
  
  const response = await fetch('/api/s3/upload', {
    method: 'POST',
    body: formData
  });
  
  return await response.json();
}

// 获取预签名URL
async function getPresignedUrl(objectKey, expiration = 3600) {
  const response = await fetch(
    `/api/s3/presigned-url/${objectKey}?expiration=${expiration}`
  );
  return await response.text();
}

// 删除文件
async function deleteFile(objectKey) {
  const response = await fetch(`/api/s3/${objectKey}`, {
    method: 'DELETE'
  });
  return await response.json();
}
```

---

## 📝 注意事项

1. **配置要求**: 必须正确配置AWS S3的访问密钥和存储桶信息
2. **环境变量**: 生产环境建议使用环境变量而非硬编码配置
3. **防盗链**: 默认启用防盗链保护，需要配置允许的域名
4. **文件大小**: 默认限制100MB，可在Spring配置中调整
5. **错误处理**: 完善的异常处理和日志记录

---

## 🔗 相关链接

- [AWS S3官方文档](https://docs.aws.amazon.com/s3/)
- [Spring Boot文件上传配置](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.web.spring-mvc.file-upload)
- [项目GitHub地址](https://github.com/your-repo/collide-all)

---

**文档版本**: 1.0.0  
**最后更新**: 2024-01-15  
**维护团队**: Collide开发团队
