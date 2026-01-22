package com.example.qr_code.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * User 用户实体类
 * 对应数据库 users 表
 */
@Data
@TableName("users")
public class User {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String username;
    private String password;
    private String nickname;
    private String avatar;
    private String phone;
    private String email;
    
    /** 个性签名 */
    private String signature;
    
    /** 今日状态 */
    @TableField("today_status")
    private String todayStatus;
    
    /** 累计学习天数 */
    @TableField("study_days")
    private Integer studyDays;
    
    /** 角色：user-普通用户，admin-管理员 */
    private String role;
    
    /** 状态：1-正常，0-禁用 */
    private Integer status;
    
    /** 隐藏排名：0-显示，1-隐藏 */
    @TableField("hide_ranking")
    private Integer hideRanking;
    
    @TableField("created_at")
    private LocalDateTime createdAt;
    
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}