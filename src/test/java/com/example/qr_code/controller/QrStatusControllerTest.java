package com.example.qr_code.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * QrStatusController 单元测试
 * 测试二维码状态控制功能
 */
public class QrStatusControllerTest {

    @InjectMocks
    private QrStatusController qrStatusController;

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        // 重置cooldownEndTime为0
        setCooldownEndTime(0L);
    }

    /**
     * 辅助方法：通过反射设置cooldownEndTime
     */
    private void setCooldownEndTime(long value) throws Exception {
        Field field = QrStatusController.class.getDeclaredField("cooldownEndTime");
        field.setAccessible(true);
        field.set(null, value);
    }

    /**
     * 辅助方法：通过反射获取cooldownEndTime
     */
    private long getCooldownEndTime() throws Exception {
        Field field = QrStatusController.class.getDeclaredField("cooldownEndTime");
        field.setAccessible(true);
        return (Long) field.get(null);
    }

    /**
     * 测试场景：检查状态时二维码处于FREE状态
     * 前置条件：cooldownEndTime已过期或为0
     * 输入数据：无
     * 预期结果：返回FREE状态，endTime为0
     */
    @Test
    public void testCheckStatus_Free() {
        Map<String, Object> response = qrStatusController.checkStatus();

        assertNotNull(response);
        assertEquals("FREE", response.get("status"));
        assertEquals(0, ((Number) response.get("endTime")).longValue());
    }

    /**
     * 测试场景：检查状态时二维码处于LOCKED状态
     * 前置条件：cooldownEndTime未过期
     * 输入数据：无
     * 预期结果：返回LOCKED状态，endTime为锁定结束时间
     */
    @Test
    public void testCheckStatus_Locked() throws Exception {
        long futureTime = System.currentTimeMillis() + 60000; // 未来1分钟
        setCooldownEndTime(futureTime);

        Map<String, Object> response = qrStatusController.checkStatus();

        assertNotNull(response);
        assertEquals("LOCKED", response.get("status"));
        assertEquals(futureTime, response.get("endTime"));
    }

    /**
     * 测试场景：成功锁定二维码
     * 前置条件：cooldownEndTime已过期
     * 输入数据：无
     * 预期结果：锁定成功，返回结束时间
     */
    @Test
    public void testLockQrCode_Success() throws Exception {
        Map<String, Object> response = qrStatusController.lockQrCode();

        assertNotNull(response);
        assertEquals(true, response.get("success"));
        assertNotNull(response.get("endTime"));

        // 验证cooldownEndTime已被设置
        long cooldownEnd = getCooldownEndTime();
        assertTrue(cooldownEnd > System.currentTimeMillis());
    }

    /**
     * 测试场景：尝试锁定已被锁定的二维码
     * 前置条件：cooldownEndTime未过期
     * 输入数据：无
     * 预期结果：锁定失败，返回错误信息
     */
    @Test
    public void testLockQrCode_AlreadyLocked() throws Exception {
        long futureTime = System.currentTimeMillis() + 60000; // 未来1分钟
        setCooldownEndTime(futureTime);

        Map<String, Object> response = qrStatusController.lockQrCode();

        assertNotNull(response);
        assertEquals(false, response.get("success"));
        assertEquals("哎呀，慢了一步！已经被别人锁定了。", response.get("message"));

        // 验证cooldownEndTime未被修改
        assertEquals(futureTime, getCooldownEndTime());
    }

    /**
     * 测试场景：锁定时间到期后可以重新锁定
     * 前置条件：cooldownEndTime已过期
     * 输入数据：无
     * 预期结果：可以成功锁定
     */
    @Test
    public void testLockQrCode_ExpiredCanLockAgain() throws Exception {
        long pastTime = System.currentTimeMillis() - 1000; // 过去1秒
        setCooldownEndTime(pastTime);

        Map<String, Object> response = qrStatusController.lockQrCode();

        assertNotNull(response);
        assertEquals(true, response.get("success"));
        assertNotNull(response.get("endTime"));

        // 验证新的cooldownEndTime大于当前时间
        long newCooldownEnd = getCooldownEndTime();
        assertTrue(newCooldownEnd > System.currentTimeMillis());
    }

    /**
     * 测试场景：并发锁定测试（模拟竞态条件）
     * 前置条件：初始状态为FREE
     * 输入数据：无
     * 预期结果：只有第一次锁定成功，第二次失败
     */
    @Test
    public void testLockQrCode_ConcurrentAccess() throws Exception {
        // 第一次锁定
        Map<String, Object> response1 = qrStatusController.lockQrCode();
        assertEquals(true, response1.get("success"));

        // 立即尝试第二次锁定
        Map<String, Object> response2 = qrStatusController.lockQrCode();
        assertEquals(false, response2.get("success"));
    }

    /**
     * 测试场景：完整的锁定-检查-释放周期
     * 前置条件：初始状态为FREE
     * 输入数据：无
     * 预期结果：状态正确转换
     */
    @Test
    public void testLockCheckReleaseCycle() throws Exception {
        // 1. 初始检查：应该是FREE
        Map<String, Object> checkResponse1 = qrStatusController.checkStatus();
        assertEquals("FREE", checkResponse1.get("status"));

        // 2. 锁定
        Map<String, Object> lockResponse = qrStatusController.lockQrCode();
        assertEquals(true, lockResponse.get("success"));

        // 3. 检查：应该是LOCKED
        Map<String, Object> checkResponse2 = qrStatusController.checkStatus();
        assertEquals("LOCKED", checkResponse2.get("status"));

        // 4. 模拟时间过期
        setCooldownEndTime(System.currentTimeMillis() - 1000);

        // 5. 再次检查：应该是FREE
        Map<String, Object> checkResponse3 = qrStatusController.checkStatus();
        assertEquals("FREE", checkResponse3.get("status"));
    }
}
