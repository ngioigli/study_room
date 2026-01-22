package com.example.qr_code.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.qr_code.entity.Seat;
import com.example.qr_code.entity.SeatReservation;
import com.example.qr_code.mapper.SeatMapper;
import com.example.qr_code.mapper.SeatReservationMapper;
import com.example.qr_code.mapper.SystemConfigMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * ReservationService 座位预约服务类
 * 处理座位预约相关业务逻辑
 */
@Service
public class ReservationService {

    @Autowired
    private SeatReservationMapper reservationMapper;

    @Autowired
    private SeatMapper seatMapper;

    @Autowired
    private SystemConfigMapper systemConfigMapper;

    /**
     * 获取用户的预约列表
     */
    public List<SeatReservation> getUserReservations(Long userId) {
        return reservationMapper.selectByUserId(userId);
    }

    /**
     * 获取用户当前待使用的预约（状态为1）
     */
    public SeatReservation getUserPendingReservation(Long userId) {
        QueryWrapper<SeatReservation> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
               .eq("status", 1) // 状态1表示待使用
               .orderByDesc("reservation_date")
               .last("LIMIT 1");
        return reservationMapper.selectOne(wrapper);
    }

    /**
     * 获取座位在指定日期的预约情况
     */
    public List<SeatReservation> getSeatReservations(Long seatId, LocalDate date) {
        return reservationMapper.selectBySeatAndDate(seatId, date);
    }

    /**
     * 创建预约
     */
    @Transactional
    public String createReservation(Long userId, Long seatId, LocalDate date, 
                                    LocalTime startTime, LocalTime endTime) {
        // 检查预约日期是否在允许范围内（只允许今天和明天）
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        if (date.isBefore(today)) {
            return "不能预约过去的日期";
        }
        if (date.isAfter(tomorrow)) {
            return "最多只能提前1天预约（今天或明天）";
        }

        // 检查时间有效性
        if (!startTime.isBefore(endTime)) {
            return "开始时间必须早于结束时间";
        }
        
        // 检查预约时长（1-4小时）
        long durationMinutes = java.time.Duration.between(startTime, endTime).toMinutes();
        if (durationMinutes < 60) {
            return "预约时长不能少于1小时";
        }
        if (durationMinutes > 240) {
            return "预约时长不能超过4小时";
        }
        
        // 检查是否提前至少1小时预约
        LocalDateTime reservationStart = LocalDateTime.of(date, startTime);
        LocalDateTime minAllowedTime = LocalDateTime.now().plusHours(1);
        if (reservationStart.isBefore(minAllowedTime)) {
            return "预约需提前至少1小时";
        }
        
        // 检查营业时间（08:00 - 22:00）
        LocalTime openTime = LocalTime.of(8, 0);
        LocalTime closeTime = LocalTime.of(22, 0);
        if (startTime.isBefore(openTime) || endTime.isAfter(closeTime)) {
            return "营业时间为08:00-22:00";
        }

        // 检查用户是否已有待使用的预约
        SeatReservation pending = getUserPendingReservation(userId);
        if (pending != null) {
            return "您已有一个待使用的预约，请先使用或取消";
        }

        // 检查座位是否存在且可用
        Seat seat = seatMapper.selectById(seatId);
        if (seat == null) {
            return "座位不存在";
        }

        // 检查时间段是否冲突
        int conflict = reservationMapper.checkTimeConflict(seatId, date, 
                startTime.toString(), endTime.toString());
        if (conflict > 0) {
            return "该时间段已被预约";
        }

        // 创建预约
        SeatReservation reservation = new SeatReservation();
        reservation.setUserId(userId);
        reservation.setSeatId(seatId);
        reservation.setReservationDate(date);
        reservation.setStartTime(startTime);
        reservation.setEndTime(endTime);
        reservation.setStatus(1);

        if (reservationMapper.insert(reservation) > 0) {
            return null; // 成功返回null
        }
        return "预约失败，请重试";
    }

    /**
     * 取消预约
     */
    public String cancelReservation(Long reservationId, Long userId) {
        SeatReservation reservation = reservationMapper.selectById(reservationId);
        if (reservation == null) {
            return "预约不存在";
        }
        if (!reservation.getUserId().equals(userId)) {
            return "无权取消此预约";
        }
        if (reservation.getStatus() != 1) {
            return "该预约状态不可取消";
        }

        // 检查是否在允许取消的时间内
        int cancelMinutes = getConfigInt("reservation_cancel_minutes", 30);
        LocalDateTime reservationStart = LocalDateTime.of(reservation.getReservationDate(), 
                reservation.getStartTime());
        if (LocalDateTime.now().plusMinutes(cancelMinutes).isAfter(reservationStart)) {
            return "预约开始前" + cancelMinutes + "分钟内不可取消";
        }

        UpdateWrapper<SeatReservation> wrapper = new UpdateWrapper<>();
        wrapper.eq("id", reservationId).set("status", 0);
        if (reservationMapper.update(null, wrapper) > 0) {
            return null;
        }
        return "取消失败，请重试";
    }

    /**
     * 签到
     */
    public String checkIn(Long reservationId, Long userId) {
        SeatReservation reservation = reservationMapper.selectById(reservationId);
        if (reservation == null) {
            return "预约不存在";
        }
        if (!reservation.getUserId().equals(userId)) {
            return "无权操作此预约";
        }
        if (reservation.getStatus() != 1) {
            return "该预约状态不可签到";
        }

        // 检查是否在签到时间范围内
        int graceMinutes = getConfigInt("checkin_grace_minutes", 15);
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        
        if (!reservation.getReservationDate().equals(today)) {
            return "只能在预约当天签到";
        }
        
        LocalTime allowStart = reservation.getStartTime().minusMinutes(graceMinutes);
        LocalTime allowEnd = reservation.getStartTime().plusMinutes(graceMinutes);
        
        if (now.isBefore(allowStart)) {
            return "还未到签到时间";
        }
        if (now.isAfter(allowEnd)) {
            return "已超过签到时间";
        }

        UpdateWrapper<SeatReservation> wrapper = new UpdateWrapper<>();
        wrapper.eq("id", reservationId)
               .set("status", 2)
               .set("check_in_time", LocalDateTime.now());
        if (reservationMapper.update(null, wrapper) > 0) {
            return null;
        }
        return "签到失败，请重试";
    }

    /**
     * 获取所有座位列表
     */
    public List<Seat> getAllSeats() {
        QueryWrapper<Seat> wrapper = new QueryWrapper<>();
        wrapper.orderByAsc("seat_number");
        return seatMapper.selectList(wrapper);
    }

    /**
     * 标记过期预约
     */
    public int markExpiredReservations() {
        return reservationMapper.markExpiredReservations();
    }

    /**
     * 获取配置整数值
     */
    private int getConfigInt(String key, int defaultValue) {
        String value = systemConfigMapper.getConfigValue(key);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }
}
