package com.example.qr_code.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.qr_code.entity.FocusRecord;
import com.example.qr_code.entity.LearningStats;
import com.example.qr_code.mapper.FocusRecordMapper;
import com.example.qr_code.mapper.LearningStatsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatsService {

    @Autowired
    private LearningStatsMapper learningStatsMapper;

    @Autowired
    private FocusRecordMapper focusRecordMapper;

    /**
     * 获取用户周统计数据
     */
    public Map<String, Object> getWeeklyStats(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(6); // 最近7天

        QueryWrapper<LearningStats> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
               .ge("stat_date", weekStart)
               .le("stat_date", today)
               .orderByAsc("stat_date");

        List<LearningStats> statsList = learningStatsMapper.selectList(wrapper);

        // 构建每日数据（补全没有记录的日期）
        List<Map<String, Object>> dailyData = new ArrayList<>();
        int totalMinutes = 0;
        int totalCount = 0;

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", date.toString());
            dayData.put("dayOfWeek", getDayOfWeekName(date));

            // 查找该日期的统计
            Optional<LearningStats> stats = statsList.stream()
                    .filter(s -> s.getStatDate().equals(date))
                    .findFirst();

            if (stats.isPresent()) {
                int minutes = stats.get().getTotalDuration() / 60;
                dayData.put("minutes", minutes);
                dayData.put("count", stats.get().getFocusCount());
                totalMinutes += minutes;
                totalCount += stats.get().getFocusCount();
            } else {
                dayData.put("minutes", 0);
                dayData.put("count", 0);
            }

            dailyData.add(dayData);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("dailyData", dailyData);
        result.put("totalMinutes", totalMinutes);
        result.put("totalCount", totalCount);
        result.put("avgMinutes", totalCount > 0 ? totalMinutes / Math.max(1, countDaysWithData(statsList)) : 0);

        return result;
    }

    /**
     * 获取用户月统计数据
     */
    public Map<String, Object> getMonthlyStats(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.minusDays(29); // 最近30天

        QueryWrapper<LearningStats> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
               .ge("stat_date", monthStart)
               .le("stat_date", today)
               .orderByAsc("stat_date");

        List<LearningStats> statsList = learningStatsMapper.selectList(wrapper);

        // 构建每日数据
        List<Map<String, Object>> dailyData = new ArrayList<>();
        int totalMinutes = 0;
        int totalCount = 0;
        int maxMinutes = 0;
        int streakDays = 0;
        int currentStreak = 0;

        for (int i = 29; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", date.toString());

            Optional<LearningStats> stats = statsList.stream()
                    .filter(s -> s.getStatDate().equals(date))
                    .findFirst();

            if (stats.isPresent()) {
                int minutes = stats.get().getTotalDuration() / 60;
                dayData.put("minutes", minutes);
                totalMinutes += minutes;
                totalCount += stats.get().getFocusCount();
                maxMinutes = Math.max(maxMinutes, minutes);
                currentStreak++;
                streakDays = Math.max(streakDays, currentStreak);
            } else {
                dayData.put("minutes", 0);
                currentStreak = 0;
            }

            dailyData.add(dayData);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("dailyData", dailyData);
        result.put("totalMinutes", totalMinutes);
        result.put("totalCount", totalCount);
        result.put("avgMinutes", totalCount > 0 ? totalMinutes / Math.max(1, countDaysWithData(statsList)) : 0);
        result.put("maxMinutes", maxMinutes);
        result.put("streakDays", streakDays);

        return result;
    }

    /**
     * 获取用户学习时段分布（24小时）
     */
    public Map<String, Object> getHourlyDistribution(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(6);

        // 查询最近一周的专注记录
        QueryWrapper<FocusRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
               .ge("focus_date", weekStart)
               .le("focus_date", today);

        List<FocusRecord> records = focusRecordMapper.selectList(wrapper);

        // 统计每小时的学习时长
        int[] hourlyMinutes = new int[24];
        
        for (FocusRecord record : records) {
            if (record.getStartTime() != null) {
                int hour = record.getStartTime().getHour();
                hourlyMinutes[hour] += record.getDuration() / 60;
            }
        }

        // 构建返回数据
        List<Map<String, Object>> hourlyData = new ArrayList<>();
        int peakHour = 0;
        int peakMinutes = 0;

        for (int i = 0; i < 24; i++) {
            Map<String, Object> hourData = new HashMap<>();
            hourData.put("hour", i);
            hourData.put("label", String.format("%02d:00", i));
            hourData.put("minutes", hourlyMinutes[i]);
            hourlyData.add(hourData);

            if (hourlyMinutes[i] > peakMinutes) {
                peakMinutes = hourlyMinutes[i];
                peakHour = i;
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("hourlyData", hourlyData);
        result.put("peakHour", peakHour);
        result.put("peakLabel", String.format("%02d:00-%02d:00", peakHour, (peakHour + 1) % 24));

        return result;
    }

    /**
     * 获取用户学习画像
     */
    public Map<String, Object> getLearningProfile(Long userId) {
        Map<String, Object> weeklyStats = getWeeklyStats(userId);
        Map<String, Object> hourlyStats = getHourlyDistribution(userId);

        List<String> tags = new ArrayList<>();
        String summary = "";

        int totalMinutes = (int) weeklyStats.get("totalMinutes");
        int avgMinutes = (int) weeklyStats.get("avgMinutes");
        int peakHour = (int) hourlyStats.get("peakHour");

        // 根据学习时长生成标签
        if (totalMinutes >= 600) { // 10小时+
            tags.add("学霸");
            tags.add("高效学习者");
        } else if (totalMinutes >= 300) { // 5小时+
            tags.add("勤奋学习者");
        } else if (totalMinutes >= 60) {
            tags.add("稳步前进");
        } else {
            tags.add("初来乍到");
        }

        // 根据学习时段生成标签
        if (peakHour >= 5 && peakHour < 9) {
            tags.add("早起鸟");
            summary = "你是一个喜欢早起学习的人，清晨的时光效率最高！";
        } else if (peakHour >= 9 && peakHour < 12) {
            tags.add("上午型选手");
            summary = "上午是你的黄金学习时间，思维最为活跃。";
        } else if (peakHour >= 14 && peakHour < 18) {
            tags.add("下午战士");
            summary = "下午是你专注力最强的时段，继续保持！";
        } else if (peakHour >= 19 && peakHour < 23) {
            tags.add("夜猫子");
            summary = "夜晚是你的学习主场，安静的环境让你更专注。";
        } else if (peakHour >= 23 || peakHour < 5) {
            tags.add("深夜修行者");
            summary = "深夜学习虽然安静，但也要注意休息哦！";
        }

        // 根据平均时长生成建议
        if (avgMinutes >= 120) {
            tags.add("持久力强");
        } else if (avgMinutes >= 60) {
            tags.add("专注力佳");
        }

        if (summary.isEmpty()) {
            summary = "继续保持学习习惯，你会变得更优秀！";
        }

        Map<String, Object> result = new HashMap<>();
        result.put("tags", tags);
        result.put("summary", summary);
        result.put("weeklyMinutes", totalMinutes);
        result.put("avgDailyMinutes", avgMinutes);
        result.put("peakHour", peakHour);

        return result;
    }

    /**
     * 获取星期几的中文名称
     */
    private String getDayOfWeekName(LocalDate date) {
        String[] days = {"周日", "周一", "周二", "周三", "周四", "周五", "周六"};
        return days[date.getDayOfWeek().getValue() % 7];
    }

    /**
     * 统计有数据的天数
     */
    private int countDaysWithData(List<LearningStats> statsList) {
        return (int) statsList.stream()
                .filter(s -> s.getTotalDuration() > 0)
                .count();
    }
}
