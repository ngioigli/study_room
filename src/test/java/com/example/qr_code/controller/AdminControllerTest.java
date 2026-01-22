package com.example.qr_code.controller;

import com.example.qr_code.entity.Seat;
import com.example.qr_code.entity.User;
import com.example.qr_code.service.AdminService;
import com.example.qr_code.service.SeatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * AdminController 单元测试类
 * 测试管理员功能相关接口：
 * 1. 权限验证
 * 2. 用户管理
 * 3. 座位管理
 * 4. 数据统计
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AdminController 单元测试")
class AdminControllerTest {

    @InjectMocks
    private AdminController adminController;

    @Mock
    private AdminService adminService;

    @Mock
    private SeatService seatService;

    private MockHttpSession session;
    private User adminUser;
    private User normalUser;
    private Seat testSeat;

    @BeforeEach
    void setUp() {
        session = new MockHttpSession();
        
        // 创建管理员用户
        adminUser = new User();
        adminUser.setId(1L);
        adminUser.setUsername("admin");
        adminUser.setRole("admin");
        adminUser.setStatus(1); // Integer类型
        
        // 创建普通用户
        normalUser = new User();
        normalUser.setId(2L);
        normalUser.setUsername("testuser");
        normalUser.setRole("user");
        normalUser.setStatus(1); // Integer类型
        
        // 创建测试座位
        testSeat = new Seat();
        testSeat.setId(1L);
        testSeat.setSeatNumber("A01");
        testSeat.setStatus("available");
        testSeat.setPowerOn(1); // Integer类型，1表示通电
    }

    // ==================== 权限验证测试 ====================

    /**
     * 测试管理员权限检查 - 成功
     * 前置条件：用户已登录且是管理员
     * 输入数据：有效的管理员会话
     * 预期结果：返回管理员信息
     */
    @Test
    @DisplayName("管理员权限检查 - 成功")
    void testCheckAdmin_Success() {
        // Arrange
        session.setAttribute("user", adminUser);

        // Act
        Map<String, Object> response = adminController.checkAdmin(session);

        // Assert
        assertTrue((Boolean) response.get("success"));
        assertTrue((Boolean) response.get("isAdmin"));
        assertEquals(adminUser.getUsername(), response.get("username"));
    }

    /**
     * 测试管理员权限检查 - 普通用户
     * 前置条件：用户已登录但不是管理员
     * 输入数据：普通用户会话
     * 预期结果：返回非管理员状态
     */
    @Test
    @DisplayName("管理员权限检查 - 普通用户")
    void testCheckAdmin_NormalUser() {
        // Arrange
        session.setAttribute("user", normalUser);

        // Act
        Map<String, Object> response = adminController.checkAdmin(session);

        // Assert
        assertTrue((Boolean) response.get("success"));
        assertFalse((Boolean) response.get("isAdmin"));
        assertEquals(normalUser.getUsername(), response.get("username"));
    }

    /**
     * 测试管理员权限检查 - 用户未登录
     * 前置条件：用户未登录
     * 输入数据：空的用户会话
     * 预期结果：返回登录提示
     */
    @Test
    @DisplayName("管理员权限检查 - 用户未登录")
    void testCheckAdmin_NotLoggedIn() {
        // Arrange - 不设置用户会话

        // Act
        Map<String, Object> response = adminController.checkAdmin(session);

        // Assert
        assertFalse((Boolean) response.get("success"));
        assertFalse((Boolean) response.get("isAdmin"));
        assertEquals("请先登录", response.get("message"));
    }

    // ==================== 用户管理测试 ====================

    /**
     * 测试获取所有用户 - 成功（不含统计）
     * 前置条件：管理员已登录
     * 输入数据：withStats=false
     * 预期结果：返回用户列表和统计信息
     */
    @Test
    @DisplayName("获取所有用户 - 成功（不含统计）")
    void testGetAllUsers_Success_WithoutStats() {
        // Arrange
        session.setAttribute("user", adminUser);
        List<User> users = Arrays.asList(adminUser, normalUser);
        Map<String, Integer> stats = new HashMap<>();
        stats.put("totalUsers", 2);
        stats.put("activeUsers", 1);
        
        when(adminService.getAllUsers()).thenReturn(users);
        when(adminService.getUserStats()).thenReturn(stats);

        // Act
        Map<String, Object> response = adminController.getAllUsers(false, session);

        // Assert
        assertTrue((Boolean) response.get("success"));
        assertEquals(users, response.get("users"));
        assertEquals(stats, response.get("stats"));
        
        verify(adminService, times(1)).getAllUsers();
        verify(adminService, times(1)).getUserStats();
        verify(adminService, never()).getAllUsersWithStats();
    }

