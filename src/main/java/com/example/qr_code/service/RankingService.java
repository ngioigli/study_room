package com.example.qr_code.service;

import com.example.qr_code.mapper.LearningStatsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;

/**
 * RankingService 排行榜服务类
 * 处理各维度学习时长排行
 */
@Service
public class RankingService {

    @Autowired
    private LearningStatsMapper learningStatsMapper;

    /**
     * 获取今日排行榜
     */
    public List<Map<String, Object>> getTodayRanking(int limit) {
        LocalDate today = LocalDate.now();
        return learningStatsMapper.getRankingByDateRange(today, today, limit);
    }

    /**
     * 获取本周排行榜
     */
    public List<Map<String, Object>> getWeeklyRanking(int limit) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        return learningStatsMapper.getRankingByDateRange(weekStart, today, limit);
    }

    /**
     * 获取本月排行榜
     */
    public List<Map<String, Object>> getMonthlyRanking(int limit) {
        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);
        return learningStatsMapper.getRankingByDateRange(monthStart, today, limit);
    }

    /**
     * 获取本年排行榜
     */
    public List<Map<String, Object>> getYearlyRanking(int limit) {
        LocalDate today = LocalDate.now();
        LocalDate yearStart = today.withDayOfYear(1);
        return learningStatsMapper.getRankingByDateRange(yearStart, today, limit);
    }

    /**
     * 获取总排行榜
     */
    public List<Map<String, Object>> getTotalRanking(int limit) {
        return learningStatsMapper.getTotalRanking(limit);
    }

    /**
     * 获取用户在各维度的排名
     */
    public Map<String, Object> getUserRankings(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate yearStart = today.withDayOfYear(1);
        
        Integer todayRank = learningStatsMapper.getUserRankByDateRange(userId, today, today);
        Integer weekRank = learningStatsMapper.getUserRankByDateRange(userId, weekStart, today);
        Integer monthRank = learningStatsMapper.getUserRankByDateRange(userId, monthStart, today);
        Integer yearRank = learningStatsMapper.getUserRankByDateRange(userId, yearStart, today);
        Integer totalRank = learningStatsMapper.getUserTotalRank(userId);
        
        return Map.of(
            "today", todayRank != null ? todayRank : 0,
            "week", weekRank != null ? weekRank : 0,
            "month", monthRank != null ? monthRank : 0,
            "year", yearRank != null ? yearRank : 0,
            "total", totalRank != null ? totalRank : 0
        );
    }
}
