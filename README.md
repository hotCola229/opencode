# Vibe Coding Backend Service

Spring Boot 2.7.x 多模块项目

## 技术栈

- Java 8
- Spring Boot 2.7.18
- Maven 多模块

## 构建与测试

```bash
# 编译项目
mvn clean compile

# 运行测试
mvn test
```

**期望结果**: 测试全部通过，无错误

## 启动应用

```bash
# 方式一：使用 Maven 插件启动
mvn -pl backend spring-boot:run

# 方式二：打包后启动
mvn -pl backend package
java -jar backend/target/backend-1.0.0.jar
```

**期望结果**: 应用启动日志显示端口 8080

## 验证 Health

```bash
# 验证 Actuator Health
curl http://localhost:8080/actuator/health

# 期望结果: {"status":"UP"}

# 验证自定义 Health 端点
curl http://localhost:8080/health

# 期望结果: {"status":"UP"}
```

## 项目结构

```
.
├── pom.xml
├── README.md
├── TASKS.md
└── backend/
    ├── pom.xml
    └── src/
        ├── main/
        │   ├── java/com/example/backend/
        │   │   ├── BackendApplication.java
        │   │   └── controller/HealthController.java
        │   └── resources/application.yml
        └── test/
            └── java/com/example/backend/controller/HealthControllerTest.java
```
