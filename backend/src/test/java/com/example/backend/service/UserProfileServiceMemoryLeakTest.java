package com.example.backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class UserProfileServiceMemoryLeakTest {

    @Test
    @DisplayName("测试 reset() 后 ThreadLocal 的清理")
    public void testThreadLocalCleanupAfterReset() throws Exception {
        UserProfileService service = new UserProfileService();

        // 正常调用 bind 和 reset
        service.loadDisplayName("1001");

        // 获取 RequestContext 的 LOCAL 字段
        Field localField = RequestContext.class.getDeclaredField("LOCAL");
        localField.setAccessible(true);
        ThreadLocal<Map<String, Object>> threadLocal = (ThreadLocal<Map<String, Object>>) localField.get(null);

        // reset 后 ThreadLocal 应该为空
        Map<String, Object> context = threadLocal.get();
        assertNull(context, "reset() 后 ThreadLocal 应该为空");
    }

    @Test
    @DisplayName("测试异常情况下 ThreadLocal 的清理")
    public void testThreadLocalCleanupOnException() throws Exception {
        Field localField = RequestContext.class.getDeclaredField("LOCAL");
        localField.setAccessible(true);
        ThreadLocal<Map<String, Object>> threadLocal = (ThreadLocal<Map<String, Object>>) localField.get(null);

        UserProfileService service = new UserProfileService();

        // 模拟异常情况：先 bind 但不 reset
        RequestContext requestContext = new RequestContext();
        requestContext.bind("trace-001", "9999"); // 9999 不存在会抛出异常

        // 此时 ThreadLocal 应该不为空
        assertNotNull(threadLocal.get(), "异常前 ThreadLocal 不应为空");

        // 验证 payload 大小
        Map<String, Object> ctx = threadLocal.get();
        assertNotNull(ctx.get("payload"), "payload 应该存在");
        assertEquals(2 * 1024 * 1024, ((byte[]) ctx.get("payload")).length);

        // 手动清理以避免影响其他测试
        threadLocal.remove();
    }

    @Test
    @DisplayName("测试多次调用后内存累积问题")
    public void testMultipleCallsMemoryAccumulation() throws Exception {
        Field localField = RequestContext.class.getDeclaredField("LOCAL");
        localField.setAccessible(true);
        ThreadLocal<Map<String, Object>> threadLocal = (ThreadLocal<Map<String, Object>>) localField.get(null);

        UserProfileService service = new UserProfileService();

        // 模拟多次调用（不抛出异常的情况）
        for (int i = 0; i < 5; i++) {
            service.loadDisplayName("1001");
        }

        // 每次调用后 reset 应该清理 ThreadLocal
        assertNull(threadLocal.get(), "多次调用后 ThreadLocal 应该为空");

        // 清理
        threadLocal.remove();
    }
}
