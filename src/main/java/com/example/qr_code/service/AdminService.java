package com.example.qr_code.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.qr_code.entity.*;
import com.example.qr_code.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class AdminService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SeatMapper seatMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private FocusRecordMapper focusRecordMapper;

    @Autowired
    private LearningStatsMapper learningStatsMapper;

    // ==================== 用户管理 ====================

    /**
     * 获取所有用户列表
     */
    public List<User> getAllUsers() {
        return userMapper.selectList(new QueryWrapper<User>().orderByDesc("created_at"));
    }

    /**
     * 获取用户数量统计
     */
    public Map<String, Integer> getUserStats() {
        List<User> users = userMapper.selectList(null);
        int total = users.size();
        int active = 0;
        int disabled = 0;
        int admins = 0;

        for (User user : users) {
            if (user.getStatus() != null && user.getStatus() == 1) {
                active++;
            } else {
                disabled++;
            }
            if ("admin".equals(user.getRole())) {
                admins++;
            }
        }

        Map<String, Integer> stats = new HashMap<>();
        stats.put("total", total);
        stats.put("active", active);
        stats.put("disabled", disabled);
        stats.put("admins", admins);
        return stats;
    }

    /**
     * 更新用户状态
     */
    @Transactional
    public User updateUserStatus(Long userId, Integer status) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setStatus(status);
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
        return user;
    }

    /**
     * 更新用户角色
     */
    @Transactional
    public User updateUserRole(Long userId, String role) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (!role.matches("user|admin")) {
            throw new RuntimeException("无效的角色");
        }
        user.setRole(role);
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
        return user;
    }

    // ==================== 座位管理 ====================

    /**
     * 获取所有座位（含当前使用者信息）
     */
    public List<Map<String, Object>> getAllSeatsWithUsers() {
        List<Seat> seats = seatMapper.selectList(new QueryWrapper<Seat>().orderByAsc("seat_number"));
        List<Map<String, Object>> result = new ArrayList<>();

        for (Seat seat : seats) {
            Map<String, Object> seatInfo = new HashMap<>();
            seatInfo.put("seat", seat);

            // 如果座位被占用，查找当前使用者
            if ("occupied".equals(seat.getStatus())) {
                Order activeOrder = orderMapper.selectOne(
                    new QueryWrapper<Order>()
                        .eq("seat_id", seat.getId())
                        .eq("status", "active")
                        .last("LIMIT 1")
                );
                if (activeOrder != null) {
                    User user = userMapper.selectById(activeOrder.getUserId());
                    seatInfo.put("currentUser", user != null ? user.getUsername() : "未知");
                    seatInfo.put("startTime", activeOrder.getStartTime());
                    seatInfo.put("orderId", activeOrder.getId());
                }
            }

            result.add(seatInfo);
        }

        return result;
    }

    /**
     * 强制释放座位
     */
    @Transactional
    public void forceReleaseSeat(Long seatId) {
        Seat seat = seatMapper.selectById(seatId);
        if (seat == null) {
            throw new RuntimeException("座位不存在");
        }

        // 结束该座位的所有活跃订单
        Order activeOrder = orderMapper.selectOne(
            new QueryWrapper<Order>()
                .eq("seat_id", seatId)
                .eq("status", "active")
        );
        if (activeOrder != null) {
            activeOrder.setEndTime(LocalDateTime.now());
            activeOrder.setStatus("cancelled");
            activeOrder.setUpdatedAt(LocalDateTime.now());
            orderMapper.updateById(activeOrder);
        }

        // 释放座位
        seat.setStatus("available");
        seat.setPowerOn(0);
        seat.setUpdatedAt(LocalDateTime.now());
        seatMapper.updateById(seat);
    }

    /**
     * 设置座位为维护状态
     */
    @Transactional
    public Seat setSeatMaintenance(Long seatId, boolean maintenance) {
        Seat seat = seatMapper.selectById(seatId);
        if (seat == null) {
            throw new RuntimeException("座位不存在");
        }

        if (maintenance) {
            // 如果座位被占用，先强制释放
            if ("occupied".equals(seat.getStatus())) {
                forceReleaseSeat(seatId);
                seat = seatMapper.selectById(seatId);
            }
            seat.setStatus("maintenance");
        } else {
            seat.setStatus("available");
        }

        seat.setPowerOn(0);
        seat.setUpdatedAt(LocalDateTime.now());
        seatMapper.updateById(seat);
        return seat;
    }

    // ==================== 数据统计 ====================

    /**
     * 获取今日概览数据
     */
    public Map<String, Object> getTodayOverview() {
        LocalDate today = LocalDate.now();
        Map<String, Object> overview = new HashMap<>();

        // 今日活跃用户数（有专注记录的用户）
        Long activeUsers = focusRecordMapper.selectCount(
            new QueryWrapper<FocusRecord>().eq("focus_date", today)
        );
        overview.put("activeUsers", activeUsers);

        // 今日总专注时长
        List<FocusRecord> todayRecords = focusRecordMapper.selectList(
            new QueryWrapper<FocusRecord>().eq("focus_date", today)
        );
        int totalDuration = todayRecords.stream().mapToInt(FocusRecord::getDuration).sum();
        overview.put("totalDuration", totalDuration);
        overview.put("totalHours", String.format("%.1f", totalDuration / 3600.0));

        // 今日订单数
        Long todayOrders = orderMapper.selectCount(
            new QueryWrapper<Order>().ge("created_at", today.atStartOfDay())
        );
        overview.put("todayOrders", todayOrders);

        // 当前在座人数
        Long currentOccupied = seatMapper.selectCount(
            new QueryWrapper<Seat>().eq("status", "occupied")
        );
        overview.put("currentOccupied", currentOccupied);

        // 座位使用率
        Long totalSeats = seatMapper.selectCount(null);
        double usageRate = totalSeats > 0 ? (currentOccupied * 100.0 / totalSeats) : 0;
        overview.put("usageRate", String.format("%.1f", usageRate));

        return overview;
    }

    /**
     * 获取最近7天的统计数据
     */
    public List<Map<String, Object>> getWeeklyStats() {
        List<Map<String, Object>> result = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            Map<String, Object> dayStat = new HashMap<>();
            dayStat.put("date", date.toString());
            dayStat.put("dayOfWeek", getDayOfWeek(date));

            // 当日专注记录
            List<FocusRecord> records = focusRecordMapper.selectList(
                new QueryWrapper<FocusRecord>().eq("focus_date", date)
            );
            int totalDuration = records.stream().mapToInt(FocusRecord::getDuration).sum();
            dayStat.put("totalDuration", totalDuration);
            dayStat.put("focusCount", records.size());

            // 当日订单数
            Long orders = orderMapper.selectCount(
                new QueryWrapper<Order>()
                    .ge("created_at", date.atStartOfDay())
                    .lt("created_at", date.plusDays(1).atStartOfDay())
            );
            dayStat.put("orderCount", orders);

            result.add(dayStat);
        }

        return result;
    }

    private String getDayOfWeek(LocalDate date) {
        String[] days = {"周日", "周一", "周二", "周三", "周四", "周五", "周六"};
        return days[date.getDayOfWeek().getValue() % 7];
    }

    /**
     * 获取用户排行榜
     */
    public List<Map<String, Object>> getUserRanking(int limit) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(6);

        // 获取本周学习统计
        List<LearningStats> stats = learningStatsMapper.selectList(
            new QueryWrapper<LearningStats>()
                .ge("stat_date", weekStart)
                .le("stat_date", today)
        );

        // 按用户汇总
        Map<Long, Integer> userDurations = new HashMap<>();
        for (LearningStats stat : stats) {
            userDurations.merge(stat.getUserId(), stat.getTotalDuration(), Integer::sum);
        }

        // 排序
        List<Map.Entry<Long, Integer>> sorted = new ArrayList<>(userDurations.entrySet());
        sorted.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        // 构建结果
        List<Map<String, Object>> result = new ArrayList<>();
        int rank = 1;
        for (Map.Entry<Long, Integer> entry : sorted) {
            if (rank > limit) break;

            User user = userMapper.selectById(entry.getKey());
            if (user != null) {
                Map<String, Object> item = new HashMap<>();
                item.put("rank", rank);
                item.put("userId", user.getId());
                item.put("username", user.getUsername());
                item.put("nickname", user.getNickname());
                item.put("totalDuration", entry.getValue());
                item.put("totalHours", String.format("%.1f", entry.getValue() / 3600.0));
                result.add(item);
                rank++;
            }
        }

        return result;
    }
}
