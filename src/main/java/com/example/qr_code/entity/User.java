package com.example.qr_code.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import java.time.LocalDateTime;

@Data // Lombok注解，自动生成Getter, Setter, ToString
@TableName("users") // 告诉MP，这个类对应数据库里的 users 表
public class User {
    
    @TableId(type = IdType.AUTO) // 主键自增
    private Long id;
    
    private String username;
    private String password;
    private String nickname;
    private String avatar;
    private String phone;
    private String email;
    
    /** 角色：user-普通用户，admin-管理员 */
    private String role;
    
    /** 状态：1-正常，0-禁用 */
    private Integer status;
    
    @TableField("created_at")
    private LocalDateTime createdAt;
    
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}