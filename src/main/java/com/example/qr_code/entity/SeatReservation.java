package com.example.qr_code.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * SeatReservation 座位预约实体类
 * 对应数据库 seat_reservation 表
 */
@Data
@TableName("seat_reservation")
public class SeatReservation {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 预约者ID */
    @TableField("user_id")
    private Long userId;
    
    /** 座位ID */
    @TableField("seat_id")
    private Long seatId;
    
    /** 预约日期 */
    @TableField("reservation_date")
    private LocalDate reservationDate;
    
    /** 开始时间 */
    @TableField("start_time")
    private LocalTime startTime;
    
    /** 结束时间 */
    @TableField("end_time")
    private LocalTime endTime;
    
    /** 状态：1有效 0取消 2已使用 3已过期 */
    private Integer status;
    
    /** 签到时间 */
    @TableField("check_in_time")
    private LocalDateTime checkInTime;
    
    @TableField("created_at")
    private LocalDateTime createdAt;
    
    @TableField("updated_at")
    private LocalDateTime updatedAt;
    
    // 非数据库字段
    @TableField(exist = false)
    private String seatNumber;
    
    @TableField(exist = false)
    private String nickname;
}
