# 使用OpenJDK 21作为基础镜像
FROM openjdk:21-jdk-slim

# 设置工作目录
WORKDIR /app

# 设置环境变量
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC"
ENV SPRING_PROFILES_ACTIVE=prod
ENV TZ=Asia/Shanghai

# 安装必要的工具
RUN apt-get update && apt-get install -y \
    curl \
    wget \
    && rm -rf /var/lib/apt/lists/*

# 复制JAR文件
COPY target/collide-all-*.jar app.jar

# 创建日志目录
RUN mkdir -p /app/logs

# 创建配置文件目录
RUN mkdir -p /app/config

# 复制配置文件（可选）
COPY src/main/resources/application-prod.yml /app/config/

# 暴露端口
EXPOSE 8080

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/collide-all/actuator/health || exit 1

# 启动命令
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar --spring.config.location=classpath:/,file:/app/config/"]
