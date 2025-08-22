-- 点赞状态检查修复验证脚本
-- 用于验证修复后的点赞状态检查逻辑是否正确

-- =================== 测试数据准备 ===================

-- 1. 查看现有的点赞记录
SELECT 
    id,
    user_id,
    like_type,
    target_id,
    status,
    create_time,
    update_time
FROM t_like 
WHERE like_type = 'DYNAMIC'
ORDER BY create_time DESC
LIMIT 10;

-- 2. 查看特定用户的点赞记录
SELECT 
    id,
    user_id,
    like_type,
    target_id,
    status,
    create_time,
    update_time
FROM t_like 
WHERE user_id = 3 AND like_type = 'DYNAMIC'
ORDER BY create_time DESC;

-- =================== 验证修复效果 ===================

-- 3. 测试修复后的findByUserAndTarget逻辑
-- 这个查询现在应该只返回active状态的记录
SELECT 
    id,
    user_id,
    like_type,
    target_id,
    status,
    create_time,
    update_time
FROM t_like
WHERE user_id = 3 
  AND like_type = 'DYNAMIC' 
  AND target_id = 1
  AND status = 'active'
ORDER BY create_time DESC
LIMIT 1;

-- 4. 对比修复前的查询（包含所有状态）
SELECT 
    id,
    user_id,
    like_type,
    target_id,
    status,
    create_time,
    update_time
FROM t_like
WHERE user_id = 3 
  AND like_type = 'DYNAMIC' 
  AND target_id = 1
ORDER BY create_time DESC
LIMIT 1;

-- =================== 批量检查点赞状态 ===================

-- 5. 检查所有动态的点赞状态（针对用户3）
SELECT 
    d.id as dynamic_id,
    d.title as dynamic_title,
    d.like_count as stored_like_count,
    CASE 
        WHEN l.id IS NOT NULL THEN '已点赞'
        ELSE '未点赞'
    END as like_status,
    l.id as like_id,
    l.status as like_record_status
FROM t_social_dynamic d
LEFT JOIN t_like l ON d.id = l.target_id 
    AND l.user_id = 3 
    AND l.like_type = 'DYNAMIC'
    AND l.status = 'active'
WHERE d.status != 'deleted'
ORDER BY d.create_time DESC
LIMIT 10;

-- =================== 数据一致性检查 ===================

-- 6. 检查点赞记录与动态表like_count的一致性
SELECT 
    d.id as dynamic_id,
    d.title as dynamic_title,
    d.like_count as stored_like_count,
    COALESCE(COUNT(l.id), 0) as actual_like_count,
    (d.like_count - COALESCE(COUNT(l.id), 0)) as difference
FROM t_social_dynamic d
LEFT JOIN t_like l ON d.id = l.target_id 
    AND l.like_type = 'DYNAMIC' 
    AND l.status = 'active'
WHERE d.status != 'deleted'
GROUP BY d.id, d.title, d.like_count
HAVING d.like_count != COALESCE(COUNT(l.id), 0)
ORDER BY ABS(d.like_count - COALESCE(COUNT(l.id), 0)) DESC
LIMIT 10;

-- =================== 修复建议 ===================

-- 7. 如果发现数据不一致，可以执行以下修复语句
-- 注意：执行前请先备份数据库

/*
-- 修复动态表的like_count字段
UPDATE t_social_dynamic d
SET d.like_count = (
    SELECT COALESCE(COUNT(l.id), 0)
    FROM t_like l
    WHERE l.target_id = d.id 
      AND l.like_type = 'DYNAMIC' 
      AND l.status = 'active'
)
WHERE d.status != 'deleted';
*/
