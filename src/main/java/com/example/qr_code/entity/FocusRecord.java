package com.example.qr_code.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("focus_records")
public class FocusRecord {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("user_id")
    private Long userId;
    
    /** 关联订单ID（可选） */
    @TableField("order_id")
    private Long orderId;
    
    /** 专注时长(秒) */
    private Integer duration;
    
    /** 专注类型: free(自由专注), pomodoro(番茄钟) */
    private String type;
    
    /** 专注日期 */
    @TableField("focus_date")
    private LocalDate focusDate;
    
    /** 开始时间 */
    @TableField("start_time")
    private LocalDateTime startTime;
    
    /** 结束时间 */
    @TableField("end_time")
    private LocalDateTime endTime;
    
    /** 客户端唯一标识（用于幂等性校验） */
    @TableField("client_id")
    private String clientId;
    
    @TableField("created_at")
    private LocalDateTime createdAt;
}
