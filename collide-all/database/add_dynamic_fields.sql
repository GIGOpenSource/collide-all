-- 为t_social_dynamic表添加缺失字段
-- 执行时间：2024-01-01
-- 说明：添加发布标题、是否免费、价格字段

USE `collide`;

-- 添加发布标题字段
ALTER TABLE `t_social_dynamic` 
ADD COLUMN `title` varchar(200) NULL COMMENT '发布标题' AFTER `content`;

-- 添加是否免费字段
ALTER TABLE `t_social_dynamic` 
ADD COLUMN `is_free` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否免费：0-付费，1-免费' AFTER `video_url`;

-- 添加价格字段
ALTER TABLE `t_social_dynamic` 
ADD COLUMN `price` decimal(10,2) NULL DEFAULT NULL COMMENT '价格（付费时必填）' AFTER `is_free`;

-- 添加索引优化查询
ALTER TABLE `t_social_dynamic` 
ADD INDEX `idx_title` (`title`),
ADD INDEX `idx_is_free` (`is_free`),
ADD INDEX `idx_price` (`price`);

-- 验证修改结果
DESCRIBE `t_social_dynamic`;
