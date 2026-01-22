package com.example.qr_code.controller;

import com.example.qr_code.entity.FocusRecord;
import com.example.qr_code.entity.LearningStats;
import com.example.qr_code.entity.User;
import com.example.qr_code.service.FocusService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * FocusController 单元测试类
 * 测试专注记录相关功能：
 * 1. 保存专注记录
 * 2. 获取今日统计
 * 3. 获取今日详情
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FocusController 单元测试")
class FocusControllerTest {

    @InjectMocks
    private FocusController focusController;

    @Mock
    private FocusService focusService;

    private MockHttpSession session;
    private User testUser;
    private FocusRecord testRecord;
    private LearningStats testStats;

    @BeforeEach
    void setUp() {
        session = new MockHttpSession();
        
        // 创建测试用户
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setNickname("测试用户");
        
        // 创建测试专注记录
        testRecord = new FocusRecord();
        testRecord.setId(1L);
        testRecord.setUserId(1L);
        testRecord.setDuration(1800); // 30分钟
        testRecord.setType("free");
        testRecord.setStartTime(LocalDateTime.now().minusMinutes(30));
        testRecord.setEndTime(LocalDateTime.now());
        
        // 创建测试学习统计
        testStats = new LearningStats();
        testStats.setUserId(1L);
        testStats.setTotalDuration(3600); // 60分钟
        testStats.setFocusCount(2);
        testStats.setAvgDuration(1800);
        testStats.setMaxDuration(2400);
        testStats.setExpEarned(90);
        testStats.setTomatoCount(1);
    }

    /**
     * 测试保存专注记录 - 成功（自由模式）
     * 前置条件：用户已登录
     * 输入数据：有效的专注时长和类型
     * 预期结果：成功保存记录并返回统计信息
     */
    @Test
    @DisplayName("保存专注记录 - 成功（自由模式）")
    void testSaveFocusRecord_Success_FreeMode() {
        // Arrange
        session.setAttribute("user", testUser);
        Map<String, Object> body = new HashMap<>();
        body.put("duration", 1800);
        body.put("type", "free");
        
        when(focusService.saveFocusRecord(eq(testUser.getId()), eq(1800), any(LocalDateTime.class), any(LocalDateTime.class), eq("free")))
                .thenReturn(testRecord);
        when(focusService.getTodayStats(testUser.getId())).thenReturn(testStats);

        // Act
        Map<String, Object> response = focusController.saveFocusRecord(body, session);

        // Assert
        assertTrue((Boolean) response.get("success"));
        assertEquals("专注记录已保存", response.get("message"));
        assertEquals(testRecord.getId(), response.get("recordId"));
        assertEquals(45, response.get("expEarned")); // 30分钟 * 1.5 = 45经验
        assertEquals(testStats.getTotalDuration(), response.get("todayTotal"));
        assertEquals(testStats.getFocusCount(), response.get("todayCount"));
        assertEquals(testStats.getTomatoCount(), response.get("tomatoCount"));
        
        verify(focusService, times(1)).saveFocusRecord(eq(testUser.getId()), eq(1800), any(LocalDateTime.class), any(LocalDateTime.class), eq("free"));
        verify(focusService, times(1)).getTodayStats(testUser.getId());
    }

    /**
     * 测试保存专注记录 - 成功（番茄钟模式）
     * 前置条件：用户已登录
     * 输入数据：有效的专注时长和番茄钟类型
     * 预期结果：成功保存记录并获得额外经验奖励
     */
    @Test
    @DisplayName("保存专注记录 - 成功（番茄钟模式）")
    void testSaveFocusRecord_Success_PomodoroMode() {
        // Arrange
        session.setAttribute("user", testUser);
        Map<String, Object> body = new HashMap<>();
        body.put("duration", 1500); // 25分钟
        body.put("type", "pomodoro");
        
        FocusRecord pomodoroRecord = new FocusRecord();
        pomodoroRecord.setId(2L);
        pomodoroRecord.setUserId(1L);
        pomodoroRecord.setDuration(1500);
        pomodoroRecord.setType("pomodoro");
        
        when(focusService.saveFocusRecord(eq(testUser.getId()), eq(1500), any(LocalDateTime.class), any(LocalDateTime.class), eq("pomodoro")))
                .thenReturn(pomodoroRecord);
        when(focusService.getTodayStats(testUser.getId())).thenReturn(testStats);

        // Act
        Map<String, Object> response = focusController.saveFocusRecord(body, session);

        // Assert
        assertTrue((Boolean) response.get("success"));
        assertEquals("专注记录已保存", response.get("message"));
        assertEquals(pomodoroRecord.getId(), response.get("recordId"));
        assertEquals(30, response.get("expEarned")); // 25分钟 * 1.2 = 30经验
        
        verify(focusService, times(1)).saveFocusRecord(eq(testUser.getId()), eq(1500), any(LocalDateTime.class), any(LocalDateTime.class), eq("pomodoro"));
        verify(focusService, times(1)).getTodayStats(testUser.getId());
    }

