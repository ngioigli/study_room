package com.example.qr_code.controller;

import com.example.qr_code.entity.Seat;
import com.example.qr_code.entity.SeatReservation;
import com.example.qr_code.entity.User;
import com.example.qr_code.service.ReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ReservationController 单元测试
 * 测试座位预约相关接口的功能
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("座位预约控制器测试")
class ReservationControllerTest {

    @InjectMocks
    private ReservationController reservationController;

    @Mock
    private ReservationService reservationService;

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
     * 测试场景：获取我的预约列表 - 用户已登录且有预约记录
     * 前置条件：用户已登录，存在预约记录
     * 输入数据：有效的用户会话
     * 预期结果：返回成功响应和预约列表
     */
    @Test
    @DisplayName("获取我的预约列表 - 成功")
    void testGetMyReservations_Success() {
        // Arrange
        session.setAttribute("user", testUser);
        
        SeatReservation reservation = new SeatReservation();
        reservation.setId(1L);
        reservation.setUserId(testUser.getId());
        reservation.setSeatId(1L);
        reservation.setReservationDate(LocalDate.now());
        reservation.setStartTime(LocalTime.of(9, 0));
        reservation.setEndTime(LocalTime.of(11, 0));
        reservation.setStatus(1);
        
        List<SeatReservation> reservations = Arrays.asList(reservation);
        when(reservationService.getUserReservations(testUser.getId())).thenReturn(reservations);

        // Act
        Map<String, Object> response = reservationController.getMyReservations(session);

        // Assert
        assertTrue((Boolean) response.get("success"));
        assertEquals(reservations, response.get("reservations"));
        verify(reservationService, times(1)).getUserReservations(testUser.getId());
    }

    /**
     * 测试场景：获取我的预约列表 - 用户未登录
     * 前置条件：用户未登录
     * 输入数据：空的用户会话
     * 预期结果：返回失败响应和登录提示
     */
    @Test
    @DisplayName("获取我的预约列表 - 用户未登录")
    void testGetMyReservations_NotLoggedIn() {
        // Act
        Map<String, Object> response = reservationController.getMyReservations(session);

        // Assert
        assertFalse((Boolean) response.get("success"));
        assertEquals("请先登录", response.get("message"));
        verify(reservationService, never()).getUserReservations(any());
    }

    /**
     * 测试场景：获取待使用预约 - 用户有待使用预约
     * 前置条件：用户已登录且有待使用预约
     * 输入数据：有效的用户会话
     * 预期结果：返回成功响应和预约信息
     */
    @Test
    @DisplayName("获取待使用预约 - 成功")
    void testGetPendingReservation_Success() {
        // Arrange
        session.setAttribute("user", testUser);
        
        SeatReservation pendingReservation = new SeatReservation();
        pendingReservation.setId(1L);
        pendingReservation.setUserId(testUser.getId());
        pendingReservation.setStatus(1);
        
        when(reservationService.getUserPendingReservation(testUser.getId())).thenReturn(pendingReservation);

        // Act
        Map<String, Object> response = reservationController.getPendingReservation(session);

        // Assert
        assertTrue((Boolean) response.get("success"));
        assertEquals(pendingReservation, response.get("reservation"));
        verify(reservationService, times(1)).getUserPendingReservation(testUser.getId());
    }

    /**
     * 测试场景：获取待使用预约 - 用户无待使用预约
     * 前置条件：用户已登录但无待使用预约
     * 输入数据：有效的用户会话
     * 预期结果：返回成功响应但无预约信息
     */
    @Test
    @DisplayName("获取待使用预约 - 无预约")
    void testGetPendingReservation_NoPending() {
        // Arrange
        session.setAttribute("user", testUser);
        when(reservationService.getUserPendingReservation(testUser.getId())).thenReturn(null);

        // Act
        Map<String, Object> response = reservationController.getPendingReservation(session);

        // Assert
        assertTrue((Boolean) response.get("success"));
        assertNull(response.get("reservation"));
        verify(reservationService, times(1)).getUserPendingReservation(testUser.getId());
    }

    /**
     * 测试场景：获取座位列表 - 成功获取
     * 前置条件：系统中存在座位数据
     * 输入数据：无需输入
     * 预期结果：返回成功响应和座位列表
     */
    @Test
    @DisplayName("获取座位列表 - 成功")
    void testGetSeats_Success() {
        // Arrange
        Seat seat1 = new Seat();
        seat1.setId(1L);
        seat1.setSeatNumber("A01");
        
        Seat seat2 = new Seat();
        seat2.setId(2L);
        seat2.setSeatNumber("A02");
        
        List<Seat> seats = Arrays.asList(seat1, seat2);
        when(reservationService.getAllSeats()).thenReturn(seats);

        // Act
        Map<String, Object> response = reservationController.getSeats();

        // Assert
        assertTrue((Boolean) response.get("success"));
        assertEquals(seats, response.get("seats"));
        verify(reservationService, times(1)).getAllSeats();
    }

