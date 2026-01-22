package com.example.qr_code.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.qr_code.entity.LearningStats;
import com.example.qr_code.entity.User;
import com.example.qr_code.mapper.LearningStatsMapper;
import com.example.qr_code.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * UserService 用户服务类
 * 处理用户资料、统计等业务逻辑
 */
@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private LearningStatsMapper learningStatsMapper;

    /**
     * 根据ID获取用户
     */
    public User getUserById(Long userId) {
        return userMapper.selectById(userId);
    }

    /**
     * 获取用户学习统计
     */
    public Map<String, Object> getUserStats(Long userId) {
        Map<String, Object> stats = new HashMap<>();
        
        // 获取总学习时长
        QueryWrapper<LearningStats> totalQuery = new QueryWrapper<>();
        totalQuery.eq("user_id", userId);
        List<LearningStats> allStats = learningStatsMapper.selectList(totalQuery);
        
        int totalMinutes = 0;
        int totalSessions = 0;
        int studyDays = 0;
        
        for (LearningStats s : allStats) {
            totalMinutes += s.getTotalDuration() != null ? s.getTotalDuration() : 0;
            totalSessions += s.getFocusCount() != null ? s.getFocusCount() : 0;
            if (s.getTotalDuration() != null && s.getTotalDuration() > 0) {
                studyDays++;
            }
        }
        
        // 计算连续学习天数
        int streakDays = calculateStreakDays(userId);
        
        // 更新用户的学习天数
        updateStudyDays(userId, studyDays);
        
        stats.put("totalMinutes", totalMinutes);
        stats.put("totalHours", totalMinutes / 60);
        stats.put("totalSessions", totalSessions);
        stats.put("studyDays", studyDays);
        stats.put("streakDays", streakDays);
        
        return stats;
    }

    /**
     * 计算连续学习天数
     */
    private int calculateStreakDays(Long userId) {
        QueryWrapper<LearningStats> query = new QueryWrapper<>();
        query.eq("user_id", userId)
             .orderByDesc("stat_date")
             .last("LIMIT 30");  // 最多查30天
        List<LearningStats> stats = learningStatsMapper.selectList(query);
        
        int streak = 0;
        LocalDate expectedDate = LocalDate.now();
        
        for (LearningStats s : stats) {
            if (s.getStatDate() == null) continue;
            
            // 如果当天还没有学习记录，从昨天开始算
            if (streak == 0 && !s.getStatDate().equals(expectedDate)) {
                expectedDate = LocalDate.now().minusDays(1);
            }
            
            if (s.getStatDate().equals(expectedDate) && s.getTotalDuration() != null && s.getTotalDuration() > 0) {
                streak++;
                expectedDate = expectedDate.minusDays(1);
            } else if (!s.getStatDate().equals(expectedDate)) {
                break;
            }
        }
        
        return streak;
    }

    /**
     * 更新用户昵称
     */
    public boolean updateNickname(Long userId, String nickname) {
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", userId).set("nickname", nickname);
        return userMapper.update(null, updateWrapper) > 0;
    }

    /**
     * 更新用户头像
     */
    public boolean updateAvatar(Long userId, String avatar) {
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", userId).set("avatar", avatar);
        return userMapper.update(null, updateWrapper) > 0;
    }

    /**
     * 更新个性签名
     */
    public boolean updateSignature(Long userId, String signature) {
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", userId).set("signature", signature);
        return userMapper.update(null, updateWrapper) > 0;
    }

    /**
     * 更新今日状态
     */
    public boolean updateTodayStatus(Long userId, String todayStatus) {
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", userId).set("today_status", todayStatus);
        return userMapper.update(null, updateWrapper) > 0;
    }

    /**
     * 更新学习天数
     */
    public boolean updateStudyDays(Long userId, int studyDays) {
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", userId).set("study_days", studyDays);
        return userMapper.update(null, updateWrapper) > 0;
    }

    /**
     * 批量更新用户资料
     */
    public boolean updateProfile(Long userId, String nickname, String avatar, String signature, String todayStatus) {
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", userId);
        
        if (nickname != null && !nickname.trim().isEmpty()) {
            updateWrapper.set("nickname", nickname.trim());
        }
        if (avatar != null && !avatar.trim().isEmpty()) {
            updateWrapper.set("avatar", avatar.trim());
        }
        if (signature != null) {
            updateWrapper.set("signature", signature.trim());
        }
        if (todayStatus != null && !todayStatus.trim().isEmpty()) {
            updateWrapper.set("today_status", todayStatus.trim());
        }
        
        return userMapper.update(null, updateWrapper) > 0;
    }
    
    /**
     * 更新排行榜隐私设置
     * @param userId 用户ID
     * @param hideRanking true-隐藏排名，false-显示排名
     */
    public boolean updateHideRanking(Long userId, boolean hideRanking) {
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", userId).set("hide_ranking", hideRanking ? 1 : 0);
        return userMapper.update(null, updateWrapper) > 0;
    }
}
