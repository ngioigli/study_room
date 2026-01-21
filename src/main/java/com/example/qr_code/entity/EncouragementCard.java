package com.example.qr_code.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * EncouragementCard 匿名鼓励卡片实体
 */
@Data
@TableName("encouragement_card")
public class EncouragementCard {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 发送者用户ID（匿名展示，但后台可追溯）
     */
    private Long userId;
    
    /**
     * 表情符号
     */
    private String emoji;
    
    /**
     * 鼓励内容
     */
    private String message;
    
    /**
     * 点赞数
     */
    private Integer likes;
    
    /**
     * 是否隐藏（管理员封禁）
     */
    private Boolean isHidden;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
