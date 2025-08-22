-- ========================================
-- CommentController接口测试数据脚本
-- ========================================

-- 清空相关表数据（可选）
-- DELETE FROM t_comment;
-- DELETE FROM t_inform;
-- DELETE FROM t_like;
-- DELETE FROM t_content;
-- DELETE FROM t_user;

-- ========================================
-- 1. 插入测试用户数据
-- ========================================
INSERT INTO t_user (id, username, nickname, avatar, email, phone, password_hash, role, status, bio, gender, location, follower_count, following_count, content_count, like_count, is_vip, create_time, update_time) VALUES
(101, 'user001', '测试用户1', 'https://example.com/avatar1.jpg', 'user001@test.com', '13800138001', 'password_hash_1', 'user', 'active', '这是测试用户1的个人简介', 'male', '北京', 10, 5, 3, 25, 'N', NOW(), NOW()),
(102, 'user002', '测试用户2', 'https://example.com/avatar2.jpg', 'user002@test.com', '13800138002', 'password_hash_2', 'user', 'active', '这是测试用户2的个人简介', 'female', '上海', 15, 8, 5, 40, 'Y', NOW(), NOW()),
(103, 'user003', '测试用户3', 'https://example.com/avatar3.jpg', 'user003@test.com', '13800138003', 'password_hash_3', 'user', 'active', '这是测试用户3的个人简介', 'male', '广州', 8, 12, 2, 15, 'N', NOW(), NOW()),
(104, 'author001', '作者用户1', 'https://example.com/author1.jpg', 'author001@test.com', '13800138004', 'password_hash_4', 'blogger', 'active', '这是作者用户1的个人简介', 'female', '深圳', 50, 20, 15, 200, 'Y', NOW(), NOW()),
(105, 'author002', '作者用户2', 'https://example.com/author2.jpg', 'author002@test.com', '13800138005', 'password_hash_5', 'blogger', 'active', '这是作者用户2的个人简介', 'male', '杭州', 30, 15, 8, 120, 'N', NOW(), NOW());

-- ========================================
-- 2. 插入测试内容数据
-- ========================================
INSERT INTO t_content (id, title, description, content_type, content_data, cover_url, tags, author_id, author_nickname, author_avatar, category_id, category_name, status, review_status, view_count, like_count, comment_count, favorite_count, score_count, score_total, publish_time, create_time, update_time) VALUES
(101, '测试小说标题1', '这是一部测试小说的描述', 'NOVEL', '{"chapters": [{"title": "第一章", "content": "这是第一章的内容..."}]}', 'https://example.com/cover1.jpg', '["小说", "测试", "文学"]', 104, '作者用户1', 'https://example.com/author1.jpg', 1, '小说分类', 'PUBLISHED', 'APPROVED', 1000, 50, 10, 20, 5, 25, NOW(), NOW(), NOW()),
(102, '测试漫画标题1', '这是一部测试漫画的描述', 'COMIC', '{"pages": [{"page": 1, "image": "https://example.com/comic1.jpg"}]}', 'https://example.com/cover2.jpg', '["漫画", "测试", "艺术"]', 104, '作者用户1', 'https://example.com/author1.jpg', 2, '漫画分类', 'PUBLISHED', 'APPROVED', 800, 30, 8, 15, 3, 15, NOW(), NOW(), NOW()),
(103, '测试视频标题1', '这是一个测试视频的描述', 'VIDEO', '{"video_url": "https://example.com/video1.mp4", "duration": 300}', 'https://example.com/cover3.jpg', '["视频", "测试", "娱乐"]', 105, '作者用户2', 'https://example.com/author2.jpg', 3, '视频分类', 'PUBLISHED', 'APPROVED', 500, 25, 5, 10, 2, 10, NOW(), NOW(), NOW()),
(104, '测试文章标题1', '这是一篇测试文章的描述', 'ARTICLE', '{"content": "这是文章的详细内容..."}', 'https://example.com/cover4.jpg', '["文章", "测试", "知识"]', 105, '作者用户2', 'https://example.com/author2.jpg', 4, '文章分类', 'PUBLISHED', 'APPROVED', 300, 15, 3, 5, 1, 5, NOW(), NOW(), NOW()),
(105, '测试音频标题1', '这是一个测试音频的描述', 'AUDIO', '{"audio_url": "https://example.com/audio1.mp3", "duration": 180}', 'https://example.com/cover5.jpg', '["音频", "测试", "音乐"]', 104, '作者用户1', 'https://example.com/author1.jpg', 5, '音频分类', 'PUBLISHED', 'APPROVED', 200, 10, 2, 3, 1, 4, NOW(), NOW(), NOW());

