package com.example.qr_code.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("learning_stats")
public class LearningStats {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("user_id")
    private Long userId;
    
    /** 统计日期 */
    @TableField("stat_date")
    private LocalDate statDate;
    
    /** 当日总时长(秒) */
    @TableField("total_duration")
    private Integer totalDuration;
    
    /** 专注次数 */
    @TableField("focus_count")
    private Integer focusCount;
    
    /** 平均时长(秒) */
    @TableField("avg_duration")
    private Integer avgDuration;
    
    /** 最长单次时长(秒) */
    @TableField("max_duration")
    private Integer maxDuration;
    
    /** 当日获得经验值 */
    @TableField("exp_earned")
    private Integer expEarned;
    
    /** 当日番茄钟完成数 */
    @TableField("tomato_count")
    private Integer tomatoCount;
    
    @TableField("created_at")
    private LocalDateTime createdAt;
    
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
