package com.example.qr_code.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * MessageBoard 留言板实体类
 * 对应数据库 message_board 表
 */
@Data
@TableName("message_board")
public class MessageBoard {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 发布者ID */
    @TableField("user_id")
    private Long userId;
    
    /** 留言内容 */
    private String content;
    
    /** 状态：1正常 0封禁 */
    private Integer status;
    
    /** 图片URL列表，多个用逗号分隔 */
    private String images;
    
    /** 回复数量 */
    @TableField("reply_count")
    private Integer replyCount;
    
    @TableField("created_at")
    private LocalDateTime createdAt;
    
    @TableField("updated_at")
    private LocalDateTime updatedAt;
    
    // 非数据库字段，用于展示
    @TableField(exist = false)
    private String nickname;
    
    @TableField(exist = false)
    private String avatar;
    
    @TableField(exist = false)
    private String todayStatus;
}
