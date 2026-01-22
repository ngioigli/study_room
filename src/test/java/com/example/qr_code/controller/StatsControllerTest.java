package com.example.qr_code.controller;

import com.example.qr_code.entity.User;
import com.example.qr_code.service.StatsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * StatsController 单元测试
 * 测试学习统计相关接口的功能
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("学习统计控制器测试")
class StatsControllerTest {

    @InjectMocks
    private StatsController statsController;

    @Mock
    private StatsService statsService;

    private MockHttpSession session;
    private User testUser;

    @BeforeEach
    void setUp() {
        session = new MockHttpSession();
        testUser = new User();
        testUser.setId(1L);
        testUser.setNickname("测试用户");
        testUser.setUsername("testuser");
    }

    /**
     * 测试场景：获取周统计数据 - 用户已登录
     * 前置条件：用户已登录，存在统计数据
     * 输入数据：有效的用户会话
     * 预期结果：返回成功响应和周统计数据
     */
    @Test
    @DisplayName("获取周统计数据 - 成功")
    void testGetWeeklyStats_Success() {
        // Arrange
        session.setAttribute("user", testUser);
        
        Map<String, Object> mockStats = new HashMap<>();
        mockStats.put("weeklyFocusTime", 300);
        mockStats.put("weeklyFocusCount", 15);
        mockStats.put("dailyData", Arrays.asList(60, 45, 30, 90, 75, 0, 0));
        
        when(statsService.getWeeklyStats(testUser.getId())).thenReturn(mockStats);

        // Act
        Map<String, Object> response = statsController.getWeeklyStats(session);

        // Assert
        assertTrue((Boolean) response.get("success"));
        assertEquals(300, response.get("weeklyFocusTime"));
        assertEquals(15, response.get("weeklyFocusCount"));
        assertNotNull(response.get("dailyData"));
        verify(statsService, times(1)).getWeeklyStats(testUser.getId());
    }

    /**
     * 测试场景：获取周统计数据 - 用户未登录
     * 前置条件：用户未登录
     * 输入数据：空的用户会话
     * 预期结果：返回失败响应和登录提示
     */
    @Test
    @DisplayName("获取周统计数据 - 用户未登录")
    void testGetWeeklyStats_NotLoggedIn() {
        // Act
        Map<String, Object> response = statsController.getWeeklyStats(session);

        // Assert
        assertFalse((Boolean) response.get("success"));
        assertEquals("请先登录", response.get("message"));
        verify(statsService, never()).getWeeklyStats(any());
    }

    /**
     * 测试场景：获取周统计数据 - 服务异常
     * 前置条件：用户已登录，服务层抛出异常
     * 输入数据：有效的用户会话
     * 预期结果：返回失败响应和错误信息
     */
    @Test
    @DisplayName("获取周统计数据 - 服务异常")
    void testGetWeeklyStats_ServiceException() {
        // Arrange
        session.setAttribute("user", testUser);
        
        when(statsService.getWeeklyStats(testUser.getId())).thenThrow(new RuntimeException("数据库连接失败"));

        // Act
        Map<String, Object> response = statsController.getWeeklyStats(session);

        // Assert
        assertFalse((Boolean) response.get("success"));
        assertTrue(((String) response.get("message")).contains("获取统计失败"));
        verify(statsService, times(1)).getWeeklyStats(testUser.getId());
    }

    /**
     * 测试场景：获取月统计数据 - 成功获取
     * 前置条件：用户已登录，存在统计数据
     * 输入数据：有效的用户会话
     * 预期结果：返回成功响应和月统计数据
     */
    @Test
    @DisplayName("获取月统计数据 - 成功")
    void testGetMonthlyStats_Success() {
        // Arrange
        session.setAttribute("user", testUser);
        
        Map<String, Object> mockStats = new HashMap<>();
        mockStats.put("monthlyFocusTime", 1200);
        mockStats.put("monthlyFocusCount", 60);
        mockStats.put("averageDailyTime", 40);
        
        when(statsService.getMonthlyStats(testUser.getId())).thenReturn(mockStats);

        // Act
        Map<String, Object> response = statsController.getMonthlyStats(session);

        // Assert
        assertTrue((Boolean) response.get("success"));
        assertEquals(1200, response.get("monthlyFocusTime"));
        assertEquals(60, response.get("monthlyFocusCount"));
        assertEquals(40, response.get("averageDailyTime"));
        verify(statsService, times(1)).getMonthlyStats(testUser.getId());
    }

