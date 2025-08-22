# 点赞逻辑修复验证测试

## 修复内容

1. **移除硬编码测试用户ID**：
   - 修复了 `SocialDynamicController.queryDynamics()` 中的硬编码 `currentUserId = 123L`
   - 修复了 `ContentServiceImpl` 中的硬编码 `currentUserId = 123L`

2. **正确获取当前用户ID**：
   - 优先从请求参数中获取 `currentUserId`
   - 其次从token中获取当前用户ID
   - 如果没有则设为 `null`（表示未登录状态）

3. **为查询接口添加当前用户ID参数**：
   - 为 `selectByDynamicType`、`selectByStatus` 等接口添加了 `currentUserId` 参数
   - 确保所有查询接口都能正确获取互动状态

## 测试步骤

### 1. 测试点赞功能
```bash
# 1. 用户3对动态2进行点赞
POST /api/v1/social/dynamics/like/increase
Content-Type: application/json

{
  "id": 2,
  "userId": 3
}

# 预期响应：{"code": 200, "message": "success", "data": 1}
```

### 2. 测试查询动态列表（包含互动状态）
```bash
# 2. 查询动态列表，传递当前用户ID=3
POST /api/v1/social/dynamics/query
Content-Type: application/json

{
  "pageNumber": 1,
  "pageSize": 20,
  "currentUserId": 3
}

# 预期响应：动态2的 isLiked 字段应该为 true
```

### 3. 测试重启后状态保持
```bash
# 3. 重启系统后，再次查询动态列表
POST /api/v1/social/dynamics/query
Content-Type: application/json

{
  "pageNumber": 1,
  "pageSize": 20,
  "currentUserId": 3
}

# 预期响应：动态2的 isLiked 字段应该仍然为 true
```

### 4. 测试不同用户的状态
```bash
# 4. 使用用户4查询动态列表
POST /api/v1/social/dynamics/query
Content-Type: application/json

{
  "pageNumber": 1,
  "pageSize": 20,
  "currentUserId": 4
}

# 预期响应：动态2的 isLiked 字段应该为 false（因为用户4没有点赞）
```

### 5. 测试未登录状态
```bash
# 5. 不传递 currentUserId 查询动态列表
POST /api/v1/social/dynamics/query
Content-Type: application/json

{
  "pageNumber": 1,
  "pageSize": 20
}

# 预期响应：所有动态的 isLiked 字段应该为 false
```

## 验证要点

1. **点赞记录正确保存**：检查数据库中 `t_like` 表是否有正确的记录
2. **状态查询正确**：确保使用正确的用户ID查询点赞状态
3. **重启后状态保持**：重启系统后，点赞状态应该正确显示
4. **不同用户隔离**：不同用户看到的点赞状态应该不同
5. **未登录处理**：未登录用户看到的点赞状态应该为 false

## 数据库验证

```sql
-- 检查点赞记录
SELECT * FROM t_like WHERE user_id = 3 AND target_id = 2 AND like_type = 'DYNAMIC';

-- 检查动态记录
SELECT * FROM t_social_dynamic WHERE id = 2;
```

## 预期结果

修复后，点赞功能应该：
- ✅ 正确保存点赞记录到数据库
- ✅ 正确显示当前用户的点赞状态
- ✅ 重启系统后状态保持正确
- ✅ 不同用户看到不同的点赞状态
- ✅ 未登录用户看到所有状态为 false