    /**
     * 测试场景：获取座位预约情况 - 成功获取
     * 前置条件：指定座位和日期存在预约记录
     * 输入数据：有效的座位ID和日期
     * 预期结果：返回成功响应和预约列表
     */
    @Test
    @DisplayName("获取座位预约情况 - 成功")
    void testGetSeatReservations_Success() {
        // Arrange
        Long seatId = 1L;
        String date = "2026-01-21";
        LocalDate localDate = LocalDate.parse(date);
        
        SeatReservation reservation = new SeatReservation();
        reservation.setId(1L);
        reservation.setSeatId(seatId);
        reservation.setReservationDate(localDate);
        
        List<SeatReservation> reservations = Arrays.asList(reservation);
        when(reservationService.getSeatReservations(seatId, localDate)).thenReturn(reservations);

        // Act
        Map<String, Object> response = reservationController.getSeatReservations(seatId, date);

        // Assert
        assertTrue((Boolean) response.get("success"));
        assertEquals(reservations, response.get("reservations"));
        verify(reservationService, times(1)).getSeatReservations(seatId, localDate);
    }

    /**
     * 测试场景：获取座位预约情况 - 日期格式错误
     * 前置条件：输入无效的日期格式
     * 输入数据：无效的日期字符串
     * 预期结果：返回失败响应和错误信息
     */
    @Test
    @DisplayName("获取座位预约情况 - 日期格式错误")
    void testGetSeatReservations_InvalidDateFormat() {
        // Arrange
        Long seatId = 1L;
        String invalidDate = "invalid-date";

        // Act
        Map<String, Object> response = reservationController.getSeatReservations(seatId, invalidDate);

        // Assert
        assertFalse((Boolean) response.get("success"));
        assertEquals("日期格式错误", response.get("message"));
        verify(reservationService, never()).getSeatReservations(any(), any());
    }

    /**
     * 测试场景：创建预约 - 成功创建
     * 前置条件：用户已登录，输入有效的预约数据
     * 输入数据：有效的座位ID、日期、时间
     * 预期结果：返回成功响应
     */
    @Test
    @DisplayName("创建预约 - 成功")
    void testCreateReservation_Success() {
        // Arrange
        session.setAttribute("user", testUser);
        
        Map<String, String> data = new HashMap<>();
        data.put("seatId", "1");
        data.put("date", "2026-01-22");
        data.put("startTime", "09:00:00");
        data.put("endTime", "11:00:00");
        
        when(reservationService.createReservation(eq(testUser.getId()), eq(1L), 
                eq(LocalDate.parse("2026-01-22")), eq(LocalTime.parse("09:00:00")), 
                eq(LocalTime.parse("11:00:00")))).thenReturn(null);

        // Act
        Map<String, Object> response = reservationController.createReservation(data, session);

        // Assert
        assertTrue((Boolean) response.get("success"));
        assertEquals("预约成功", response.get("message"));
        verify(reservationService, times(1)).createReservation(any(), any(), any(), any(), any());
    }

    /**
     * 测试场景：创建预约 - 用户未登录
     * 前置条件：用户未登录
     * 输入数据：预约数据
     * 预期结果：返回失败响应和登录提示
     */
    @Test
    @DisplayName("创建预约 - 用户未登录")
    void testCreateReservation_NotLoggedIn() {
        // Arrange
        Map<String, String> data = new HashMap<>();
        data.put("seatId", "1");
        data.put("date", "2026-01-22");
        data.put("startTime", "09:00:00");
        data.put("endTime", "11:00:00");

        // Act
        Map<String, Object> response = reservationController.createReservation(data, session);

        // Assert
        assertFalse((Boolean) response.get("success"));
        assertEquals("请先登录", response.get("message"));
        verify(reservationService, never()).createReservation(any(), any(), any(), any(), any());
    }

    /**
     * 测试场景：创建预约 - 参数格式错误
     * 前置条件：用户已登录，输入无效的参数格式
     * 输入数据：无效的座位ID格式
     * 预期结果：返回失败响应和格式错误信息
     */
    @Test
    @DisplayName("创建预约 - 参数格式错误")
    void testCreateReservation_InvalidFormat() {
        // Arrange
        session.setAttribute("user", testUser);
        
        Map<String, String> data = new HashMap<>();
        data.put("seatId", "invalid");
        data.put("date", "2026-01-22");
        data.put("startTime", "09:00:00");
        data.put("endTime", "11:00:00");

        // Act
        Map<String, Object> response = reservationController.createReservation(data, session);

        // Assert
        assertFalse((Boolean) response.get("success"));
        assertEquals("参数格式错误", response.get("message"));
        verify(reservationService, never()).createReservation(any(), any(), any(), any(), any());
    }

