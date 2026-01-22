package com.example.qr_code.controller;

import com.example.qr_code.entity.Order;
import com.example.qr_code.entity.Seat;
import com.example.qr_code.entity.User;
import com.example.qr_code.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.servlet.http.HttpSession;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * OrderController 单元测试
 * 测试订单管理功能
 */
public class OrderControllerTest {

    @InjectMocks
    private OrderController orderController;

    @Mock
    private OrderService orderService;

    @Mock
    private HttpSession session;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * 测试场景：成功创建订单（已登录用户）
     * 前置条件：用户已登录，座位可用
     * 输入数据：seatId和plannedDuration
     * 预期结果：创建订单成功，座位通电
     */
    @Test
    public void testCreateOrder_Success() {
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("张三");

        when(session.getAttribute("user")).thenReturn(mockUser);

        Map<String, Object> body = new HashMap<>();
        body.put("seatId", 5L);
        body.put("plannedDuration", 120);

        Order mockOrder = new Order();
        mockOrder.setId(100L);
        mockOrder.setUserId(1L);
        mockOrder.setSeatId(5L);
        mockOrder.setStatus("ACTIVE");

        when(orderService.createOrder(1L, 5L, 120)).thenReturn(mockOrder);

        Map<String, Object> response = orderController.createOrder(body, session);

        assertNotNull(response);
        assertEquals(true, response.get("success"));
        assertEquals("入座成功，座位已通电", response.get("message"));
        assertNotNull(response.get("order"));

        verify(session, times(1)).getAttribute("user");
        verify(orderService, times(1)).createOrder(1L, 5L, 120);
    }

    /**
     * 测试场景：创建订单时未登录
     * 前置条件：用户未登录
     * 输入数据：seatId和plannedDuration
     * 预期结果：返回错误信息
     */
    @Test
    public void testCreateOrder_NotLoggedIn() {
        when(session.getAttribute("user")).thenReturn(null);

        Map<String, Object> body = new HashMap<>();
        body.put("seatId", 5L);

        Map<String, Object> response = orderController.createOrder(body, session);

        assertNotNull(response);
        assertEquals(false, response.get("success"));
        assertEquals("请先登录", response.get("message"));

        verify(session, times(1)).getAttribute("user");
        verify(orderService, never()).createOrder(anyLong(), anyLong(), any());
    }

    /**
     * 测试场景：创建订单时未选择座位
     * 前置条件：用户已登录
     * 输入数据：seatId为null
     * 预期结果：返回错误信息
     */
    @Test
    public void testCreateOrder_NoSeatSelected() {
        User mockUser = new User();
        mockUser.setId(1L);

        when(session.getAttribute("user")).thenReturn(mockUser);

        Map<String, Object> body = new HashMap<>();
        body.put("plannedDuration", 120);

        Map<String, Object> response = orderController.createOrder(body, session);

        assertNotNull(response);
        assertEquals(false, response.get("success"));
        assertEquals("请选择座位", response.get("message"));

        verify(orderService, never()).createOrder(anyLong(), anyLong(), any());
    }

    /**
     * 测试场景：创建订单时服务层抛出异常
     * 前置条件：用户已登录，但座位已被占用
     * 输入数据：被占用的seatId
     * 预期结果：返回错误信息
     */
    @Test
    public void testCreateOrder_SeatOccupied() {
        User mockUser = new User();
        mockUser.setId(1L);

        when(session.getAttribute("user")).thenReturn(mockUser);

        Map<String, Object> body = new HashMap<>();
        body.put("seatId", 5L);
        body.put("plannedDuration", 120);

        when(orderService.createOrder(1L, 5L, 120)).thenThrow(new RuntimeException("座位已被占用"));

        Map<String, Object> response = orderController.createOrder(body, session);

        assertNotNull(response);
        assertEquals(false, response.get("success"));
        assertTrue(response.get("message").toString().contains("座位已被占用"));

        verify(orderService, times(1)).createOrder(1L, 5L, 120);
    }