-- ========================================
-- 3. 插入测试评论数据
-- ========================================
INSERT INTO t_comment (id, comment_type, target_id, parent_comment_id, content, user_id, user_nickname, user_avatar, reply_to_user_id, reply_to_user_nickname, reply_to_user_avatar, status, like_count, reply_count, create_time, update_time) VALUES
-- 根评论
(101, 'CONTENT', 101, 0, '这是一条测试根评论，内容很精彩！', 101, '测试用户1', 'https://example.com/avatar1.jpg', NULL, NULL, NULL, 'NORMAL', 5, 2, NOW(), NOW()),
(102, 'CONTENT', 101, 0, '我也觉得这个内容不错，支持一下！', 102, '测试用户2', 'https://example.com/avatar2.jpg', NULL, NULL, NULL, 'NORMAL', 3, 1, NOW(), NOW()),
(103, 'CONTENT', 102, 0, '漫画画风很棒，期待更新！', 103, '测试用户3', 'https://example.com/avatar3.jpg', NULL, NULL, NULL, 'NORMAL', 2, 0, NOW(), NOW()),
(104, 'CONTENT', 103, 0, '视频制作很用心，学习了！', 101, '测试用户1', 'https://example.com/avatar1.jpg', NULL, NULL, NULL, 'NORMAL', 4, 1, NOW(), NOW()),
(105, 'CONTENT', 104, 0, '文章写得很好，受益匪浅！', 102, '测试用户2', 'https://example.com/avatar2.jpg', NULL, NULL, NULL, 'NORMAL', 1, 0, NOW(), NOW()),

-- 回复评论
(106, 'CONTENT', 101, 101, '同意你的观点，确实很棒！', 102, '测试用户2', 'https://example.com/avatar2.jpg', 101, '测试用户1', 'https://example.com/avatar1.jpg', 'NORMAL', 2, 0, NOW(), NOW()),
(107, 'CONTENT', 101, 101, '我也来支持一下！', 103, '测试用户3', 'https://example.com/avatar3.jpg', 101, '测试用户1', 'https://example.com/avatar1.jpg', 'NORMAL', 1, 0, NOW(), NOW()),
(108, 'CONTENT', 101, 102, '感谢支持，我会继续努力的！', 104, '作者用户1', 'https://example.com/author1.jpg', 102, '测试用户2', 'https://example.com/avatar2.jpg', 'NORMAL', 3, 0, NOW(), NOW()),
(109, 'CONTENT', 103, 104, '谢谢夸奖，我会继续制作更好的内容！', 105, '作者用户2', 'https://example.com/author2.jpg', 101, '测试用户1', 'https://example.com/avatar1.jpg', 'NORMAL', 2, 0, NOW(), NOW()),

-- 动态评论
(110, 'DYNAMIC', 101, 0, '这是一条动态评论！', 101, '测试用户1', 'https://example.com/avatar1.jpg', NULL, NULL, NULL, 'NORMAL', 1, 0, NOW(), NOW()),
(111, 'DYNAMIC', 101, 110, '回复动态评论！', 102, '测试用户2', 'https://example.com/avatar2.jpg', 101, '测试用户1', 'https://example.com/avatar1.jpg', 'NORMAL', 0, 0, NOW(), NOW());