    /**
     * 测试获取所有用户 - 成功（含统计）
     * 前置条件：管理员已登录
     * 输入数据：withStats=true
     * 预期结果：返回带统计的用户列表
     */
    @Test
    @DisplayName("获取所有用户 - 成功（含统计）")
    void testGetAllUsers_Success_WithStats() {
        // Arrange
        session.setAttribute("user", adminUser);
        List<Map<String, Object>> usersWithStats = Arrays.asList(
                createUserWithStats(adminUser, 10, 3600),
                createUserWithStats(normalUser, 5, 1800)
        );
        
        when(adminService.getAllUsersWithStats()).thenReturn(usersWithStats);

        // Act
        Map<String, Object> response = adminController.getAllUsers(true, session);

        // Assert
        assertTrue((Boolean) response.get("success"));
        assertEquals(usersWithStats, response.get("users"));
        
        verify(adminService, times(1)).getAllUsersWithStats();
        verify(adminService, never()).getAllUsers();
        verify(adminService, never()).getUserStats();
    }

    /**
     * 测试获取所有用户 - 权限不足
     * 前置条件：普通用户已登录
     * 输入数据：有效请求
     * 预期结果：返回权限不足提示
     */
    @Test
    @DisplayName("获取所有用户 - 权限不足")
    void testGetAllUsers_Unauthorized() {
        // Arrange
        session.setAttribute("user", normalUser);

        // Act
        Map<String, Object> response = adminController.getAllUsers(false, session);

        // Assert
        assertFalse((Boolean) response.get("success"));
        assertEquals("需要管理员权限", response.get("message"));
        
        verify(adminService, never()).getAllUsers();
        verify(adminService, never()).getAllUsersWithStats();
    }

    /**
     * 测试获取所有用户 - 服务异常
     * 前置条件：管理员已登录
     * 输入数据：有效请求
     * 预期结果：返回错误信息
     */
    @Test
    @DisplayName("获取所有用户 - 服务异常")
    void testGetAllUsers_ServiceException() {
        // Arrange
        session.setAttribute("user", adminUser);
        when(adminService.getAllUsers()).thenThrow(new RuntimeException("数据库连接失败"));

        // Act
        Map<String, Object> response = adminController.getAllUsers(false, session);

        // Assert
        assertFalse((Boolean) response.get("success"));
        assertEquals("获取用户列表失败: 数据库连接失败", response.get("message"));
        
        verify(adminService, times(1)).getAllUsers();
    }

    /**
     * 测试获取用户详情 - 成功
     * 前置条件：管理员已登录
     * 输入数据：有效的用户ID
     * 预期结果：返回用户详细统计
     */
    @Test
    @DisplayName("获取用户详情 - 成功")
    void testGetUserDetail_Success() {
        // Arrange
        session.setAttribute("user", adminUser);
        Long userId = 2L;
        Map<String, Object> userDetail = createUserDetailStats(normalUser);
        
        when(adminService.getUserDetailStats(userId)).thenReturn(userDetail);

        // Act
        Map<String, Object> response = adminController.getUserDetail(userId, session);

        // Assert
        assertTrue((Boolean) response.get("success"));
        assertEquals(userDetail, response.get("user"));
        
        verify(adminService, times(1)).getUserDetailStats(userId);
    }

    /**
     * 测试获取用户详情 - 权限不足
     * 前置条件：普通用户已登录
     * 输入数据：有效的用户ID
     * 预期结果：返回权限不足提示
     */
    @Test
    @DisplayName("获取用户详情 - 权限不足")
    void testGetUserDetail_Unauthorized() {
        // Arrange
        session.setAttribute("user", normalUser);
        Long userId = 2L;

        // Act
        Map<String, Object> response = adminController.getUserDetail(userId, session);

        // Assert
        assertFalse((Boolean) response.get("success"));
        assertEquals("需要管理员权限", response.get("message"));
        
        verify(adminService, never()).getUserDetailStats(anyLong());
    }

