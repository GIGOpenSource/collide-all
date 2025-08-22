@echo off
echo ========================================
echo Collide-All 认证流程演示启动脚本
echo ========================================
echo.

echo 1. 检查Java环境...
java -version
if %errorlevel% neq 0 (
    echo 错误: 未找到Java环境，请安装JDK 21+
    pause
    exit /b 1
)

echo.
echo 2. 检查Maven环境...
mvn -version
if %errorlevel% neq 0 (
    echo 错误: 未找到Maven环境，请安装Maven 3.9+
    pause
    exit /b 1
)

echo.
echo 3. 清理并编译项目...
mvn clean compile
if %errorlevel% neq 0 (
    echo 错误: 项目编译失败
    pause
    exit /b 1
)

echo.
echo 4. 启动Collide-All服务...
echo 服务启动中，请稍候...
echo 启动完成后，可以访问: http://localhost:8080/collide-all/doc.html
echo.
echo 按 Ctrl+C 停止服务
echo.

mvn spring-boot:run

pause
