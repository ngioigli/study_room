package com.example.qr_code.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.qr_code.entity.SeatReservation;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * SeatReservationMapper 座位预约数据访问接口
 */
@Repository
public interface SeatReservationMapper extends BaseMapper<SeatReservation> {
    
    /**
     * 查询用户的预约列表
     */
    @Select("SELECT r.*, s.seat_number " +
            "FROM seat_reservations r " +
            "LEFT JOIN seats s ON r.seat_id = s.id " +
            "WHERE r.user_id = #{userId} AND r.status IN (1, 2) " +
            "ORDER BY r.reservation_date DESC, r.start_time DESC")
    List<SeatReservation> selectByUserId(@Param("userId") Long userId);
    
    /**
     * 查询座位在指定日期的预约情况
     */
    @Select("SELECT r.*, u.nickname " +
            "FROM seat_reservations r " +
            "LEFT JOIN users u ON r.user_id = u.id " +
            "WHERE r.seat_id = #{seatId} AND r.reservation_date = #{date} AND r.status = 1 " +
            "ORDER BY r.start_time ASC")
    List<SeatReservation> selectBySeatAndDate(@Param("seatId") Long seatId, @Param("date") LocalDate date);
    
    /**
     * 检查时间段是否冲突
     */
    @Select("SELECT COUNT(*) FROM seat_reservations " +
            "WHERE seat_id = #{seatId} AND reservation_date = #{date} AND status = 1 " +
            "AND ((start_time <= #{startTime} AND end_time > #{startTime}) " +
            "OR (start_time < #{endTime} AND end_time >= #{endTime}) " +
            "OR (start_time >= #{startTime} AND end_time <= #{endTime}))")
    int checkTimeConflict(@Param("seatId") Long seatId, @Param("date") LocalDate date,
                          @Param("startTime") String startTime, @Param("endTime") String endTime);
    
    /**
     * 统计用户当日预约数量
     */
    @Select("SELECT COUNT(*) FROM seat_reservations WHERE user_id = #{userId} AND reservation_date = #{date} AND status = 1")
    int countUserDailyReservations(@Param("userId") Long userId, @Param("date") LocalDate date);
    
    /**
     * 将过期预约标记为已过期
     */
    @Update("UPDATE seat_reservations SET status = 3 " +
            "WHERE status = 1 AND (reservation_date < CURDATE() " +
            "OR (reservation_date = CURDATE() AND end_time < CURTIME()))")
    int markExpiredReservations();
}