    /**
     * 测试更新用户状态 - 成功（启用）
     * 前置条件：管理员已登录
     * 输入数据：有效的用户ID和状态
     * 预期结果：成功更新用户状态
     */
    @Test
    @DisplayName("更新用户状态 - 成功（启用）")
    void testUpdateUserStatus_Success_Enable() {
        // Arrange
        session.setAttribute("user", adminUser);
        Long userId = 2L;
        Map<String, Object> body = new HashMap<>();
        body.put("status", 1);
        
        User updatedUser = new User();
        updatedUser.setId(userId);
        updatedUser.setStatus(1);
        
        when(adminService.updateUserStatus(userId, 1)).thenReturn(updatedUser);

        // Act
        Map<String, Object> response = adminController.updateUserStatus(userId, body, session);

        // Assert
        assertTrue((Boolean) response.get("success"));
        assertEquals(updatedUser, response.get("user"));
        assertEquals("用户已启用", response.get("message"));
        
        verify(adminService, times(1)).updateUserStatus(userId, 1);
    }

    /**
     * 测试更新用户状态 - 成功（禁用）
     * 前置条件：管理员已登录
     * 输入数据：有效的用户ID和状态
     * 预期结果：成功更新用户状态
     */
    @Test
    @DisplayName("更新用户状态 - 成功（禁用）")
    void testUpdateUserStatus_Success_Disable() {
        // Arrange
        session.setAttribute("user", adminUser);
        Long userId = 2L;
        Map<String, Object> body = new HashMap<>();
        body.put("status", 0);
        
        User updatedUser = new User();
        updatedUser.setId(userId);
        updatedUser.setStatus(0);
        
        when(adminService.updateUserStatus(userId, 0)).thenReturn(updatedUser);

        // Act
        Map<String, Object> response = adminController.updateUserStatus(userId, body, session);

        // Assert
        assertTrue((Boolean) response.get("success"));
        assertEquals(updatedUser, response.get("user"));
        assertEquals("用户已禁用", response.get("message"));
        
        verify(adminService, times(1)).updateUserStatus(userId, 0);
    }

    /**
     * 测试更新用户状态 - 状态参数为空
     * 前置条件：管理员已登录
     * 输入数据：空的状态参数
     * 预期结果：返回参数错误提示
     */
    @Test
    @DisplayName("更新用户状态 - 状态参数为空")
    void testUpdateUserStatus_NullStatus() {
        // Arrange
        session.setAttribute("user", adminUser);
        Long userId = 2L;
        Map<String, Object> body = new HashMap<>();
        // 不设置status参数

        // Act
        Map<String, Object> response = adminController.updateUserStatus(userId, body, session);

        // Assert
        assertFalse((Boolean) response.get("success"));
        assertEquals("请指定状态", response.get("message"));
        
        verify(adminService, never()).updateUserStatus(anyLong(), anyInt());
    }

    /**
     * 测试更新用户角色 - 成功
     * 前置条件：管理员已登录
     * 输入数据：有效的用户ID和角色
     * 预期结果：成功更新用户角色
     */
    @Test
    @DisplayName("更新用户角色 - 成功")
    void testUpdateUserRole_Success() {
        // Arrange
        session.setAttribute("user", adminUser);
        Long userId = 2L;
        Map<String, Object> body = new HashMap<>();
        body.put("role", "admin");
        
        User updatedUser = new User();
        updatedUser.setId(userId);
        updatedUser.setRole("admin");
        
        when(adminService.updateUserRole(userId, "admin")).thenReturn(updatedUser);

        // Act
        Map<String, Object> response = adminController.updateUserRole(userId, body, session);

        // Assert
        assertTrue((Boolean) response.get("success"));
        assertEquals(updatedUser, response.get("user"));
        assertEquals("角色已更新", response.get("message"));
        
        verify(adminService, times(1)).updateUserRole(userId, "admin");
    }