    /**
     * 测试保存专注记录 - 成功（默认类型）
     * 前置条件：用户已登录
     * 输入数据：只有时长，没有类型
     * 预期结果：默认使用自由模式保存记录
     */
    @Test
    @DisplayName("保存专注记录 - 成功（默认类型）")
    void testSaveFocusRecord_Success_DefaultType() {
        // Arrange
        session.setAttribute("user", testUser);
        Map<String, Object> body = new HashMap<>();
        body.put("duration", 1800);
        // 不设置type，应该默认为"free"
        
        when(focusService.saveFocusRecord(eq(testUser.getId()), eq(1800), any(LocalDateTime.class), any(LocalDateTime.class), eq("free")))
                .thenReturn(testRecord);
        when(focusService.getTodayStats(testUser.getId())).thenReturn(testStats);

        // Act
        Map<String, Object> response = focusController.saveFocusRecord(body, session);

        // Assert
        assertTrue((Boolean) response.get("success"));
        assertEquals("专注记录已保存", response.get("message"));
        
        verify(focusService, times(1)).saveFocusRecord(eq(testUser.getId()), eq(1800), any(LocalDateTime.class), any(LocalDateTime.class), eq("free"));
    }

    /**
     * 测试保存专注记录 - 用户未登录
     * 前置条件：用户未登录
     * 输入数据：有效的专注记录
     * 预期结果：返回登录提示
     */
    @Test
    @DisplayName("保存专注记录 - 用户未登录")
    void testSaveFocusRecord_UserNotLoggedIn() {
        // Arrange
        Map<String, Object> body = new HashMap<>();
        body.put("duration", 1800);
        body.put("type", "free");

        // Act
        Map<String, Object> response = focusController.saveFocusRecord(body, session);

        // Assert
        assertFalse((Boolean) response.get("success"));
        assertEquals("请先登录", response.get("message"));
        
        verify(focusService, never()).saveFocusRecord(any(), any(), any(), any(), any());
    }

    /**
     * 测试保存专注记录 - 无效时长（null）
     * 前置条件：用户已登录
     * 输入数据：时长为null
     * 预期结果：返回无效时长提示
     */
    @Test
    @DisplayName("保存专注记录 - 无效时长（null）")
    void testSaveFocusRecord_InvalidDuration_Null() {
        // Arrange
        session.setAttribute("user", testUser);
        Map<String, Object> body = new HashMap<>();
        body.put("duration", null);
        body.put("type", "free");

        // Act
        Map<String, Object> response = focusController.saveFocusRecord(body, session);

        // Assert
        assertFalse((Boolean) response.get("success"));
        assertEquals("无效的专注时长", response.get("message"));
        
        verify(focusService, never()).saveFocusRecord(any(), any(), any(), any(), any());
    }

    /**
     * 测试保存专注记录 - 无效时长（负数）
     * 前置条件：用户已登录
     * 输入数据：时长为负数
     * 预期结果：返回无效时长提示
     */
    @Test
    @DisplayName("保存专注记录 - 无效时长（负数）")
    void testSaveFocusRecord_InvalidDuration_Negative() {
        // Arrange
        session.setAttribute("user", testUser);
        Map<String, Object> body = new HashMap<>();
        body.put("duration", -100);
        body.put("type", "free");

        // Act
        Map<String, Object> response = focusController.saveFocusRecord(body, session);

        // Assert
        assertFalse((Boolean) response.get("success"));
        assertEquals("无效的专注时长", response.get("message"));
        
        verify(focusService, never()).saveFocusRecord(any(), any(), any(), any(), any());
    }