    /**
     * 测试场景：成功结束订单（离座）
     * 前置条件：用户已登录，订单存在
     * 输入数据：订单ID
     * 预期结果：订单结束，计算费用，座位断电
     */
    @Test
    public void testEndOrder_Success() {
        User mockUser = new User();
        mockUser.setId(1L);

        when(session.getAttribute("user")).thenReturn(mockUser);

        Order mockOrder = new Order();
        mockOrder.setId(100L);
        mockOrder.setUserId(1L);
        mockOrder.setActualDuration(150);
        mockOrder.setStatus("COMPLETED");

        when(orderService.endOrder(100L)).thenReturn(mockOrder);
        when(orderService.calculateFee(150)).thenReturn(5.0);

        Map<String, Object> response = orderController.endOrder(100L, session);

        assertNotNull(response);
        assertEquals(true, response.get("success"));
        assertEquals("离座成功，座位已断电", response.get("message"));
        assertEquals(150, response.get("actualDuration"));
        assertEquals(5.0, response.get("fee"));

        verify(session, times(1)).getAttribute("user");
        verify(orderService, times(1)).endOrder(100L);
        verify(orderService, times(1)).calculateFee(150);
    }

    /**
     * 测试场景：结束订单时未登录
     * 前置条件：用户未登录
     * 输入数据：订单ID
     * 预期结果：返回错误信息
     */
    @Test
    public void testEndOrder_NotLoggedIn() {
        when(session.getAttribute("user")).thenReturn(null);

        Map<String, Object> response = orderController.endOrder(100L, session);

        assertNotNull(response);
        assertEquals(false, response.get("success"));
        assertEquals("请先登录", response.get("message"));

        verify(session, times(1)).getAttribute("user");
        verify(orderService, never()).endOrder(anyLong());
    }

    /**
     * 测试场景：结束订单时服务层异常
     * 前置条件：用户已登录，订单不存在
     * 输入数据：不存在的订单ID
     * 预期结果：返回错误信息
     */
    @Test
    public void testEndOrder_OrderNotFound() {
        User mockUser = new User();
        mockUser.setId(1L);

        when(session.getAttribute("user")).thenReturn(mockUser);
        when(orderService.endOrder(999L)).thenThrow(new RuntimeException("订单不存在"));

        Map<String, Object> response = orderController.endOrder(999L, session);

        assertNotNull(response);
        assertEquals(false, response.get("success"));
        assertTrue(response.get("message").toString().contains("订单不存在"));

        verify(orderService, times(1)).endOrder(999L);
    }

    /**
     * 测试场景：成功取消订单
     * 前置条件：用户已登录，订单存在
     * 输入数据：订单ID
     * 预期结果：订单取消成功
     */
    @Test
    public void testCancelOrder_Success() {
        User mockUser = new User();
        mockUser.setId(1L);

        when(session.getAttribute("user")).thenReturn(mockUser);

        Order mockOrder = new Order();
        mockOrder.setId(100L);
        mockOrder.setStatus("CANCELLED");

        when(orderService.cancelOrder(100L)).thenReturn(mockOrder);

        Map<String, Object> response = orderController.cancelOrder(100L, session);

        assertNotNull(response);
        assertEquals(true, response.get("success"));
        assertEquals("订单已取消", response.get("message"));

        verify(session, times(1)).getAttribute("user");
        verify(orderService, times(1)).cancelOrder(100L);
    }

    /**
     * 测试场景：取消订单时未登录
     * 前置条件：用户未登录
     * 输入数据：订单ID
     * 预期结果：返回错误信息
     */
    @Test
    public void testCancelOrder_NotLoggedIn() {
        when(session.getAttribute("user")).thenReturn(null);

        Map<String, Object> response = orderController.cancelOrder(100L, session);

        assertNotNull(response);
        assertEquals(false, response.get("success"));
        assertEquals("请先登录", response.get("message"));

        verify(session, times(1)).getAttribute("user");
        verify(orderService, never()).cancelOrder(anyLong());
    }