    /**
     * 测试更新用户角色 - 角色参数为空
     * 前置条件：管理员已登录
     * 输入数据：空的角色参数
     * 预期结果：返回参数错误提示
     */
    @Test
    @DisplayName("更新用户角色 - 角色参数为空")
    void testUpdateUserRole_NullRole() {
        // Arrange
        session.setAttribute("user", adminUser);
        Long userId = 2L;
        Map<String, Object> body = new HashMap<>();
        // 不设置role参数

        // Act
        Map<String, Object> response = adminController.updateUserRole(userId, body, session);

        // Assert
        assertFalse((Boolean) response.get("success"));
        assertEquals("请指定角色", response.get("message"));
        
        verify(adminService, never()).updateUserRole(anyLong(), anyString());
    }

    // ==================== 座位管理测试 ====================

    /**
     * 测试获取所有座位 - 成功
     * 前置条件：管理员已登录
     * 输入数据：有效请求
     * 预期结果：返回座位列表和统计信息
     */
    @Test
    @DisplayName("获取所有座位 - 成功")
    void testGetAllSeats_Success() {
        // Arrange
        session.setAttribute("user", adminUser);
        List<Map<String, Object>> seats = Arrays.asList(createSeatWithUser(testSeat, normalUser));
        SeatService.SeatStats stats = new SeatService.SeatStats(8, 6, 2, 0, 0);
        
        when(adminService.getAllSeatsWithUsers()).thenReturn(seats);
        when(seatService.getSeatStats()).thenReturn(stats);

        // Act
        Map<String, Object> response = adminController.getAllSeats(session);

        // Assert
        assertTrue((Boolean) response.get("success"));
        assertEquals(seats, response.get("seats"));
        assertEquals(stats, response.get("stats"));
        
        verify(adminService, times(1)).getAllSeatsWithUsers();
        verify(seatService, times(1)).getSeatStats();
    }

    /**
     * 测试获取所有座位 - 权限不足
     * 前置条件：普通用户已登录
     * 输入数据：有效请求
     * 预期结果：返回权限不足提示
     */
    @Test
    @DisplayName("获取所有座位 - 权限不足")
    void testGetAllSeats_Unauthorized() {
        // Arrange
        session.setAttribute("user", normalUser);

        // Act
        Map<String, Object> response = adminController.getAllSeats(session);

        // Assert
        assertFalse((Boolean) response.get("success"));
        assertEquals("需要管理员权限", response.get("message"));
        
        verify(adminService, never()).getAllSeatsWithUsers();
        verify(seatService, never()).getSeatStats();
    }

    /**
     * 测试强制释放座位 - 成功
     * 前置条件：管理员已登录
     * 输入数据：有效的座位ID
     * 预期结果：成功释放座位
     */
    @Test
    @DisplayName("强制释放座位 - 成功")
    void testForceReleaseSeat_Success() {
        // Arrange
        session.setAttribute("user", adminUser);
        Long seatId = 1L;
        
        doNothing().when(adminService).forceReleaseSeat(seatId);

        // Act
        Map<String, Object> response = adminController.forceReleaseSeat(seatId, session);

        // Assert
        assertTrue((Boolean) response.get("success"));
        assertEquals("座位已强制释放", response.get("message"));
        
        verify(adminService, times(1)).forceReleaseSeat(seatId);
    }

    /**
     * 测试设置座位维护状态 - 成功（维护中）
     * 前置条件：管理员已登录
     * 输入数据：有效的座位ID和维护状态
     * 预期结果：成功设置维护状态
     */
    @Test
    @DisplayName("设置座位维护状态 - 成功（维护中）")
    void testSetSeatMaintenance_Success_Maintenance() {
        // Arrange
        session.setAttribute("user", adminUser);
        Long seatId = 1L;
        Map<String, Object> body = new HashMap<>();
        body.put("maintenance", true);
        
        Seat updatedSeat = new Seat();
        updatedSeat.setId(seatId);
        updatedSeat.setStatus("maintenance");
        
        when(adminService.setSeatMaintenance(seatId, true)).thenReturn(updatedSeat);

        // Act
        Map<String, Object> response = adminController.setSeatMaintenance(seatId, body, session);

        // Assert
        assertTrue((Boolean) response.get("success"));
        assertEquals(updatedSeat, response.get("seat"));
        assertEquals("座位已设为维护中", response.get("message"));
        
        verify(adminService, times(1)).setSeatMaintenance(seatId, true);
    }