    /**
     * 测试保存专注记录 - 服务异常
     * 前置条件：用户已登录
     * 输入数据：有效的专注记录
     * 预期结果：返回保存失败提示
     */
    @Test
    @DisplayName("保存专注记录 - 服务异常")
    void testSaveFocusRecord_ServiceException() {
        // Arrange
        session.setAttribute("user", testUser);
        Map<String, Object> body = new HashMap<>();
        body.put("duration", 1800);
        body.put("type", "free");
        
        when(focusService.saveFocusRecord(any(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("数据库连接失败"));

        // Act
        Map<String, Object> response = focusController.saveFocusRecord(body, session);

        // Assert
        assertFalse((Boolean) response.get("success"));
        assertEquals("保存失败: 数据库连接失败", response.get("message"));
        
        verify(focusService, times(1)).saveFocusRecord(any(), any(), any(), any(), any());
    }

    /**
     * 测试获取今日统计 - 成功（有数据）
     * 前置条件：用户已登录且有学习记录
     * 输入数据：有效的用户会话
     * 预期结果：返回今日学习统计
     */
    @Test
    @DisplayName("获取今日统计 - 成功（有数据）")
    void testGetTodayStats_Success_HasData() {
        // Arrange
        session.setAttribute("user", testUser);
        when(focusService.getTodayStats(testUser.getId())).thenReturn(testStats);

        // Act
        Map<String, Object> response = focusController.getTodayStats(session);

        // Assert
        assertTrue((Boolean) response.get("success"));
        assertEquals(testStats.getTotalDuration(), response.get("totalDuration"));
        assertEquals(testStats.getFocusCount(), response.get("focusCount"));
        assertEquals(testStats.getAvgDuration(), response.get("avgDuration"));
        assertEquals(testStats.getMaxDuration(), response.get("maxDuration"));
        assertEquals(testStats.getExpEarned(), response.get("expEarned"));
        assertEquals(testStats.getTomatoCount(), response.get("tomatoCount"));
        
        verify(focusService, times(1)).getTodayStats(testUser.getId());
    }

    /**
     * 测试获取今日统计 - 成功（无数据）
     * 前置条件：用户已登录但没有学习记录
     * 输入数据：有效的用户会话
     * 预期结果：返回默认值
     */
    @Test
    @DisplayName("获取今日统计 - 成功（无数据）")
    void testGetTodayStats_Success_NoData() {
        // Arrange
        session.setAttribute("user", testUser);
        when(focusService.getTodayStats(testUser.getId())).thenReturn(null);

        // Act
        Map<String, Object> response = focusController.getTodayStats(session);

        // Assert
        assertTrue((Boolean) response.get("success"));
        assertEquals(0, response.get("totalDuration"));
        assertEquals(0, response.get("focusCount"));
        assertEquals(0, response.get("avgDuration"));
        assertEquals(0, response.get("maxDuration"));
        assertEquals(0, response.get("expEarned"));
        assertEquals(0, response.get("tomatoCount"));
        
        verify(focusService, times(1)).getTodayStats(testUser.getId());
    }

    /**
     * 测试获取今日统计 - 用户未登录
     * 前置条件：用户未登录
     * 输入数据：空的用户会话
     * 预期结果：返回登录提示
     */
    @Test
    @DisplayName("获取今日统计 - 用户未登录")
    void testGetTodayStats_UserNotLoggedIn() {
        // Arrange - 不设置用户会话

        // Act
        Map<String, Object> response = focusController.getTodayStats(session);

        // Assert
        assertFalse((Boolean) response.get("success"));
        assertEquals("请先登录", response.get("message"));
        
        verify(focusService, never()).getTodayStats(any());
    }

    /**
     * 测试获取今日详情 - 成功（有数据）
     * 前置条件：用户已登录且有学习记录
     * 输入数据：有效的用户会话
     * 预期结果：返回今日统计和记录列表
     */
    @Test
    @DisplayName("获取今日详情 - 成功（有数据）")
    void testGetTodayDetails_Success_HasData() {
        // Arrange
        session.setAttribute("user", testUser);
        
        FocusRecord record1 = new FocusRecord();
        record1.setId(1L);
        record1.setDuration(1800);
        record1.setType("free");
        record1.setStartTime(LocalDateTime.now().minusHours(2));
        record1.setEndTime(LocalDateTime.now().minusHours(1).minusMinutes(30));
        
        FocusRecord record2 = new FocusRecord();
        record2.setId(2L);
        record2.setDuration(1500);
        record2.setType("pomodoro");
        record2.setStartTime(LocalDateTime.now().minusMinutes(25));
        record2.setEndTime(LocalDateTime.now());
        
        List<FocusRecord> records = Arrays.asList(record1, record2);
        
        when(focusService.getTodayStats(testUser.getId())).thenReturn(testStats);
        when(focusService.getTodayRecords(testUser.getId())).thenReturn(records);

        // Act
        Map<String, Object> response = focusController.getTodayDetails(session);

        // Assert
        assertTrue((Boolean) response.get("success"));
        assertEquals(testStats.getTotalDuration(), response.get("totalDuration"));
        assertEquals(testStats.getFocusCount(), response.get("focusCount"));
        assertEquals(testStats.getExpEarned(), response.get("expEarned"));
        assertEquals(testStats.getTomatoCount(), response.get("tomatoCount"));
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> recordList = (List<Map<String, Object>>) response.get("records");
        assertEquals(2, recordList.size());
        
        Map<String, Object> r1 = recordList.get(0);
        assertEquals(record1.getId(), r1.get("id"));
        assertEquals(record1.getDuration(), r1.get("duration"));
        assertEquals(record1.getType(), r1.get("type"));
        assertEquals(45, r1.get("exp")); // 30分钟 * 1.5 = 45经验
        
        Map<String, Object> r2 = recordList.get(1);
        assertEquals(record2.getId(), r2.get("id"));
        assertEquals(record2.getDuration(), r2.get("duration"));
        assertEquals(record2.getType(), r2.get("type"));
        assertEquals(30, r2.get("exp")); // 25分钟 * 1.2 = 30经验
        
        verify(focusService, times(1)).getTodayStats(testUser.getId());
        verify(focusService, times(1)).getTodayRecords(testUser.getId());
    }

    /**
     * 测试获取今日详情 - 成功（无数据）
     * 前置条件：用户已登录但没有学习记录
     * 输入数据：有效的用户会话
     * 预期结果：返回默认值和空记录列表
     */
    @Test
    @DisplayName("获取今日详情 - 成功（无数据）")
    void testGetTodayDetails_Success_NoData() {
        // Arrange
        session.setAttribute("user", testUser);
        when(focusService.getTodayStats(testUser.getId())).thenReturn(null);
        when(focusService.getTodayRecords(testUser.getId())).thenReturn(Arrays.asList());

        // Act
        Map<String, Object> response = focusController.getTodayDetails(session);

        // Assert
        assertTrue((Boolean) response.get("success"));
        assertEquals(0, response.get("totalDuration"));
        assertEquals(0, response.get("focusCount"));
        assertEquals(0, response.get("expEarned"));
        assertEquals(0, response.get("tomatoCount"));
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> recordList = (List<Map<String, Object>>) response.get("records");
        assertEquals(0, recordList.size());
        
        verify(focusService, times(1)).getTodayStats(testUser.getId());
        verify(focusService, times(1)).getTodayRecords(testUser.getId());
    }

    /**
     * 测试获取今日详情 - 用户未登录
     * 前置条件：用户未登录
     * 输入数据：空的用户会话
     * 预期结果：返回登录提示
     */
    @Test
    @DisplayName("获取今日详情 - 用户未登录")
    void testGetTodayDetails_UserNotLoggedIn() {
        // Arrange - 不设置用户会话

        // Act
        Map<String, Object> response = focusController.getTodayDetails(session);

        // Assert
        assertFalse((Boolean) response.get("success"));
        assertEquals("请先登录", response.get("message"));
        
        verify(focusService, never()).getTodayStats(any());
        verify(focusService, never()).getTodayRecords(any());
    }

    /**
     * 测试获取今日详情 - 记录类型为null的处理
     * 前置条件：用户已登录且有学习记录
     * 输入数据：有效的用户会话
     * 预期结果：正确处理null类型，默认为"free"
     */
    @Test
    @DisplayName("获取今日详情 - 记录类型为null的处理")
    void testGetTodayDetails_RecordTypeNull() {
        // Arrange
        session.setAttribute("user", testUser);
        
        FocusRecord record = new FocusRecord();
        record.setId(1L);
        record.setDuration(1800);
        record.setType(null); // 类型为null
        record.setStartTime(LocalDateTime.now().minusMinutes(30));
        record.setEndTime(LocalDateTime.now());
        
        List<FocusRecord> records = Arrays.asList(record);
        
        when(focusService.getTodayStats(testUser.getId())).thenReturn(testStats);
        when(focusService.getTodayRecords(testUser.getId())).thenReturn(records);

        // Act
        Map<String, Object> response = focusController.getTodayDetails(session);

        // Assert
        assertTrue((Boolean) response.get("success"));
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> recordList = (List<Map<String, Object>>) response.get("records");
        assertEquals(1, recordList.size());
        
        Map<String, Object> r = recordList.get(0);
        assertEquals("free", r.get("type")); // null应该被转换为"free"
        assertEquals(45, r.get("exp")); // 按free模式计算经验
        
        verify(focusService, times(1)).getTodayStats(testUser.getId());
        verify(focusService, times(1)).getTodayRecords(testUser.getId());
    }
}