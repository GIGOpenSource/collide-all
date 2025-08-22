# Collide-All 项目

## 项目简介
这是一个基于Spring Boot + MyBatis Plus + Redis + MySQL的Java Web项目。

## 环境要求
- JDK 21+
- Maven 3.9+
- MySQL 8.0+
- Redis 6.0+

## 快速启动

### 1. 数据库准备
```sql
-- 执行 database/init.sql 脚本创建数据库和表
mysql -u root -p < database/init.sql
```

### 2. 启动Redis
```bash
# Windows
redis-server

# Linux/Mac
redis-server /etc/redis/redis.conf
```

### 3. 修改配置
根据您的环境修改 `src/main/resources/application.yml` 中的数据库和Redis配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/collide?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=false&serverTimezone=GMT%2B8&allowPublicKeyRetrieval=true
    username: root
    password: 123456

  data:
    redis:
      host: localhost
      port: 6379
      password: 
```

### 4. 编译运行
```bash
# 编译项目
mvn clean compile

# 运行项目
mvn spring-boot:run

# 或者打包后运行
mvn clean package
java -jar target/collide-all-1.0.0-SNAPSHOT.jar
```

### 5. 访问应用
- 应用地址: http://localhost:8080/collide-all
- API文档: http://localhost:8080/collide-all/doc.html
- Druid监控: http://localhost:8080/collide-all/druid

## 项目结构
```
src/main/java/com/gig/collide/
├── Apientry/           # API接口层
│   └── api/           # 请求响应对象
├── cache/             # 缓存层
├── config/            # 配置类
├── controller/        # 控制器层
├── converter/         # 转换器
├── domain/           # 领域对象
├── entity/           # 实体类
├── mapper/           # MyBatis映射器
├── service/          # 服务层
└── CollideAllApplication.java  # 启动类
```

## 配置文件说明
- `application.yml`: 主配置文件
- `application-dev.yml`: 开发环境配置
- `application-prod.yml`: 生产环境配置
- `bootstrap.yml`: 启动配置

## 技术栈
- **框架**: Spring Boot 3.x
- **ORM**: MyBatis Plus
- **数据库**: MySQL 8.0
- **缓存**: Redis
- **连接池**: Druid
- **文档**: Knife4j (Swagger)
- **认证**: Sa-Token
- **缓存框架**: JetCache

## 常见问题

### 1. 编译错误
如果遇到编译错误，请检查：
- JDK版本是否为21+
- Maven版本是否为3.9+
- 依赖是否下载完整

### 2. 数据库连接失败
- 检查MySQL服务是否启动
- 检查数据库连接配置是否正确
- 检查数据库用户权限

### 3. Redis连接失败
- 检查Redis服务是否启动
- 检查Redis连接配置是否正确

### 4. 端口占用
如果8080端口被占用，可以修改 `application.yml` 中的端口配置：
```yaml
server:
  port: 8081
```
