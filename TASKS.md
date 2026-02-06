# Spring Boot 项目初始化任务清单

## 环境检查
- [ ] 确认 Maven 版本 (需 3.6+)
- [ ] 确认 Java 版本 (需 Java 8)

## 项目结构创建
- [ ] 创建根目录 pom.xml
- [ ] 创建 backend 模块目录结构
- [ ] 创建 backend/pom.xml
- [ ] 创建 BackendApplication.java
- [ ] 创建 HealthController.java
- [ ] 创建 application.yml
- [ ] 创建 HealthControllerTest.java
- [ ] 创建 README.md

## 构建与验证
- [ ] 执行 `mvn clean compile` 编译项目
- [ ] 执行 `mvn test` 运行测试
- [ ] 启动应用验证 Actuator health
- [ ] 启动应用验证自定义 /health 端点

## 执行命令

```bash
# 创建目录
mkdir -p backend/src/main/java/com/example/backend/controller
mkdir -p backend/src/main/resources
mkdir -p backend/src/test/java/com/example/backend/controller

# 编译
mvn clean compile

# 测试
mvn test

# 启动
mvn -pl backend spring-boot:run

# 验证
curl http://localhost:8080/actuator/health
curl http://localhost:8080/health
```