    /**
     * 测试场景：成功获取当前进行中的订单
     * 前置条件：用户已登录，有活动订单
     * 输入数据：无
     * 预期结果：返回订单和座位信息
     */
    @Test
    public void testGetCurrentOrder_HasActiveOrder() {
        User mockUser = new User();
        mockUser.setId(1L);

        when(session.getAttribute("user")).thenReturn(mockUser);

        Order mockOrder = new Order();
        mockOrder.setId(100L);
        mockOrder.setUserId(1L);
        mockOrder.setSeatId(5L);

        Seat mockSeat = new Seat();
        mockSeat.setId(5L);
        mockSeat.setSeatNumber("A05");

        OrderService.OrderDetail mockDetail = new OrderService.OrderDetail(mockOrder, mockSeat);

        when(orderService.getActiveOrderByUserId(1L)).thenReturn(mockOrder);
        when(orderService.getOrderDetail(100L)).thenReturn(mockDetail);

        Map<String, Object> response = orderController.getCurrentOrder(session);

        assertNotNull(response);
        assertEquals(true, response.get("success"));
        assertEquals(true, response.get("hasActiveOrder"));
        assertNotNull(response.get("order"));
        assertNotNull(response.get("seat"));

        verify(session, times(1)).getAttribute("user");
        verify(orderService, times(1)).getActiveOrderByUserId(1L);
        verify(orderService, times(1)).getOrderDetail(100L);
    }

    /**
     * 测试场景：获取当前订单但无活动订单
     * 前置条件：用户已登录，无活动订单
     * 输入数据：无
     * 预期结果：返回hasActiveOrder=false
     */
    @Test
    public void testGetCurrentOrder_NoActiveOrder() {
        User mockUser = new User();
        mockUser.setId(1L);

        when(session.getAttribute("user")).thenReturn(mockUser);
        when(orderService.getActiveOrderByUserId(1L)).thenReturn(null);

        Map<String, Object> response = orderController.getCurrentOrder(session);

        assertNotNull(response);
        assertEquals(true, response.get("success"));
        assertEquals(false, response.get("hasActiveOrder"));

        verify(session, times(1)).getAttribute("user");
        verify(orderService, times(1)).getActiveOrderByUserId(1L);
        verify(orderService, never()).getOrderDetail(anyLong());
    }

    /**
     * 测试场景：获取当前订单时未登录
     * 前置条件：用户未登录
     * 输入数据：无
     * 预期结果：返回错误信息
     */
    @Test
    public void testGetCurrentOrder_NotLoggedIn() {
        when(session.getAttribute("user")).thenReturn(null);

        Map<String, Object> response = orderController.getCurrentOrder(session);

        assertNotNull(response);
        assertEquals(false, response.get("success"));
        assertEquals("请先登录", response.get("message"));

        verify(session, times(1)).getAttribute("user");
        verify(orderService, never()).getActiveOrderByUserId(anyLong());
    }

    /**
     * 测试场景：成功获取订单详情
     * 前置条件：用户已登录，订单存在
     * 输入数据：订单ID
     * 预期结果：返回订单和座位详细信息
     */
    @Test
    public void testGetOrderDetail_Success() {
        User mockUser = new User();
        mockUser.setId(1L);

        when(session.getAttribute("user")).thenReturn(mockUser);

        Order mockOrder = new Order();
        mockOrder.setId(100L);
        mockOrder.setActualDuration(150);

        Seat mockSeat = new Seat();
        mockSeat.setId(5L);

        OrderService.OrderDetail mockDetail = new OrderService.OrderDetail(mockOrder, mockSeat);

        when(orderService.getOrderDetail(100L)).thenReturn(mockDetail);
        when(orderService.calculateFee(150)).thenReturn(5.0);

        Map<String, Object> response = orderController.getOrderDetail(100L, session);

        assertNotNull(response);
        assertEquals(true, response.get("success"));
        assertEquals(5.0, response.get("fee"));

        verify(session, times(1)).getAttribute("user");
        verify(orderService, times(1)).getOrderDetail(100L);
        verify(orderService, times(1)).calculateFee(150);
    }