    /**
     * 测试设置座位维护状态 - 维护参数为空
     * 前置条件：管理员已登录
     * 输入数据：空的维护参数
     * 预期结果：返回参数错误提示
     */
    @Test
    @DisplayName("设置座位维护状态 - 维护参数为空")
    void testSetSeatMaintenance_NullMaintenance() {
        // Arrange
        session.setAttribute("user", adminUser);
        Long seatId = 1L;
        Map<String, Object> body = new HashMap<>();
        // 不设置maintenance参数

        // Act
        Map<String, Object> response = adminController.setSeatMaintenance(seatId, body, session);

        // Assert
        assertFalse((Boolean) response.get("success"));
        assertEquals("请指定维护状态", response.get("message"));
        
        verify(adminService, never()).setSeatMaintenance(anyLong(), anyBoolean());
    }

    /**
     * 测试控制座位电源 - 成功（通电）
     * 前置条件：管理员已登录
     * 输入数据：有效的座位ID和电源状态
     * 预期结果：成功控制座位电源
     */
    @Test
    @DisplayName("控制座位电源 - 成功（通电）")
    void testControlPower_Success_PowerOn() {
        // Arrange
        session.setAttribute("user", adminUser);
        Long seatId = 1L;
        Map<String, Object> body = new HashMap<>();
        body.put("powerOn", true);
        
        Seat updatedSeat = new Seat();
        updatedSeat.setId(seatId);
        updatedSeat.setPowerOn(1); // Integer类型，1表示通电
        
        when(seatService.controlPower(seatId, true)).thenReturn(updatedSeat);

        // Act
        Map<String, Object> response = adminController.controlPower(seatId, body, session);

        // Assert
        assertTrue((Boolean) response.get("success"));
        assertEquals(updatedSeat, response.get("seat"));
        assertEquals("座位已通电", response.get("message"));
        
        verify(seatService, times(1)).controlPower(seatId, true);
    }

    /**
     * 测试控制座位电源 - 电源参数为空
     * 前置条件：管理员已登录
     * 输入数据：空的电源参数
     * 预期结果：返回参数错误提示
     */
    @Test
    @DisplayName("控制座位电源 - 电源参数为空")
    void testControlPower_NullPowerOn() {
        // Arrange
        session.setAttribute("user", adminUser);
        Long seatId = 1L;
        Map<String, Object> body = new HashMap<>();
        // 不设置powerOn参数

        // Act
        Map<String, Object> response = adminController.controlPower(seatId, body, session);

        // Assert
        assertFalse((Boolean) response.get("success"));
        assertEquals("请指定电源状态", response.get("message"));
        
        verify(seatService, never()).controlPower(anyLong(), anyBoolean());
    }

    // ==================== 数据统计测试 ====================

    /**
     * 测试获取今日概览 - 成功
     * 前置条件：管理员已登录
     * 输入数据：有效请求
     * 预期结果：返回今日概览数据
     */
    @Test
    @DisplayName("获取今日概览 - 成功")
    void testGetTodayOverview_Success() {
        // Arrange
        session.setAttribute("user", adminUser);
        Map<String, Object> overview = new HashMap<>();
        overview.put("totalUsers", 100);
        overview.put("activeUsers", 80);
        overview.put("totalFocusTime", 7200);
        overview.put("totalFocusCount", 150);
        
        when(adminService.getTodayOverview()).thenReturn(overview);

        // Act
        Map<String, Object> response = adminController.getTodayOverview(session);

        // Assert
        assertTrue((Boolean) response.get("success"));
        assertEquals(100, response.get("totalUsers"));
        assertEquals(80, response.get("activeUsers"));
        assertEquals(7200, response.get("totalFocusTime"));
        assertEquals(150, response.get("totalFocusCount"));
        
        verify(adminService, times(1)).getTodayOverview();
    }

    /**
     * 测试获取今日概览 - 权限不足
     * 前置条件：普通用户已登录
     * 输入数据：有效请求
     * 预期结果：返回权限不足提示
     */
    @Test
    @DisplayName("获取今日概览 - 权限不足")
    void testGetTodayOverview_Unauthorized() {
        // Arrange
        session.setAttribute("user", normalUser);

        // Act
        Map<String, Object> response = adminController.getTodayOverview(session);

        // Assert
        assertFalse((Boolean) response.get("success"));
        assertEquals("需要管理员权限", response.get("message"));
        
        verify(adminService, never()).getTodayOverview();
    }

