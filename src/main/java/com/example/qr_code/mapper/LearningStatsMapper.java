package com.example.qr_code.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.qr_code.entity.LearningStats;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * LearningStatsMapper 学习统计数据访问接口
 */
@Repository
public interface LearningStatsMapper extends BaseMapper<LearningStats> {
    
    /**
     * 按日期范围获取排行榜
     */
    @Select("SELECT u.id as userId, u.nickname, u.avatar, u.today_status as todayStatus, " +
            "SUM(ls.total_duration) as totalMinutes " +
            "FROM learning_stats ls " +
            "JOIN users u ON ls.user_id = u.id " +
            "WHERE ls.stat_date BETWEEN #{startDate} AND #{endDate} " +
            "AND u.status = 1 " +
            "GROUP BY u.id, u.nickname, u.avatar, u.today_status " +
            "ORDER BY totalMinutes DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> getRankingByDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("limit") int limit);
    
    /**
     * 获取总排行榜
     */
    @Select("SELECT u.id as userId, u.nickname, u.avatar, u.today_status as todayStatus, " +
            "SUM(ls.total_duration) as totalMinutes " +
            "FROM learning_stats ls " +
            "JOIN users u ON ls.user_id = u.id " +
            "WHERE u.status = 1 " +
            "GROUP BY u.id, u.nickname, u.avatar, u.today_status " +
            "ORDER BY totalMinutes DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> getTotalRanking(@Param("limit") int limit);
    
    /**
     * 获取用户在指定日期范围内的排名
     */
    @Select("SELECT COUNT(*) + 1 FROM (" +
            "SELECT user_id, SUM(total_duration) as total " +
            "FROM learning_stats " +
            "WHERE stat_date BETWEEN #{startDate} AND #{endDate} " +
            "GROUP BY user_id " +
            "HAVING total > (SELECT COALESCE(SUM(total_duration), 0) FROM learning_stats " +
            "WHERE user_id = #{userId} AND stat_date BETWEEN #{startDate} AND #{endDate})" +
            ") t")
    Integer getUserRankByDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
    
    /**
     * 获取用户总排名
     */
    @Select("SELECT COUNT(*) + 1 FROM (" +
            "SELECT user_id, SUM(total_duration) as total " +
            "FROM learning_stats " +
            "GROUP BY user_id " +
            "HAVING total > (SELECT COALESCE(SUM(total_duration), 0) FROM learning_stats " +
            "WHERE user_id = #{userId})" +
            ") t")
    Integer getUserTotalRank(@Param("userId") Long userId);
}
