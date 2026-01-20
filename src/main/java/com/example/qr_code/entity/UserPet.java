package com.example.qr_code.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user_pets")
public class UserPet {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("user_id")
    private Long userId;
    
    /** 宠物名称 */
    @TableField("pet_name")
    private String petName;
    
    /** 宠物类型：cat, dog, rabbit */
    @TableField("pet_type")
    private String petType;
    
    /** 经验值 */
    private Integer exp;
    
    /** 等级 */
    private Integer level;
    
    /** 进化阶段：egg-蛋, baby-幼年, teen-少年, adult-成年, professional-职业 */
    private String stage;
    
    /** 心情值(0-100) */
    private Integer mood;
    
    /** 上次互动时间 */
    @TableField("last_interact_time")
    private LocalDateTime lastInteractTime;
    
    @TableField("created_at")
    private LocalDateTime createdAt;
    
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