    /**
     * 测试场景：获取订单详情时订单不存在
     * 前置条件：用户已登录
     * 输入数据：不存在的订单ID
     * 预期结果：返回订单不存在错误
     */
    @Test
    public void testGetOrderDetail_NotFound() {
        User mockUser = new User();
        mockUser.setId(1L);

        when(session.getAttribute("user")).thenReturn(mockUser);
        when(orderService.getOrderDetail(999L)).thenReturn(null);

        Map<String, Object> response = orderController.getOrderDetail(999L, session);

        assertNotNull(response);
        assertEquals(false, response.get("success"));
        assertEquals("订单不存在", response.get("message"));

        verify(orderService, times(1)).getOrderDetail(999L);
    }

    /**
     * 测试场景：成功获取订单历史
     * 前置条件：用户已登录
     * 输入数据：默认limit=10
     * 预期结果：返回订单历史列表
     */
    @Test
    public void testGetOrderHistory_Success() {
        User mockUser = new User();
        mockUser.setId(1L);

        when(session.getAttribute("user")).thenReturn(mockUser);

        List<Order> mockOrders = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            Order order = new Order();
            order.setId((long) i);
            order.setUserId(1L);
            mockOrders.add(order);
        }

        when(orderService.getOrderHistory(1L, 10)).thenReturn(mockOrders);

        Map<String, Object> response = orderController.getOrderHistory(10, session);

        assertNotNull(response);
        assertEquals(true, response.get("success"));
        assertEquals(5, response.get("count"));

        verify(session, times(1)).getAttribute("user");
        verify(orderService, times(1)).getOrderHistory(1L, 10);
    }

    /**
     * 测试场景：获取订单历史时未登录
     * 前置条件：用户未登录
     * 输入数据：默认limit
     * 预期结果：返回错误信息
     */
    @Test
    public void testGetOrderHistory_NotLoggedIn() {
        when(session.getAttribute("user")).thenReturn(null);

        Map<String, Object> response = orderController.getOrderHistory(10, session);

        assertNotNull(response);
        assertEquals(false, response.get("success"));
        assertEquals("请先登录", response.get("message"));

        verify(session, times(1)).getAttribute("user");
        verify(orderService, never()).getOrderHistory(anyLong(), anyInt());
    }

    /**
     * 测试场景：成功获取所有进行中的订单（管理员）
     * 前置条件：用户已登录
     * 输入数据：无
     * 预期结果：返回所有活动订单列表
     */
    @Test
    public void testGetAllActiveOrders_Success() {
        User mockUser = new User();
        mockUser.setId(1L);

        when(session.getAttribute("user")).thenReturn(mockUser);

        List<Order> mockOrders = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            Order order = new Order();
            order.setId((long) i);
            order.setStatus("ACTIVE");
            mockOrders.add(order);
        }

        when(orderService.getAllActiveOrders()).thenReturn(mockOrders);

        Map<String, Object> response = orderController.getAllActiveOrders(session);

        assertNotNull(response);
        assertEquals(true, response.get("success"));
        assertEquals(3, response.get("count"));

        verify(session, times(1)).getAttribute("user");
        verify(orderService, times(1)).getAllActiveOrders();
    }

    /**
     * 测试场景：获取所有进行中的订单时未登录
     * 前置条件：用户未登录
     * 输入数据：无
     * 预期结果：返回错误信息
     */
    @Test
    public void testGetAllActiveOrders_NotLoggedIn() {
        when(session.getAttribute("user")).thenReturn(null);

        Map<String, Object> response = orderController.getAllActiveOrders(session);

        assertNotNull(response);
        assertEquals(false, response.get("success"));
        assertEquals("请先登录", response.get("message"));

        verify(session, times(1)).getAttribute("user");
        verify(orderService, never()).getAllActiveOrders();
    }
}
