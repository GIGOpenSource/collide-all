# 简化后的点赞逻辑测试

## 修改说明

按照用户要求，简化了点赞逻辑：

1. **增加点赞**：向 `t_like` 表插入数据，如果已存在则返回错误
2. **取消点赞**：从 `t_like` 表删除数据，如果不存在则返回错误

## 修改内容

### 1. SocialDynamicServiceImpl.increaseLikeCount

```java
@Override
@Transactional
public int increaseLikeCount(Long dynamicId, Long operatorId) {
    log.info("增加点赞数: 动态ID={}, 操作者ID={}", dynamicId, operatorId);
    
    // 检查是否已经点赞过
    boolean alreadyLiked = likeService.checkLikeStatus(operatorId, "DYNAMIC", dynamicId);
    if (alreadyLiked) {
        log.warn("用户已点赞该动态，忽略重复点赞: 动态ID={}, 操作者ID={}", dynamicId, operatorId);
        return 0; // 返回0表示没有进行任何操作
    }
    
    // 创建点赞记录
    Like like = new Like();
    like.setUserId(operatorId);
    like.setLikeType("DYNAMIC");
    like.setTargetId(dynamicId);
    like.setStatus("active");
    
    // 获取动态信息用于冗余字段
    SocialDynamic dynamic = socialDynamicMapper.selectById(dynamicId);
    if (dynamic != null) {
        like.setTargetTitle(dynamic.getTitle());
        like.setTargetAuthorId(dynamic.getUserId());
    }
    
    // 直接插入点赞记录（如果已存在会抛出异常）
    int insertResult = likeMapper.insert(like);
    if (insertResult <= 0) {
        log.error("插入点赞记录失败: 动态ID={}, 操作者ID={}", dynamicId, operatorId);
        throw new RuntimeException("插入点赞记录失败");
    }
    
    // 增加动态表中的点赞数
    int result = socialDynamicMapper.increaseLikeCount(dynamicId);
    if (result > 0) {
        log.info("点赞数增加成功: 动态ID={}, 操作者ID={}, 点赞记录ID={}", dynamicId, operatorId, like.getId());
    } else {
        log.error("增加动态点赞数失败: 动态ID={}, 操作者ID={}", dynamicId, operatorId);
        throw new RuntimeException("增加动态点赞数失败");
    }
    
    return result;
}
```

### 2. SocialDynamicServiceImpl.decreaseLikeCount

```java
@Override
@Transactional
public int decreaseLikeCount(Long dynamicId, Long operatorId) {
    log.info("减少点赞数: 动态ID={}, 操作者ID={}", dynamicId, operatorId);
    
    // 检查是否已经点赞过
    boolean alreadyLiked = likeService.checkLikeStatus(operatorId, "DYNAMIC", dynamicId);
    if (!alreadyLiked) {
        log.warn("用户未点赞该动态，忽略取消点赞: 动态ID={}, 操作者ID={}", dynamicId, operatorId);
        return 0; // 返回0表示没有进行任何操作
    }
    
    // 直接删除点赞记录
    int deleteResult = likeMapper.deleteByUserAndTarget(operatorId, "DYNAMIC", dynamicId);
    if (deleteResult <= 0) {
        log.error("删除点赞记录失败: 动态ID={}, 操作者ID={}", dynamicId, operatorId);
        throw new RuntimeException("删除点赞记录失败");
    }
    
    // 减少动态表中的点赞数
    int result = socialDynamicMapper.decreaseLikeCount(dynamicId);
    if (result > 0) {
        log.info("点赞数减少成功: 动态ID={}, 操作者ID={}", dynamicId, operatorId);
    } else {
        log.error("减少动态点赞数失败: 动态ID={}, 操作者ID={}", dynamicId, operatorId);
        throw new RuntimeException("减少动态点赞数失败");
    }
    
    return result;
}
```

### 3. LikeMapper 新增方法

