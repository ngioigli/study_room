package com.example.qr_code.controller;

import com.example.qr_code.entity.User;
import com.example.qr_code.service.UserService;
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
 * UserController 单元测试
 * 测试用户管理相关接口的功能
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("用户控制器测试")
class UserControllerTest {

    @InjectMocks
    private UserController userController;

    @Mock
    private UserService userService;

    private MockHttpSession session;
    private User testUser;

    @BeforeEach
    void setUp() {
        session = new MockHttpSession();
        testUser = new User();
        testUser.setId(1L);
        testUser.setNickname("测试用户");
        testUser.setUsername("testuser");
        testUser.setAvatar("😊");
        testUser.setSignature("努力学习中");
        testUser.setTodayStatus("专注中");
    }

    /**
     * 测试场景：获取用户资料 - 用户已登录
     * 前置条件：用户已登录
     * 输入数据：有效的用户会话
     * 预期结果：返回成功响应和用户资料
     */
    @Test
    @DisplayName("获取用户资料 - 成功")
    void testGetProfile_Success() {
        // Arrange
        session.setAttribute("user", testUser);
        
        Map<String, Object> mockStats = new HashMap<>();
        mockStats.put("totalMinutes", 120);
        mockStats.put("totalHours", 2);
        mockStats.put("totalSessions", 5);
        mockStats.put("studyDays", 10);
        mockStats.put("streakDays", 3);
        
        when(userService.getUserById(testUser.getId())).thenReturn(testUser);
        when(userService.getUserStats(testUser.getId())).thenReturn(mockStats);

        // Act
        Map<String, Object> response = userController.getProfile(session);

        // Assert
        assertTrue((Boolean) response.get("success"));
        assertNotNull(response.get("profile"));
        assertEquals(mockStats, response.get("stats"));
        verify(userService, times(1)).getUserById(testUser.getId());
        verify(userService, times(1)).getUserStats(testUser.getId());
    }

    /**
     * 测试场景：获取用户资料 - 用户未登录
     * 前置条件：用户未登录
     * 输入数据：空的用户会话
     * 预期结果：返回失败响应和登录提示
     */
    @Test
    @DisplayName("获取用户资料 - 用户未登录")
    void testGetProfile_NotLoggedIn() {
        // Act
        Map<String, Object> response = userController.getProfile(session);

        // Assert
        assertFalse((Boolean) response.get("success"));
        assertEquals("请先登录", response.get("message"));
        verify(userService, never()).getUserStats(any());
    }

    /**
     * 测试场景：更新用户昵称 - 成功更新
     * 前置条件：用户已登录，输入有效昵称
     * 输入数据：有效的昵称
     * 预期结果：返回成功响应，会话中用户信息更新
     */
    @Test
    @DisplayName("更新用户昵称 - 成功")
    void testUpdateNickname_Success() {
        // Arrange
        session.setAttribute("user", testUser);
        
        Map<String, String> data = new HashMap<>();
        data.put("nickname", "新昵称");
        
        when(userService.updateNickname(testUser.getId(), "新昵称")).thenReturn(true);

        // Act
        Map<String, Object> response = userController.updateNickname(data, session);

        // Assert
        assertTrue((Boolean) response.get("success"));
        assertEquals("昵称修改成功", response.get("message"));
        assertEquals("新昵称", testUser.getNickname());
        verify(userService, times(1)).updateNickname(testUser.getId(), "新昵称");
    }

    /**
     * 测试场景：更新用户昵称 - 昵称为空
     * 前置条件：用户已登录，输入空昵称
     * 输入数据：空的昵称
     * 预期结果：返回失败响应和错误信息
     */
    @Test
    @DisplayName("更新用户昵称 - 昵称为空")
    void testUpdateNickname_EmptyNickname() {
        // Arrange
        session.setAttribute("user", testUser);
        
        Map<String, String> data = new HashMap<>();
        data.put("nickname", "");

        // Act
        Map<String, Object> response = userController.updateNickname(data, session);

        // Assert
        assertFalse((Boolean) response.get("success"));
        assertEquals("昵称不能为空", response.get("message"));
        verify(userService, never()).updateNickname(any(), any());
    }

    /**
     * 测试场景：更新用户昵称 - 用户未登录
     * 前置条件：用户未登录
     * 输入数据：昵称数据
     * 预期结果：返回失败响应和登录提示
     */
    @Test
    @DisplayName("更新用户昵称 - 用户未登录")
    void testUpdateNickname_NotLoggedIn() {
        // Arrange
        Map<String, String> data = new HashMap<>();
        data.put("nickname", "新昵称");

        // Act
        Map<String, Object> response = userController.updateNickname(data, session);

        // Assert
        assertFalse((Boolean) response.get("success"));
        assertEquals("请先登录", response.get("message"));
        verify(userService, never()).updateNickname(any(), any());
    }

