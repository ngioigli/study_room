package com.example.qr_code.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("orders")
public class Order {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("user_id")
    private Long userId;
    
    @TableField("seat_id")
    private Long seatId;
    
    /** 开始时间 */
    @TableField("start_time")
    private LocalDateTime startTime;
    
    /** 结束时间 */
    @TableField("end_time")
    private LocalDateTime endTime;
    
    /** 计划时长(分钟) */
    @TableField("planned_duration")
    private Integer plannedDuration;
    
    /** 实际时长(分钟) */
    @TableField("actual_duration")
    private Integer actualDuration;
    
    /** 订单状态：active-进行中, completed-已完成, cancelled-已取消, timeout-超时 */
    private String status;
    
    @TableField("created_at")
    private LocalDateTime createdAt;
    
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