    /**
     * 测试场景：获取时段分布 - 成功获取
     * 前置条件：用户已登录，存在时段数据
     * 输入数据：有效的用户会话
     * 预期结果：返回成功响应和时段分布数据
     */
    @Test
    @DisplayName("获取时段分布 - 成功")
    void testGetHourlyDistribution_Success() {
        // Arrange
        session.setAttribute("user", testUser);
        
        Map<String, Object> mockStats = new HashMap<>();
        List<Integer> hourlyData = Arrays.asList(0, 0, 0, 0, 0, 0, 0, 0, 30, 45, 60, 30, 15, 0, 45, 60, 30, 15, 0, 0, 0, 0, 0, 0);
        mockStats.put("hourlyDistribution", hourlyData);
        mockStats.put("peakHour", 11);
        mockStats.put("totalTime", 300);
        
        when(statsService.getHourlyDistribution(testUser.getId())).thenReturn(mockStats);

        // Act
        Map<String, Object> response = statsController.getHourlyDistribution(session);

        // Assert
        assertTrue((Boolean) response.get("success"));
        assertEquals(hourlyData, response.get("hourlyDistribution"));
        assertEquals(11, response.get("peakHour"));
        assertEquals(300, response.get("totalTime"));
        verify(statsService, times(1)).getHourlyDistribution(testUser.getId());
    }

    /**
     * 测试场景：获取学习画像 - 成功获取
     * 前置条件：用户已登录，存在学习数据
     * 输入数据：有效的用户会话
     * 预期结果：返回成功响应和学习画像数据
     */
    @Test
    @DisplayName("获取学习画像 - 成功")
    void testGetLearningProfile_Success() {
        // Arrange
        session.setAttribute("user", testUser);
        
        Map<String, Object> mockProfile = new HashMap<>();
        mockProfile.put("learningType", "夜猫子型");
        mockProfile.put("focusLevel", "高度专注");
        mockProfile.put("consistency", "持续稳定");
        mockProfile.put("averageSessionTime", 45);
        mockProfile.put("preferredTimeSlot", "晚上");
        
        when(statsService.getLearningProfile(testUser.getId())).thenReturn(mockProfile);

        // Act
        Map<String, Object> response = statsController.getLearningProfile(session);

        // Assert
        assertTrue((Boolean) response.get("success"));
        assertEquals("夜猫子型", response.get("learningType"));
        assertEquals("高度专注", response.get("focusLevel"));
        assertEquals("持续稳定", response.get("consistency"));
        assertEquals(45, response.get("averageSessionTime"));
        assertEquals("晚上", response.get("preferredTimeSlot"));
        verify(statsService, times(1)).getLearningProfile(testUser.getId());
    }

    /**
     * 测试场景：获取学习画像 - 用户未登录
     * 前置条件：用户未登录
     * 输入数据：空的用户会话
     * 预期结果：返回失败响应和登录提示
     */
    @Test
    @DisplayName("获取学习画像 - 用户未登录")
    void testGetLearningProfile_NotLoggedIn() {
        // Act
        Map<String, Object> response = statsController.getLearningProfile(session);

        // Assert
        assertFalse((Boolean) response.get("success"));
        assertEquals("请先登录", response.get("message"));
        verify(statsService, never()).getLearningProfile(any());
    }

    /**
     * 测试场景：获取学习画像 - 服务异常
     * 前置条件：用户已登录，服务层抛出异常
     * 输入数据：有效的用户会话
     * 预期结果：返回失败响应和错误信息
     */
    @Test
    @DisplayName("获取学习画像 - 服务异常")
    void testGetLearningProfile_ServiceException() {
        // Arrange
        session.setAttribute("user", testUser);
        
        when(statsService.getLearningProfile(testUser.getId())).thenThrow(new RuntimeException("计算错误"));

        // Act
        Map<String, Object> response = statsController.getLearningProfile(session);

        // Assert
        assertFalse((Boolean) response.get("success"));
        assertTrue(((String) response.get("message")).contains("获取画像失败"));
        verify(statsService, times(1)).getLearningProfile(testUser.getId());
    }
}