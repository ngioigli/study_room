package com.example.qr_code.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("seats")
public class Seat {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 座位编号，如 A01, B02 */
    @TableField("seat_number")
    private String seatNumber;
    
    /** 座位状态：available-空闲, occupied-占用, reserved-预约, maintenance-维护 */
    private String status;
    
    /** 电源状态：0-断电，1-通电 */
    @TableField("power_on")
    private Integer powerOn;
    
    /** 座位描述，如靠窗、角落 */
    private String description;
    
    @TableField("created_at")
    private LocalDateTime createdAt;
    
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