```java
/**
 * 删除用户对目标对象的点赞记录
 * 
 * @param userId 用户ID
 * @param likeType 点赞类型
 * @param targetId 目标对象ID
 * @return 删除行数
 */
int deleteByUserAndTarget(@Param("userId") Long userId,
                         @Param("likeType") String likeType,
                         @Param("targetId") Long targetId);
```

### 4. LikeMapper.xml 新增 SQL

```xml
<!-- 删除用户对目标对象的点赞记录 -->
<!-- 索引使用: uk_user_target(user_id, like_type, target_id) - 唯一索引最优性能 -->
<delete id="deleteByUserAndTarget">
    DELETE FROM t_like
    WHERE user_id = #{userId}
      AND like_type = #{likeType}
      AND target_id = #{targetId}
</delete>
```

## 测试步骤

### 1. 第一次点赞

```bash
curl -X POST "http://localhost:8080/api/v1/social/dynamics/like/increase" \
  -H "Content-Type: application/json" \
  -d '{
    "id": 1,
    "userId": 123
  }'
```

**预期结果**：
- 返回成功，动态点赞数+1
- `t_like` 表中新增一条记录
- 日志显示："点赞数增加成功"

### 2. 第二次点赞（重复点赞）

```bash
curl -X POST "http://localhost:8080/api/v1/social/dynamics/like/increase" \
  -H "Content-Type: application/json" \
  -d '{
    "id": 1,
    "userId": 123
  }'
```

**预期结果**：
- 返回0，表示没有进行操作
- `t_like` 表中没有新增记录
- 日志显示："用户已点赞该动态，忽略重复点赞"

### 3. 取消点赞

```bash
curl -X POST "http://localhost:8080/api/v1/social/dynamics/like/decrease" \
  -H "Content-Type: application/json" \
  -d '{
    "id": 1,
    "userId": 123
  }'
```

**预期结果**：
- 返回成功，动态点赞数-1
- `t_like` 表中删除对应记录
- 日志显示："点赞数减少成功"

### 4. 再次取消点赞（重复取消）

```bash
curl -X POST "http://localhost:8080/api/v1/social/dynamics/like/decrease" \
  -H "Content-Type: application/json" \
  -d '{
    "id": 1,
    "userId": 123
  }'
```

**预期结果**：
- 返回0，表示没有进行操作
- `t_like` 表中没有变化
- 日志显示："用户未点赞该动态，忽略取消点赞"

## 数据库验证

### 检查点赞记录

```sql
-- 查看所有点赞记录
SELECT * FROM t_like WHERE like_type = 'DYNAMIC' ORDER BY create_time DESC;

-- 查看特定用户的点赞记录
SELECT * FROM t_like WHERE user_id = 123 AND like_type = 'DYNAMIC';

-- 查看特定动态的点赞记录
SELECT * FROM t_like WHERE target_id = 1 AND like_type = 'DYNAMIC';
```

### 检查动态点赞数

```sql
-- 查看动态表的点赞数
SELECT id, title, like_count FROM t_social_dynamic WHERE id = 1;

-- 对比实际点赞记录数和动态表中的点赞数
SELECT 
    d.id as dynamic_id,
    d.title,
    d.like_count as dynamic_like_count,
    COUNT(l.id) as actual_like_count
FROM t_social_dynamic d
LEFT JOIN t_like l ON d.id = l.target_id AND l.like_type = 'DYNAMIC'
WHERE d.id = 1
GROUP BY d.id, d.title, d.like_count;
```

## 关键改进

1. **简化逻辑**：不再使用复杂的 `LikeService.addLike` 方法，直接操作数据库
2. **明确语义**：增加点赞 = 插入记录，取消点赞 = 删除记录
3. **数据库约束**：依赖 `uk_user_target` 唯一索引防止重复插入
4. **事务安全**：所有操作都在事务中进行，确保数据一致性

## 预期效果

- 防止重复点赞：通过唯一索引和业务逻辑双重保证
- 数据一致性：动态表的点赞数与 `t_like` 表的记录数保持一致
- 性能优化：直接操作数据库，减少不必要的服务层调用