-- ========================================
-- 4. 插入测试点赞数据
-- ========================================
INSERT INTO t_like (id, like_type, target_id, user_id, target_title, target_author_id, user_nickname, user_avatar, status, create_time, update_time) VALUES
-- 内容点赞
(101, 'CONTENT', 101, 101, '测试小说标题1', 104, '测试用户1', 'https://example.com/avatar1.jpg', 'active', NOW(), NOW()),
(102, 'CONTENT', 101, 102, '测试小说标题1', 104, '测试用户2', 'https://example.com/avatar2.jpg', 'active', NOW(), NOW()),
(103, 'CONTENT', 102, 101, '测试漫画标题1', 104, '测试用户1', 'https://example.com/avatar1.jpg', 'active', NOW(), NOW()),
(104, 'CONTENT', 103, 102, '测试视频标题1', 105, '测试用户2', 'https://example.com/avatar2.jpg', 'active', NOW(), NOW()),

-- 评论点赞
(105, 'COMMENT', 101, 102, '这是一条测试根评论，内容很精彩！', 101, '测试用户2', 'https://example.com/avatar2.jpg', 'active', NOW(), NOW()),
(106, 'COMMENT', 101, 103, '这是一条测试根评论，内容很精彩！', 101, '测试用户3', 'https://example.com/avatar3.jpg', 'active', NOW(), NOW()),
(107, 'COMMENT', 102, 101, '我也觉得这个内容不错，支持一下！', 102, '测试用户1', 'https://example.com/avatar1.jpg', 'active', NOW(), NOW()),
(108, 'COMMENT', 106, 101, '同意你的观点，确实很棒！', 102, '测试用户1', 'https://example.com/avatar1.jpg', 'active', NOW(), NOW());

-- ========================================
-- 5. 插入测试通知数据
-- ========================================
INSERT INTO t_inform (id, app_name, type_relation, user_type, notification_content, send_time, is_deleted, is_sent, create_time, update_time) VALUES
(101, 'collide-app', 'LIKE', 'user', '测试用户2点赞了你的评论"这是一条测试根评论，内容很精彩！"', NOW(), 'N', 'Y', NOW(), NOW()),
(102, 'collide-app', 'LIKE', 'user', '测试用户3点赞了你的评论"这是一条测试根评论，内容很精彩！"', NOW(), 'N', 'Y', NOW(), NOW()),
(103, 'collide-app', 'COMMENT', 'user', '测试用户2评论了你的内容"测试小说标题1"', NOW(), 'N', 'Y', NOW(), NOW()),
(104, 'collide-app', 'REPLY', 'user', '测试用户2回复了你的评论"这是一条测试根评论，内容很精彩！"', NOW(), 'N', 'Y', NOW(), NOW());

-- ========================================
-- 6. 更新评论的回复数
-- ========================================
UPDATE t_comment SET reply_count = 2 WHERE id = 101;
UPDATE t_comment SET reply_count = 1 WHERE id = 102;
UPDATE t_comment SET reply_count = 1 WHERE id = 104;
UPDATE t_comment SET reply_count = 1 WHERE id = 110;

-- ========================================
-- 7. 更新内容的评论数
-- ========================================
UPDATE t_content SET comment_count = 9 WHERE id = 101;
UPDATE t_content SET comment_count = 1 WHERE id = 102;
UPDATE t_content SET comment_count = 2 WHERE id = 103;
UPDATE t_content SET comment_count = 1 WHERE id = 104;
UPDATE t_content SET comment_count = 0 WHERE id = 105;

-- ========================================
-- 测试数据插入完成
-- ========================================
SELECT '测试数据插入完成！' AS message;

-- 查看插入的数据
SELECT '用户数据：' AS info;
SELECT id, username, nickname, role, status FROM t_user;

SELECT '内容数据：' AS info;
SELECT id, title, content_type, author_nickname, comment_count FROM t_content;

SELECT '评论数据：' AS info;
SELECT id, comment_type, target_id, parent_comment_id, user_nickname, content, like_count, reply_count FROM t_comment;

SELECT '点赞数据：' AS info;
SELECT id, like_type, target_id, user_nickname, status FROM t_like;

SELECT '通知数据：' AS info;
SELECT id, type_relation, notification_content, is_sent FROM t_inform;
