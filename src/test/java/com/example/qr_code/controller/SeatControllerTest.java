package com.example.qr_code.controller;

import com.example.qr_code.entity.Seat;
import com.example.qr_code.entity.User;
import com.example.qr_code.service.SeatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpSession;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * SeatController 单元测试
 * 测试座位管理功能
 */
public class SeatControllerTest {

    @Mock
    private SeatService seatService;

    @InjectMocks
    private SeatController seatController;

    private MockHttpSession session;
    private User testUser;
    private Seat testSeat;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        session = new MockHttpSession();
        
        // 创建测试用户
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        
        // 创建测试座位
        testSeat = new Seat();
        testSeat.setId(1L);
        testSeat.setSeatNumber("A01");
        testSeat.setStatus("available");
        testSeat.setPowerOn(0);
    }

    // 测试场景：获取所有座位列表 - 成功
    // 前置条件：数据库中存在座位数据
    // 输入数据：无
    // 预期结果：返回所有座位和统计信息
    @Test
    void testGetAllSeats_Success() {
        List<Seat> mockSeats = new ArrayList<>();
        mockSeats.add(testSeat);
        
        SeatService.SeatStats mockStats = new SeatService.SeatStats(8, 6, 2, 0, 0);
        
        when(seatService.getAllSeats()).thenReturn(mockSeats);
        when(seatService.getSeatStats()).thenReturn(mockStats);

        Map<String, Object> response = seatController.getAllSeats();

        assertTrue((Boolean) response.get("success"));
        assertNotNull(response.get("seats"));
        assertNotNull(response.get("stats"));
        assertEquals(1, ((List<?>) response.get("seats")).size());
        verify(seatService, times(1)).getAllSeats();
        verify(seatService, times(1)).getSeatStats();
    }

    // 测试场景：获取所有座位列表 - 异常处理
    // 前置条件：Service抛出异常
    // 输入数据：无
    // 预期结果：返回错误信息
    @Test
    void testGetAllSeats_Exception() {
        when(seatService.getAllSeats()).thenThrow(new RuntimeException("数据库连接失败"));

        Map<String, Object> response = seatController.getAllSeats();

        assertFalse((Boolean) response.get("success"));
        assertTrue(((String) response.get("message")).contains("获取座位列表失败"));
    }

    // 测试场景：获取可用座位列表 - 成功
    // 前置条件：存在可用座位
    // 输入数据：无
    // 预期结果：返回可用座位列表
    @Test
    void testGetAvailableSeats_Success() {
        List<Seat> mockSeats = new ArrayList<>();
        mockSeats.add(testSeat);
        
        when(seatService.getAvailableSeats()).thenReturn(mockSeats);

        Map<String, Object> response = seatController.getAvailableSeats();

        assertTrue((Boolean) response.get("success"));
        assertNotNull(response.get("seats"));
        assertEquals(1, response.get("count"));
        verify(seatService, times(1)).getAvailableSeats();
    }

    // 测试场景：获取可用座位列表 - 无可用座位
    // 前置条件：所有座位均被占用
    // 输入数据：无
    // 预期结果：返回空列表
    @Test
    void testGetAvailableSeats_Empty() {
        when(seatService.getAvailableSeats()).thenReturn(new ArrayList<>());

        Map<String, Object> response = seatController.getAvailableSeats();

        assertTrue((Boolean) response.get("success"));
        assertEquals(0, response.get("count"));
    }

    // 测试场景：根据ID获取座位详情 - 成功
    // 前置条件：座位存在
    // 输入数据：有效的座位ID
    // 预期结果：返回座位详情
    @Test
    void testGetSeatById_Success() {
        when(seatService.getSeatById(1L)).thenReturn(testSeat);

        Map<String, Object> response = seatController.getSeatById(1L);

        assertTrue((Boolean) response.get("success"));
        assertNotNull(response.get("seat"));
        verify(seatService, times(1)).getSeatById(1L);
    }

    // 测试场景：根据ID获取座位详情 - 座位不存在
    // 前置条件：指定ID的座位不存在
    // 输入数据：不存在的座位ID
    // 预期结果：返回"座位不存在"错误
    @Test
    void testGetSeatById_NotFound() {
        when(seatService.getSeatById(999L)).thenReturn(null);

        Map<String, Object> response = seatController.getSeatById(999L);

        assertFalse((Boolean) response.get("success"));
        assertEquals("座位不存在", response.get("message"));
    }

    // 测试场景：根据座位编号获取座位 - 成功
    // 前置条件：座位存在
    // 输入数据：有效的座位编号
    // 预期结果：返回座位详情
    @Test
    void testGetSeatByNumber_Success() {
        when(seatService.getSeatByNumber("A01")).thenReturn(testSeat);

        Map<String, Object> response = seatController.getSeatByNumber("A01");

        assertTrue((Boolean) response.get("success"));
        assertNotNull(response.get("seat"));
        verify(seatService, times(1)).getSeatByNumber("A01");
    }

    // 测试场景：根据座位编号获取座位 - 座位不存在
    // 前置条件：指定编号的座位不存在
    // 输入数据：不存在的座位编号
    // 预期结果：返回"座位不存在"错误
    @Test
    void testGetSeatByNumber_NotFound() {
        when(seatService.getSeatByNumber("Z99")).thenReturn(null);

        Map<String, Object> response = seatController.getSeatByNumber("Z99");

        assertFalse((Boolean) response.get("success"));
        assertEquals("座位不存在", response.get("message"));
    }

    // 测试场景：控制座位电源 - 通电成功
    // 前置条件：用户已登录，座位存在
    // 输入数据：座位ID和powerOn=true
    // 预期结果：座位通电成功
    @Test
    void testControlPower_PowerOn_Success() {
        session.setAttribute("user", testUser);
        
        Seat poweredSeat = new Seat();
        poweredSeat.setId(1L);
        poweredSeat.setPowerOn(1);
        
        when(seatService.controlPower(1L, true)).thenReturn(poweredSeat);

        Map<String, Object> body = new HashMap<>();
        body.put("powerOn", true);

        Map<String, Object> response = seatController.controlPower(1L, body, session);

        assertTrue((Boolean) response.get("success"));
        assertEquals("座位已通电", response.get("message"));
        verify(seatService, times(1)).controlPower(1L, true);
    }

    // 测试场景：控制座位电源 - 断电成功
    // 前置条件：用户已登录，座位存在
    // 输入数据：座位ID和powerOn=false
    // 预期结果：座位断电成功
    @Test
    void testControlPower_PowerOff_Success() {
        session.setAttribute("user", testUser);
        
        when(seatService.controlPower(1L, false)).thenReturn(testSeat);

        Map<String, Object> body = new HashMap<>();
        body.put("powerOn", false);

        Map<String, Object> response = seatController.controlPower(1L, body, session);

        assertTrue((Boolean) response.get("success"));
        assertEquals("座位已断电", response.get("message"));
        verify(seatService, times(1)).controlPower(1L, false);
    }

    // 测试场景：控制座位电源 - 未登录
    // 前置条件：session中无用户信息
    // 输入数据：座位ID和电源状态
    // 预期结果：返回"请先登录"错误
    @Test
    void testControlPower_NotLoggedIn() {
        Map<String, Object> body = new HashMap<>();
        body.put("powerOn", true);

        Map<String, Object> response = seatController.controlPower(1L, body, session);

        assertFalse((Boolean) response.get("success"));
        assertEquals("请先登录", response.get("message"));
        verify(seatService, never()).controlPower(anyLong(), anyBoolean());
    }

    // 测试场景：控制座位电源 - 未指定电源状态
    // 前置条件：用户已登录
    // 输入数据：空的body或powerOn为null
    // 预期结果：返回"请指定电源状态"错误
    @Test
    void testControlPower_NoPowerStatus() {
        session.setAttribute("user", testUser);
        
        Map<String, Object> body = new HashMap<>();

        Map<String, Object> response = seatController.controlPower(1L, body, session);

        assertFalse((Boolean) response.get("success"));
        assertEquals("请指定电源状态", response.get("message"));
    }

    // 测试场景：更新座位状态 - 成功
    // 前置条件：用户已登录
    // 输入数据：座位ID和有效的状态值
    // 预期结果：座位状态更新成功
    @Test
    void testUpdateSeatStatus_Success() {
        session.setAttribute("user", testUser);
        
        Seat updatedSeat = new Seat();
        updatedSeat.setId(1L);
        updatedSeat.setStatus("maintenance");
        
        when(seatService.updateSeatStatus(1L, "maintenance")).thenReturn(updatedSeat);

        Map<String, Object> body = new HashMap<>();
        body.put("status", "maintenance");

        Map<String, Object> response = seatController.updateSeatStatus(1L, body, session);

        assertTrue((Boolean) response.get("success"));
        assertEquals("座位状态已更新", response.get("message"));
        verify(seatService, times(1)).updateSeatStatus(1L, "maintenance");
    }

    // 测试场景：更新座位状态 - 未登录
    // 前置条件：session中无用户信息
    // 输入数据：座位ID和状态值
    // 预期结果：返回"请先登录"错误
    @Test
    void testUpdateSeatStatus_NotLoggedIn() {
        Map<String, Object> body = new HashMap<>();
        body.put("status", "available");

        Map<String, Object> response = seatController.updateSeatStatus(1L, body, session);

        assertFalse((Boolean) response.get("success"));
        assertEquals("请先登录", response.get("message"));
    }

    // 测试场景：更新座位状态 - 未指定状态值
    // 前置条件：用户已登录
    // 输入数据：空的body或status为null
    // 预期结果：返回"请指定座位状态"错误
    @Test
    void testUpdateSeatStatus_NoStatus() {
        session.setAttribute("user", testUser);
        
        Map<String, Object> body = new HashMap<>();

        Map<String, Object> response = seatController.updateSeatStatus(1L, body, session);

        assertFalse((Boolean) response.get("success"));
        assertEquals("请指定座位状态", response.get("message"));
    }

    // 测试场景：更新座位状态 - 无效的状态值
    // 前置条件：用户已登录
    // 输入数据：不符合规则的状态值
    // 预期结果：返回"无效的座位状态"错误
    @Test
    void testUpdateSeatStatus_InvalidStatus() {
        session.setAttribute("user", testUser);
        
        Map<String, Object> body = new HashMap<>();
        body.put("status", "invalid_status");

        Map<String, Object> response = seatController.updateSeatStatus(1L, body, session);

        assertFalse((Boolean) response.get("success"));
        assertEquals("无效的座位状态", response.get("message"));
        verify(seatService, never()).updateSeatStatus(anyLong(), anyString());
    }

    // 测试场景：获取座位统计信息 - 成功
    // 前置条件：系统中有座位数据
    // 输入数据：无
    // 预期结果：返回各状态座位数量统计
    @Test
    void testGetSeatStats_Success() {
        SeatService.SeatStats mockStats = new SeatService.SeatStats(8, 6, 2, 0, 0);
        
        when(seatService.getSeatStats()).thenReturn(mockStats);

        Map<String, Object> response = seatController.getSeatStats();

        assertTrue((Boolean) response.get("success"));
        assertEquals(8, response.get("total"));
        assertEquals(6, response.get("available"));
        assertEquals(2, response.get("occupied"));
        assertEquals(0, response.get("reserved"));
        assertEquals(0, response.get("maintenance"));
        verify(seatService, times(1)).getSeatStats();
    }

    // 测试场景：获取座位统计信息 - 异常处理
    // 前置条件：Service抛出异常
    // 输入数据：无
    // 预期结果：返回错误信息
    @Test
    void testGetSeatStats_Exception() {
        when(seatService.getSeatStats()).thenThrow(new RuntimeException("统计失败"));

        Map<String, Object> response = seatController.getSeatStats();

        assertFalse((Boolean) response.get("success"));
        assertTrue(((String) response.get("message")).contains("获取统计信息失败"));
    }
}