    /**
     * 测试场景：更新用户头像 - 成功更新
     * 前置条件：用户已登录，输入有效头像
     * 输入数据：有效的头像表情
     * 预期结果：返回成功响应，会话中用户信息更新
     */
    @Test
    @DisplayName("更新用户头像 - 成功")
    void testUpdateAvatar_Success() {
        // Arrange
        session.setAttribute("user", testUser);
        
        Map<String, String> data = new HashMap<>();
        data.put("avatar", "🎉");
        
        when(userService.updateAvatar(testUser.getId(), "🎉")).thenReturn(true);

        // Act
        Map<String, Object> response = userController.updateAvatar(data, session);

        // Assert
        assertTrue((Boolean) response.get("success"));
        assertEquals("头像修改成功", response.get("message"));
        assertEquals("🎉", testUser.getAvatar());
        verify(userService, times(1)).updateAvatar(testUser.getId(), "🎉");
    }

    /**
     * 测试场景：更新用户头像 - 头像为空
     * 前置条件：用户已登录，输入空头像
     * 输入数据：空的头像
     * 预期结果：返回失败响应和错误信息
     */
    @Test
    @DisplayName("更新用户头像 - 头像为空")
    void testUpdateAvatar_EmptyAvatar() {
        // Arrange
        session.setAttribute("user", testUser);
        
        Map<String, String> data = new HashMap<>();
        data.put("avatar", "");

        // Act
        Map<String, Object> response = userController.updateAvatar(data, session);

        // Assert
        assertFalse((Boolean) response.get("success"));
        assertEquals("头像不能为空", response.get("message"));
        verify(userService, never()).updateAvatar(any(), any());
    }

    /**
     * 测试场景：更新用户个性签名 - 成功更新
     * 前置条件：用户已登录，输入有效签名
     * 输入数据：有效的个性签名
     * 预期结果：返回成功响应，会话中用户信息更新
     */
    @Test
    @DisplayName("更新用户个性签名 - 成功")
    void testUpdateSignature_Success() {
        // Arrange
        session.setAttribute("user", testUser);
        
        Map<String, String> data = new HashMap<>();
        data.put("signature", "新的个性签名");
        
        when(userService.updateSignature(testUser.getId(), "新的个性签名")).thenReturn(true);

        // Act
        Map<String, Object> response = userController.updateSignature(data, session);

        // Assert
        assertTrue((Boolean) response.get("success"));
        assertEquals("签名修改成功", response.get("message"));
        assertEquals("新的个性签名", testUser.getSignature());
        verify(userService, times(1)).updateSignature(testUser.getId(), "新的个性签名");
    }

    /**
     * 测试场景：更新用户状态 - 成功更新
     * 前置条件：用户已登录，输入有效状态
     * 输入数据：有效的用户状态
     * 预期结果：返回成功响应，会话中用户信息更新
     */
    @Test
    @DisplayName("更新用户状态 - 成功")
    void testUpdateTodayStatus_Success() {
        // Arrange
        session.setAttribute("user", testUser);
        
        Map<String, String> data = new HashMap<>();
        data.put("todayStatus", "学习中");
        
        when(userService.updateTodayStatus(testUser.getId(), "学习中")).thenReturn(true);

        // Act
        Map<String, Object> response = userController.updateTodayStatus(data, session);

        // Assert
        assertTrue((Boolean) response.get("success"));
        assertEquals("状态修改成功", response.get("message"));
        assertEquals("学习中", testUser.getTodayStatus());
        verify(userService, times(1)).updateTodayStatus(testUser.getId(), "学习中");
    }

    /**
     * 测试场景：更新用户状态 - 状态为空
     * 前置条件：用户已登录，输入空状态
     * 输入数据：空的状态
     * 预期结果：返回失败响应和错误信息
     */
    @Test
    @DisplayName("更新用户状态 - 状态为空")
    void testUpdateTodayStatus_EmptyStatus() {
        // Arrange
        session.setAttribute("user", testUser);
        
        Map<String, String> data = new HashMap<>();
        data.put("todayStatus", "");

        // Act
        Map<String, Object> response = userController.updateTodayStatus(data, session);

        // Assert
        assertFalse((Boolean) response.get("success"));
        assertEquals("状态不能为空", response.get("message"));
        verify(userService, never()).updateTodayStatus(any(), any());
    }

    /**
     * 测试场景：获取头像列表 - 成功获取
     * 前置条件：系统中存在预设头像
     * 输入数据：无需输入
     * 预期结果：返回成功响应和头像列表
     */
    @Test
    @DisplayName("获取头像列表 - 成功")
    void testGetAvatarList_Success() {
        // Act
        Map<String, Object> response = userController.getAvatarList();

        // Assert
        assertTrue((Boolean) response.get("success"));
        assertNotNull(response.get("avatars"));
        
        String[] avatars = (String[]) response.get("avatars");
        assertTrue(avatars.length > 0);
    }

    /**
     * 测试场景：获取状态列表 - 成功获取
     * 前置条件：系统中存在预设状态
     * 输入数据：无需输入
     * 预期结果：返回成功响应和状态列表
     */
    @Test
    @DisplayName("获取状态列表 - 成功")
    void testGetStatusList_Success() {
        // Act
        Map<String, Object> response = userController.getStatusList();

        // Assert
        assertTrue((Boolean) response.get("success"));
        assertNotNull(response.get("statuses"));
        
        String[] statuses = (String[]) response.get("statuses");
        assertTrue(statuses.length > 0);
    }
}