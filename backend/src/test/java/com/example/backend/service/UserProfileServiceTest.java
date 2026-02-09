package com.example.backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

public class UserProfileServiceTest {

    private final UserProfileService service = new UserProfileService();

    @Test
    @DisplayName("正常场景：获取用户展示名")
    public void testLoadDisplayName_Success() {
        String displayName = service.loadDisplayName("1001");
        assertEquals("Alice", displayName);
    }

    @Test
    @DisplayName("异常场景：用户不存在")
    public void testLoadDisplayName_UserNotFound() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> service.loadDisplayName("9999")
        );
        assertTrue(exception.getMessage().contains("user not found: 9999"));
    }

    @RepeatedTest(10)
    @DisplayName("并发测试：多个线程同时调用服务")
    public void testConcurrentAccess() throws InterruptedException {
        int threadCount = 10;
        Thread[] threads = new Thread[threadCount];
        RuntimeException[] exceptions = new RuntimeException[threadCount];

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                try {
                    String userId = (index % 2 == 0) ? "1001" : "1002";
                    String result = service.loadDisplayName(userId);
                    assertNotNull(result);
                } catch (RuntimeException e) {
                    exceptions[index] = e;
                }
            });
        }

        for (Thread thread : threads) {
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        for (RuntimeException e : exceptions) {
            if (e != null) {
                fail("并发测试失败: " + e.getMessage());
            }
        }
    }
}