    /**
     * 测试场景：创建预约 - 业务逻辑错误
     * 前置条件：用户已登录，输入有效参数但违反业务规则
     * 输入数据：有效的预约数据
     * 预期结果：返回失败响应和业务错误信息
     */
    @Test
    @DisplayName("创建预约 - 业务逻辑错误")
    void testCreateReservation_BusinessError() {
        // Arrange
        session.setAttribute("user", testUser);
        
        Map<String, String> data = new HashMap<>();
        data.put("seatId", "1");
        data.put("date", "2026-01-22");
        data.put("startTime", "09:00:00");
        data.put("endTime", "11:00:00");
        
        String errorMessage = "该时间段已被预约";
        when(reservationService.createReservation(any(), any(), any(), any(), any())).thenReturn(errorMessage);

        // Act
        Map<String, Object> response = reservationController.createReservation(data, session);

        // Assert
        assertFalse((Boolean) response.get("success"));
        assertEquals(errorMessage, response.get("message"));
        verify(reservationService, times(1)).createReservation(any(), any(), any(), any(), any());
    }

    /**
     * 测试场景：取消预约 - 成功取消
     * 前置条件：用户已登录，预约存在且可取消
     * 输入数据：有效的预约ID
     * 预期结果：返回成功响应
     */
    @Test
    @DisplayName("取消预约 - 成功")
    void testCancelReservation_Success() {
        // Arrange
        session.setAttribute("user", testUser);
        Long reservationId = 1L;
        
        when(reservationService.cancelReservation(reservationId, testUser.getId())).thenReturn(null);

        // Act
        Map<String, Object> response = reservationController.cancelReservation(reservationId, session);

        // Assert
        assertTrue((Boolean) response.get("success"));
        assertEquals("取消成功", response.get("message"));
        verify(reservationService, times(1)).cancelReservation(reservationId, testUser.getId());
    }

    /**
     * 测试场景：取消预约 - 用户未登录
     * 前置条件：用户未登录
     * 输入数据：预约ID
     * 预期结果：返回失败响应和登录提示
     */
    @Test
    @DisplayName("取消预约 - 用户未登录")
    void testCancelReservation_NotLoggedIn() {
        // Arrange
        Long reservationId = 1L;

        // Act
        Map<String, Object> response = reservationController.cancelReservation(reservationId, session);

        // Assert
        assertFalse((Boolean) response.get("success"));
        assertEquals("请先登录", response.get("message"));
        verify(reservationService, never()).cancelReservation(any(), any());
    }

    /**
     * 测试场景：签到 - 成功签到
     * 前置条件：用户已登录，预约存在且在签到时间内
     * 输入数据：有效的预约ID
     * 预期结果：返回成功响应
     */
    @Test
    @DisplayName("签到 - 成功")
    void testCheckIn_Success() {
        // Arrange
        session.setAttribute("user", testUser);
        Long reservationId = 1L;
        
        when(reservationService.checkIn(reservationId, testUser.getId())).thenReturn(null);

        // Act
        Map<String, Object> response = reservationController.checkIn(reservationId, session);

        // Assert
        assertTrue((Boolean) response.get("success"));
        assertEquals("签到成功", response.get("message"));
        verify(reservationService, times(1)).checkIn(reservationId, testUser.getId());
    }

    /**
     * 测试场景：签到 - 签到失败
     * 前置条件：用户已登录，但签到条件不满足
     * 输入数据：有效的预约ID
     * 预期结果：返回失败响应和错误信息
     */
    @Test
    @DisplayName("签到 - 签到失败")
    void testCheckIn_Failed() {
        // Arrange
        session.setAttribute("user", testUser);
        Long reservationId = 1L;
        String errorMessage = "还未到签到时间";
        
        when(reservationService.checkIn(reservationId, testUser.getId())).thenReturn(errorMessage);

        // Act
        Map<String, Object> response = reservationController.checkIn(reservationId, session);

        // Assert
        assertFalse((Boolean) response.get("success"));
        assertEquals(errorMessage, response.get("message"));
        verify(reservationService, times(1)).checkIn(reservationId, testUser.getId());
    }

    /**
     * 测试场景：获取时间段 - 成功获取
     * 前置条件：系统配置正常
     * 输入数据：无需输入
     * 预期结果：返回成功响应和时间段列表
     */
    @Test
    @DisplayName("获取时间段 - 成功")
    void testGetTimeSlots_Success() {
        // Act
        Map<String, Object> response = reservationController.getTimeSlots();

        // Assert
        assertTrue((Boolean) response.get("success"));
        assertNotNull(response.get("timeSlots"));
        
        String[][] timeSlots = (String[][]) response.get("timeSlots");
        assertEquals(7, timeSlots.length); // 预设7个时间段
        assertEquals("08:00", timeSlots[0][0]);
        assertEquals("10:00", timeSlots[0][1]);
    }
}