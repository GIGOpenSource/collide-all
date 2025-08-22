@echo off
echo ============================================================
echo CommentController接口测试 (使用curl)
echo ============================================================
echo 测试时间: %date% %time%
echo 测试地址: http://localhost:8080/collide-all
echo.

echo 【测试1】查询评论列表
curl -X GET "http://localhost:8080/collide-all/api/v1/comments/list" -H "Content-Type: application/json" -H "Accept: application/json"
echo.
echo.

echo 【测试2】按内容ID查询评论
curl -X GET "http://localhost:8080/collide-all/api/v1/comments/list?targetId=101&commentType=CONTENT&currentPage=1&pageSize=5" -H "Content-Type: application/json" -H "Accept: application/json"
echo.
echo.

echo 【测试3】创建根评论
curl -X POST "http://localhost:8080/collide-all/api/v1/comments/create" -H "Content-Type: application/json" -H "Accept: application/json" -d "{\"commentType\":\"CONTENT\",\"targetId\":101,\"parentCommentId\":0,\"content\":\"这是一条curl测试评论！\",\"userId\":101,\"userNickname\":\"测试用户1\",\"userAvatar\":\"https://example.com/avatar1.jpg\",\"status\":\"NORMAL\"}"
echo.
echo.

echo 【测试4】错误情况测试 - 缺少用户ID
curl -X POST "http://localhost:8080/collide-all/api/v1/comments/create" -H "Content-Type: application/json" -H "Accept: application/json" -d "{\"commentType\":\"CONTENT\",\"targetId\":101,\"content\":\"缺少用户ID的评论\"}"
echo.
echo.

echo ============================================================
echo 测试完成
echo ============================================================
pause