    /**
     * 测试获取周统计 - 成功
     * 前置条件：管理员已登录
     * 输入数据：有效请求
     * 预期结果：返回最近7天统计数据
     */
    @Test
    @DisplayName("获取周统计 - 成功")
    void testGetWeeklyStats_Success() {
        // Arrange
        session.setAttribute("user", adminUser);
        List<Map<String, Object>> stats = Arrays.asList(
                createDailyStats("2026-01-21", 50, 3600),
                createDailyStats("2026-01-20", 45, 3200)
        );
        
        when(adminService.getWeeklyStats()).thenReturn(stats);

        // Act
        Map<String, Object> response = adminController.getWeeklyStats(session);

        // Assert
        assertTrue((Boolean) response.get("success"));
        assertEquals(stats, response.get("stats"));
        
        verify(adminService, times(1)).getWeeklyStats();
    }

    /**
     * 测试获取周统计 - 权限不足
     * 前置条件：普通用户已登录
     * 输入数据：有效请求
     * 预期结果：返回权限不足提示
     */
    @Test
    @DisplayName("获取周统计 - 权限不足")
    void testGetWeeklyStats_Unauthorized() {
        // Arrange
        session.setAttribute("user", normalUser);

        // Act
        Map<String, Object> response = adminController.getWeeklyStats(session);

        // Assert
        assertFalse((Boolean) response.get("success"));
        assertEquals("需要管理员权限", response.get("message"));
        
        verify(adminService, never()).getWeeklyStats();
    }

    /**
     * 测试获取用户排行榜 - 成功（默认限制）
     * 前置条件：管理员已登录
     * 输入数据：不指定limit参数
     * 预期结果：返回前10名用户排行榜
     */
    @Test
    @DisplayName("获取用户排行榜 - 成功（默认限制）")
    void testGetUserRanking_Success_DefaultLimit() {
        // Arrange
        session.setAttribute("user", adminUser);
        List<Map<String, Object>> ranking = Arrays.asList(
                createRankingUser(1, adminUser, 10, 7200),
                createRankingUser(2, normalUser, 8, 5400)
        );
        
        when(adminService.getUserRanking(10)).thenReturn(ranking);

        // Act
        Map<String, Object> response = adminController.getUserRanking(10, session);

        // Assert
        assertTrue((Boolean) response.get("success"));
        assertEquals(ranking, response.get("ranking"));
        
        verify(adminService, times(1)).getUserRanking(10);
    }

    /**
     * 测试获取用户排行榜 - 成功（自定义限制）
     * 前置条件：管理员已登录
     * 输入数据：指定limit=5
     * 预期结果：返回前5名用户排行榜
     */
    @Test
    @DisplayName("获取用户排行榜 - 成功（自定义限制）")
    void testGetUserRanking_Success_CustomLimit() {
        // Arrange
        session.setAttribute("user", adminUser);
        List<Map<String, Object>> ranking = Arrays.asList(
                createRankingUser(1, adminUser, 10, 7200)
        );
        
        when(adminService.getUserRanking(5)).thenReturn(ranking);

        // Act
        Map<String, Object> response = adminController.getUserRanking(5, session);

        // Assert
        assertTrue((Boolean) response.get("success"));
        assertEquals(ranking, response.get("ranking"));
        
        verify(adminService, times(1)).getUserRanking(5);
    }

    /**
     * 测试获取用户排行榜 - 权限不足
     * 前置条件：普通用户已登录
     * 输入数据：有效请求
     * 预期结果：返回权限不足提示
     */
    @Test
    @DisplayName("获取用户排行榜 - 权限不足")
    void testGetUserRanking_Unauthorized() {
        // Arrange
        session.setAttribute("user", normalUser);

        // Act
        Map<String, Object> response = adminController.getUserRanking(10, session);

        // Assert
        assertFalse((Boolean) response.get("success"));
        assertEquals("需要管理员权限", response.get("message"));
        
        verify(adminService, never()).getUserRanking(anyInt());
    }

