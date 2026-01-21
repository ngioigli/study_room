package com.example.qr_code.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * MessageReply 留言回复实体类
 * 对应数据库 message_reply 表
 */
@Data
@TableName("message_replies")
public class MessageReply {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 留言ID */
    @TableField("message_id")
    private Long messageId;
    
    /** 回复者ID */
    @TableField("user_id")
    private Long userId;
    
    /** 回复内容 */
    private String content;
    
    /** 状态：1正常 0封禁 */
    private Integer status;
    
    @TableField("created_at")
    private LocalDateTime createdAt;
    
    // 非数据库字段，用于展示
    @TableField(exist = false)
    private String nickname;
    
    @TableField(exist = false)
    private String avatar;
}
