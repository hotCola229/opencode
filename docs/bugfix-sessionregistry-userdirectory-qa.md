# UserProfileService ThreadLocal 内存泄漏问题处理

## 问题是什么 / 影响范围

### 问题描述
`UserProfileService` 内部使用的 `RequestContext` 类存在 ThreadLocal 内存泄漏缺陷。`reset()` 方法仅调用 `ctx.clear()` 清空 Map 内容，但没有调用 `ThreadLocal.remove()` 彻底清理 ThreadLocal 本身。

### 影响范围
1. **内存泄漏**：每次调用 `bind()` 会创建 2MB 的 `byte[]` 数组（用于 payload），若 `reset()` 未被正确调用（如异常场景），这些大对象会在线程池场景下持续累积
2. **资源浪费**：ThreadLocal 中的空 Map 对象无法被 GC 回收
3. **潜在问题**：在高并发场景下，ThreadLocal 泄漏可能导致内存溢出（OOM）

### 涉及类
- `com.example.backend.service.RequestContext`
- `com.example.backend.service.UserProfileService`

---

## 如何复现

### 复现命令
```bash
cd /Users/zhangguohui/workspace/vibe-coding/test/opencode/backend
mvn test -Dtest=UserProfileServiceMemoryLeakTest
```

### 复现现象（修复前）
```
[ERROR] Tests run: 3, Failures: 2, Errors: 0, Skipped: 0
[ERROR]   UserProfileServiceMemoryLeakTest.testThreadLocalCleanupAfterReset:29 
    reset() 后 ThreadLocal 应该为空 ==> expected: <null> but was: <{}>
[ERROR]   UserProfileServiceMemoryLeakTest.testMultipleCallsMemoryAccumulation:72 
    多次调用后 ThreadLocal 应该为空 ==> expected: <null> but was: <{}>
```

### 修复后
```
Tests run: 3, Failures: 0, Errors: 0, Skipped: 0 - BUILD SUCCESS
```

---

## 根因是什么

### 根因定位
**位置**：`RequestContext.java` 第 26-31 行

**问题代码**：
```java
public void reset() {
    Map<String, Object> ctx = LOCAL.get();
    if (ctx != null) {
        ctx.clear();  // 仅清空 Map 内容，未清理 ThreadLocal 本身
    }
}
```

### 问题分析
1. `ThreadLocal.get()` 在 `clear()` 后仍返回空的 Map 对象 `{}`
2. ThreadLocal 仍持有 Map 的引用，导致内存无法释放
3. Java 最佳实践要求：`ThreadLocal` 使用完毕后必须调用 `remove()` 方法彻底清理

---

## 怎么修

### 改动点

**文件**：`backend/src/main/java/com/example/backend/service/RequestContext.java`

**修复前**：
```java
public void reset() {
    Map<String, Object> ctx = LOCAL.get();
    if (ctx != null) {
        ctx.clear();
    }
}
```

**修复后**：
```java
public void reset() {
    LOCAL.remove();
}
```

### 修复原理
- 直接调用 `ThreadLocal.remove()` 可彻底移除当前线程的 ThreadLocal 值
- 符合 Java ThreadLocal 最佳实践，避免内存泄漏

---

## 如何验证 / 回归

### 验证命令
```bash
cd /Users/zhangguohui/workspace/vibe-coding/test/opencode/backend
mvn test
```

### 验证结果
- 所有测试通过（包括新加的回归测试）
- `mvn test` 返回 `BUILD SUCCESS`

### 回归用例
新增测试类：`UserProfileServiceMemoryLeakTest.java`
- `testThreadLocalCleanupAfterReset()`：验证 reset() 后 ThreadLocal 被正确清理
- `testThreadLocalCleanupOnException()`：验证异常场景下的内存管理
- `testMultipleCallsMemoryAccumulation()`：验证多次调用后无内存累积

---

## 风险与监控建议

### 风险评估
**风险等级**：低

**理由**：
- 改动范围小，仅修改一行代码
- 使用标准的 `ThreadLocal.remove()` 方法，是官方推荐做法
- 新增完整的回归测试覆盖

### 监控建议
1. **内存监控**：部署后观察 JVM 堆内存使用情况，特别是长期运行后的内存曲线
2. **线程监控**：监控线程池中的线程数量和状态
3. **GC 日志**：开启 GC 日志观察 `byte[]` 对象的回收情况

### 最佳实践建议
1. 所有使用 ThreadLocal 的代码都应遵循 "使用后必须 remove" 的原则
2. 建议在框架层面统一处理 ThreadLocal 的清理（如使用过滤器或拦截器）
3. 对于关键业务场景，建议添加 ThreadLocal 泄漏检测工具（如 `heapdump` 分析）

---

## 变更文件清单

| 文件路径 | 变更类型 | 说明 |
|---------|---------|------|
| `backend/src/main/java/com/example/backend/service/RequestContext.java` | 修改 | reset() 方法改为直接调用 LOCAL.remove() |
| `backend/src/test/java/com/example/backend/service/UserProfileServiceTest.java` | 新增 | 基础功能测试 |
| `backend/src/test/java/com/example/backend/service/UserProfileServiceMemoryLeakTest.java` | 新增 | 内存泄漏回归测试 |
| `docs/bugfix-sessionregistry-userdirectory-qa.md` | 新增 | QA 文档 |