    /**
     * 测试获取今日概览 - 服务异常
     * 前置条件：管理员已登录
     * 输入数据：有效请求
     * 预期结果：返回错误信息
     */
    @Test
    @DisplayName("获取今日概览 - 服务异常")
    void testGetTodayOverview_ServiceException() {
        // Arrange
        session.setAttribute("user", adminUser);
        when(adminService.getTodayOverview()).thenThrow(new RuntimeException("数据库连接失败"));

        // Act
        Map<String, Object> response = adminController.getTodayOverview(session);

        // Assert
        assertFalse((Boolean) response.get("success"));
        assertEquals("获取概览数据失败: 数据库连接失败", response.get("message"));
        
        verify(adminService, times(1)).getTodayOverview();
    }

    /**
     * 测试获取周统计 - 服务异常
     * 前置条件：管理员已登录
     * 输入数据：有效请求
     * 预期结果：返回错误信息
     */
    @Test
    @DisplayName("获取周统计 - 服务异常")
    void testGetWeeklyStats_ServiceException() {
        // Arrange
        session.setAttribute("user", adminUser);
        when(adminService.getWeeklyStats()).thenThrow(new RuntimeException("数据查询失败"));

        // Act
        Map<String, Object> response = adminController.getWeeklyStats(session);

        // Assert
        assertFalse((Boolean) response.get("success"));
        assertEquals("获取周统计失败: 数据查询失败", response.get("message"));
        
        verify(adminService, times(1)).getWeeklyStats();
    }

    /**
     * 测试获取用户排行榜 - 服务异常
     * 前置条件：管理员已登录
     * 输入数据：有效请求
     * 预期结果：返回错误信息
     */
    @Test
    @DisplayName("获取用户排行榜 - 服务异常")
    void testGetUserRanking_ServiceException() {
        // Arrange
        session.setAttribute("user", adminUser);
        when(adminService.getUserRanking(10)).thenThrow(new RuntimeException("排行榜计算失败"));

        // Act
        Map<String, Object> response = adminController.getUserRanking(10, session);

        // Assert
        assertFalse((Boolean) response.get("success"));
        assertEquals("获取排行榜失败: 排行榜计算失败", response.get("message"));
        
        verify(adminService, times(1)).getUserRanking(10);
    }

    // ==================== 辅助方法 ====================

    private Map<String, Object> createUserWithStats(User user, int focusCount, int totalDuration) {
        Map<String, Object> userWithStats = new HashMap<>();
        userWithStats.put("id", user.getId());
        userWithStats.put("username", user.getUsername());
        userWithStats.put("role", user.getRole());
        userWithStats.put("todayFocusCount", focusCount);
        userWithStats.put("todayDuration", totalDuration);
        return userWithStats;
    }

    private Map<String, Object> createUserDetailStats(User user) {
        Map<String, Object> detail = new HashMap<>();
        detail.put("id", user.getId());
        detail.put("username", user.getUsername());
        detail.put("todayStats", new HashMap<>());
        detail.put("weekStats", new HashMap<>());
        detail.put("monthStats", new HashMap<>());
        return detail;
    }

    private Map<String, Object> createSeatWithUser(Seat seat, User user) {
        Map<String, Object> seatWithUser = new HashMap<>();
        seatWithUser.put("id", seat.getId());
        seatWithUser.put("seatNumber", seat.getSeatNumber());
        seatWithUser.put("status", seat.getStatus());
        seatWithUser.put("powerOn", seat.getPowerOn());
        if (user != null) {
            seatWithUser.put("currentUser", user.getUsername());
        }
        return seatWithUser;
    }

    private Map<String, Object> createDailyStats(String date, int focusCount, int totalDuration) {
        Map<String, Object> dailyStats = new HashMap<>();
        dailyStats.put("date", date);
        dailyStats.put("focusCount", focusCount);
        dailyStats.put("totalDuration", totalDuration);
        return dailyStats;
    }

    private Map<String, Object> createRankingUser(int rank, User user, int focusCount, int totalDuration) {
        Map<String, Object> rankingUser = new HashMap<>();
        rankingUser.put("rank", rank);
        rankingUser.put("userId", user.getId());
        rankingUser.put("username", user.getUsername());
        rankingUser.put("focusCount", focusCount);
        rankingUser.put("totalDuration", totalDuration);
        return rankingUser;
    }
}