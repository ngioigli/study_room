package com.example.qr_code.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * SystemConfig 系统配置实体类
 * 对应数据库 system_config 表
 */
@Data
@TableName("system_config")
public class SystemConfig {
    
    /** 配置键 */
    @TableId
    @TableField("config_key")
    private String configKey;
    
    /** 配置值 */
    @TableField("config_value")
    private String configValue;
    
    /** 配置说明 */
    private String description;
    
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
