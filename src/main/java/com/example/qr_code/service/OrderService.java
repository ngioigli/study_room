package com.example.qr_code.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.qr_code.entity.Order;
import com.example.qr_code.entity.Seat;
import com.example.qr_code.mapper.OrderMapper;
import com.example.qr_code.mapper.SeatMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private SeatMapper seatMapper;

    @Autowired
    private SeatService seatService;

    /**
     * 创建订单（入座）
     * @param userId 用户ID
     * @param seatId 座位ID
     * @param plannedDuration 计划时长（分钟），可为null
     * @return 创建的订单
     */
    @Transactional
    public Order createOrder(Long userId, Long seatId, Integer plannedDuration) {
        // 检查用户是否有进行中的订单
        Order activeOrder = getActiveOrderByUserId(userId);
        if (activeOrder != null) {
            throw new RuntimeException("您已有进行中的订单，请先结束当前订单");
        }

        // 检查座位是否可用
        Seat seat = seatMapper.selectById(seatId);
        if (seat == null) {
            throw new RuntimeException("座位不存在");
        }
        if (!"available".equals(seat.getStatus())) {
            throw new RuntimeException("座位当前不可用");
        }

        // 占用座位并通电
        seatService.occupySeat(seatId);

        // 创建订单
        Order order = new Order();
        order.setUserId(userId);
        order.setSeatId(seatId);
        order.setStartTime(LocalDateTime.now());
        order.setPlannedDuration(plannedDuration);
        order.setStatus("active");
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        orderMapper.insert(order);

        return order;
    }

    /**
     * 结束订单（离座）
     * @param orderId 订单ID
     * @return 更新后的订单
     */
    @Transactional
    public Order endOrder(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (!"active".equals(order.getStatus())) {
            throw new RuntimeException("订单已结束");
        }

        LocalDateTime endTime = LocalDateTime.now();
        Duration duration = Duration.between(order.getStartTime(), endTime);
        int actualDuration = (int) duration.toMinutes();

        order.setEndTime(endTime);
        order.setActualDuration(actualDuration);
        order.setStatus("completed");
        order.setUpdatedAt(LocalDateTime.now());

        orderMapper.updateById(order);

        // 释放座位并断电
        seatService.releaseSeat(order.getSeatId());

        return order;
    }

    /**
     * 取消订单
     * @param orderId 订单ID
     * @return 更新后的订单
     */
    @Transactional
    public Order cancelOrder(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (!"active".equals(order.getStatus())) {
            throw new RuntimeException("订单已结束");
        }

        order.setEndTime(LocalDateTime.now());
        order.setStatus("cancelled");
        order.setUpdatedAt(LocalDateTime.now());

        orderMapper.updateById(order);

        // 释放座位并断电
        seatService.releaseSeat(order.getSeatId());

        return order;
    }

    /**
     * 获取用户当前进行中的订单
     */
    public Order getActiveOrderByUserId(Long userId) {
        return orderMapper.selectOne(
            new QueryWrapper<Order>()
                .eq("user_id", userId)
                .eq("status", "active")
                .orderByDesc("created_at")
                .last("LIMIT 1")
        );
    }

    /**
     * 获取订单详情（包含座位信息）
     */
    public OrderDetail getOrderDetail(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            return null;
        }
        
        Seat seat = seatMapper.selectById(order.getSeatId());
        return new OrderDetail(order, seat);
    }

    /**
     * 获取用户的订单历史
     */
    public List<Order> getOrderHistory(Long userId, int limit) {
        return orderMapper.selectList(
            new QueryWrapper<Order>()
                .eq("user_id", userId)
                .orderByDesc("created_at")
                .last("LIMIT " + limit)
        );
    }

    /**
     * 获取所有进行中的订单
     */
    public List<Order> getAllActiveOrders() {
        return orderMapper.selectList(
            new QueryWrapper<Order>()
                .eq("status", "active")
                .orderByAsc("start_time")
        );
    }

    /**
     * 检查并处理超时订单
     * @param timeoutMinutes 超时时间（分钟）
     */
    @Transactional
    public int handleTimeoutOrders(int timeoutMinutes) {
        LocalDateTime timeoutThreshold = LocalDateTime.now().minusMinutes(timeoutMinutes);
        
        List<Order> activeOrders = orderMapper.selectList(
            new QueryWrapper<Order>()
                .eq("status", "active")
                .lt("start_time", timeoutThreshold)
        );

        int count = 0;
        for (Order order : activeOrders) {
            // 如果有计划时长，检查是否超过计划时长
            if (order.getPlannedDuration() != null) {
                LocalDateTime plannedEnd = order.getStartTime().plusMinutes(order.getPlannedDuration());
                if (LocalDateTime.now().isAfter(plannedEnd.plusMinutes(30))) {
                    // 超过计划时长30分钟，标记为超时
                    markOrderTimeout(order);
                    count++;
                }
            } else if (Duration.between(order.getStartTime(), LocalDateTime.now()).toMinutes() > timeoutMinutes) {
                // 没有计划时长，超过默认超时时间
                markOrderTimeout(order);
                count++;
            }
        }

        return count;
    }

    private void markOrderTimeout(Order order) {
        order.setEndTime(LocalDateTime.now());
        order.setStatus("timeout");
        order.setActualDuration((int) Duration.between(order.getStartTime(), LocalDateTime.now()).toMinutes());
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(order);

        // 释放座位
        seatService.releaseSeat(order.getSeatId());
    }

    /**
     * 计算订单费用
     * @param durationMinutes 使用时长（分钟）
     * @return 费用（元）
     */
    public double calculateFee(int durationMinutes) {
        // 计费规则：每小时5元，不足1小时按1小时计算
        int hours = (int) Math.ceil(durationMinutes / 60.0);
        return hours * 5.0;
    }

    /**
     * 订单详情类（包含座位信息）
     */
    public static class OrderDetail {
        public Order order;
        public Seat seat;

        public OrderDetail(Order order, Seat seat) {
            this.order = order;
            this.seat = seat;
        }
    }
}
