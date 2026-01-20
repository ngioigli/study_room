package com.example.qr_code.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("access_logs")
public class AccessLog {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("user_id")
    private Long userId;
    
    /** 操作类型：lock-锁定, unlock-解锁, enter-进入, exit-离开 */
    @TableField("action_type")
    private String actionType;
    
    /** 锁定开始时间 */
    @TableField("lock_start_time")
    private LocalDateTime lockStartTime;
    
    /** 锁定结束时间 */
    @TableField("lock_end_time")
    private LocalDateTime lockEndTime;
    
    /** IP地址 */
    @TableField("ip_address")
    private String ipAddress;
    
    /** 用户代理 */
    @TableField("user_agent")
    private String userAgent;
    
    /** 操作结果：success-成功, fail-失败, timeout-超时 */
    private String result;
    
    /** 备注 */
    private String remark;
    
    @TableField("created_at")
    private LocalDateTime createdAt;
}
