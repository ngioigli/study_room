package com.example.qr_code.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.qr_code.entity.User;
import com.example.qr_code.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpSession;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * LoginController 单元测试
 * 测试用户登录相关接口的功能
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("登录控制器测试")
class LoginControllerTest {

    @InjectMocks
    private LoginController loginController;

    @Mock
    private UserMapper userMapper;

    private MockHttpSession session;
    private User testUser;

    @BeforeEach
    void setUp() {
        session = new MockHttpSession();
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("password123");
        testUser.setNickname("测试用户");
        testUser.setStatus(1); // 正常状态
        testUser.setRole("user");
    }

    /**
     * 测试场景：用户登录 - 成功登录
     * 前置条件：用户名和密码正确，用户状态正常
     * 输入数据：有效的用户名和密码
     * 预期结果：返回成功响应，用户信息存入会话
     */
    @Test
    @DisplayName("用户登录 - 成功")
    void testLogin_Success() {
        // Arrange
        Map<String, String> loginData = new HashMap<>();
        loginData.put("username", "testuser");
        loginData.put("password", "password123");
        
        when(userMapper.selectOne(any(QueryWrapper.class))).thenReturn(testUser);

        // Act
        ResponseEntity<Map<String, Object>> response = loginController.login(loginData, session);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertTrue((Boolean) body.get("success"));
        assertTrue(((String) body.get("message")).contains("欢迎回来"));
        assertEquals("/index.html", body.get("redirectUrl"));
        
        // 验证用户信息已存入会话
        User sessionUser = (User) session.getAttribute("user");
        assertNotNull(sessionUser);
        assertEquals(testUser.getId(), sessionUser.getId());
        
        verify(userMapper, times(1)).selectOne(any(QueryWrapper.class));
    }

    /**
     * 测试场景：用户登录 - 用户不存在
     * 前置条件：输入不存在的用户名
     * 输入数据：不存在的用户名
     * 预期结果：返回失败响应和错误信息
     */
    @Test
    @DisplayName("用户登录 - 用户不存在")
    void testLogin_UserNotFound() {
        // Arrange
        Map<String, String> loginData = new HashMap<>();
        loginData.put("username", "nonexistent");
        loginData.put("password", "password123");
        
        when(userMapper.selectOne(any(QueryWrapper.class))).thenReturn(null);

        // Act
        ResponseEntity<Map<String, Object>> response = loginController.login(loginData, session);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertFalse((Boolean) body.get("success"));
        assertEquals("账号或密码错误", body.get("message"));
        
        // 验证会话中没有用户信息
        assertNull(session.getAttribute("user"));
        
        verify(userMapper, times(1)).selectOne(any(QueryWrapper.class));
    }

    /**
     * 测试场景：用户登录 - 密码错误
     * 前置条件：用户名正确但密码错误
     * 输入数据：正确的用户名和错误的密码
     * 预期结果：返回失败响应和错误信息
     */
    @Test
    @DisplayName("用户登录 - 密码错误")
    void testLogin_WrongPassword() {
        // Arrange
        Map<String, String> loginData = new HashMap<>();
        loginData.put("username", "testuser");
        loginData.put("password", "wrongpassword");
        
        when(userMapper.selectOne(any(QueryWrapper.class))).thenReturn(testUser);

        // Act
        ResponseEntity<Map<String, Object>> response = loginController.login(loginData, session);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertFalse((Boolean) body.get("success"));
        assertEquals("账号或密码错误", body.get("message"));
        
        // 验证会话中没有用户信息
        assertNull(session.getAttribute("user"));
        
        verify(userMapper, times(1)).selectOne(any(QueryWrapper.class));
    }

    /**
     * 测试场景：用户登录 - 用户被禁用
     * 前置条件：用户名和密码正确，但用户状态为禁用
     * 输入数据：有效的用户名和密码
     * 预期结果：返回失败响应和禁用提示
     */
    @Test
    @DisplayName("用户登录 - 用户被禁用")
    void testLogin_UserDisabled() {
        // Arrange
        testUser.setStatus(0); // 禁用状态
        
        Map<String, String> loginData = new HashMap<>();
        loginData.put("username", "testuser");
        loginData.put("password", "password123");
        
        when(userMapper.selectOne(any(QueryWrapper.class))).thenReturn(testUser);

        // Act
        ResponseEntity<Map<String, Object>> response = loginController.login(loginData, session);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertFalse((Boolean) body.get("success"));
        assertEquals("您的账号已被禁用，请联系管理员", body.get("message"));
        
        // 验证会话中没有用户信息
        assertNull(session.getAttribute("user"));
        
        verify(userMapper, times(1)).selectOne(any(QueryWrapper.class));
    }

    /**
     * 测试场景：管理员登录 - 跳转到管理后台
     * 前置条件：管理员用户登录成功
     * 输入数据：管理员的用户名和密码
     * 预期结果：返回成功响应，跳转到管理后台
     */
    @Test
    @DisplayName("管理员登录 - 跳转到管理后台")
    void testLogin_AdminUser() {
        // Arrange
        testUser.setRole("admin");
        
        Map<String, String> loginData = new HashMap<>();
        loginData.put("username", "admin");
        loginData.put("password", "password123");
        
        when(userMapper.selectOne(any(QueryWrapper.class))).thenReturn(testUser);

        // Act
        ResponseEntity<Map<String, Object>> response = loginController.login(loginData, session);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertTrue((Boolean) body.get("success"));
        assertEquals("/admin.html", body.get("redirectUrl"));
        
        verify(userMapper, times(1)).selectOne(any(QueryWrapper.class));
    }

    /**
     * 测试场景：用户登出 - POST 方式
     * 前置条件：用户已登录
     * 输入数据：有效的用户会话
     * 预期结果：清除会话，返回成功响应
     */
    @Test
    @DisplayName("用户登出 - POST方式")
    void testLogoutPost_Success() {
        // Arrange
        session.setAttribute("user", testUser);

        // Act
        ResponseEntity<Map<String, Object>> response = loginController.logoutPost(session);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertTrue((Boolean) body.get("success"));
        assertEquals("已退出登录", body.get("message"));
        
        // 验证会话已失效
        assertTrue(session.isInvalid());
    }

    /**
     * 测试场景：获取用户信息 - 用户已登录
     * 前置条件：用户已登录
     * 输入数据：有效的用户会话
     * 预期结果：返回成功响应和用户信息
     */
    @Test
    @DisplayName("获取用户信息 - 成功")
    void testGetUserInfo_Success() {
        // Arrange
        session.setAttribute("user", testUser);

        // Act
        Map<String, Object> response = loginController.getUserInfo(session);

        // Assert
        assertTrue((Boolean) response.get("success"));
        assertNotNull(response.get("user"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> userInfo = (Map<String, Object>) response.get("user");
        assertEquals(testUser.getId(), userInfo.get("id"));
        assertEquals(testUser.getUsername(), userInfo.get("username"));
        assertEquals(testUser.getNickname(), userInfo.get("nickname"));
        assertEquals(testUser.getRole(), userInfo.get("role"));
    }

    /**
     * 测试场景：获取用户信息 - 用户未登录
     * 前置条件：用户未登录
     * 输入数据：空的用户会话
     * 预期结果：返回失败响应和登录提示
     */
    @Test
    @DisplayName("获取用户信息 - 用户未登录")
    void testGetUserInfo_NotLoggedIn() {
        // Act
        Map<String, Object> response = loginController.getUserInfo(session);

        // Assert
        assertFalse((Boolean) response.get("success"));
        assertEquals("请先登录", response.get("message"));
        assertNull(response.get("user"));
    }
}