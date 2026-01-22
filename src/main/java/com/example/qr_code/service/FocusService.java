package com.example.qr_code.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.qr_code.entity.FocusRecord;
import com.example.qr_code.entity.LearningStats;
import com.example.qr_code.entity.UserPet;
import com.example.qr_code.mapper.FocusRecordMapper;
import com.example.qr_code.mapper.LearningStatsMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class FocusService {

    @Autowired
    private FocusRecordMapper focusRecordMapper;

    @Autowired
    private LearningStatsMapper learningStatsMapper;

    @Autowired
    @Lazy
    private PetService petService;

    /**
     * 保存专注记录并更新学习统计
     * @param userId 用户ID
     * @param duration 专注时长（秒）
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 保存的记录
     */
    @Transactional(rollbackFor = Exception.class)
    public FocusRecord saveFocusRecord(Long userId, Integer duration, LocalDateTime startTime, LocalDateTime endTime) {
        return saveFocusRecord(userId, duration, startTime, endTime, "free");
    }

    /**
     * 保存专注记录并更新学习统计（带类型和幂等性校验）
     * @param userId 用户ID
     * @param duration 专注时长（秒）
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param type 专注类型: free 或 pomodoro
     * @param clientId 客户端唯一标识（用于幂等性校验）
     * @return 保存的记录（如果是重复请求则返回已有记录）
     */
    @Transactional(rollbackFor = Exception.class)
    public FocusRecord saveFocusRecord(Long userId, Integer duration, LocalDateTime startTime, LocalDateTime endTime, String type, String clientId) {
        // 幂等性校验：检查是否已存在相同 clientId 的记录
        if (clientId != null && !clientId.isEmpty()) {
            FocusRecord existing = findByClientId(clientId);
            if (existing != null) {
                log.info("幂等性校验：发现重复请求，clientId={}, recordId={}", clientId, existing.getId());
                return existing; // 返回已有记录，不重复插入
            }
        }
        
        // 1. 保存专注记录
        FocusRecord record = new FocusRecord();
        record.setUserId(userId);
        record.setDuration(duration);
        record.setType(type != null ? type : "free");
        record.setFocusDate(LocalDate.now());
        record.setStartTime(startTime);
        record.setEndTime(endTime);
        record.setClientId(clientId);
        focusRecordMapper.insert(record);

        // 2. 计算经验值（带心情加成）
        int baseExp = calculateExp(duration);
        int finalExp = baseExp;
        
        // 番茄钟模式额外奖励
        if ("pomodoro".equals(type)) {
            finalExp = (int) (finalExp * 1.2); // 番茄钟20%额外奖励
        }
        
        // 获取宠物心情值并计算加成
        try {
            UserPet pet = petService.getPet(userId);
            if (pet != null) {
                finalExp = petService.calculateExpWithMoodBonus(finalExp, pet.getMood());
            }
        } catch (Exception e) {
            // 如果获取宠物失败，使用基础经验值
            log.warn("获取宠物心情失败: {}", e.getMessage());
        }

        // 3. 更新当日学习统计
        updateLearningStats(userId, duration, finalExp, "pomodoro".equals(type));

        // 4. 给宠物增加经验值
        petService.addExp(userId, finalExp);

        return record;
    }
    
    /**
     * 保存专注记录并更新学习统计（带类型）
     * @param userId 用户ID
     * @param duration 专注时长（秒）
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param type 专注类型: free 或 pomodoro
     * @return 保存的记录
     */
    @Transactional(rollbackFor = Exception.class)
    public FocusRecord saveFocusRecord(Long userId, Integer duration, LocalDateTime startTime, LocalDateTime endTime, String type) {
        return saveFocusRecord(userId, duration, startTime, endTime, type, null);
    }
    
    /**
     * 根据 clientId 查找记录（用于幂等性校验）
     */
    public FocusRecord findByClientId(String clientId) {
        if (clientId == null || clientId.isEmpty()) {
            return null;
        }
        QueryWrapper<FocusRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("client_id", clientId);
        return focusRecordMapper.selectOne(wrapper);
    }

    /**
     * 更新用户当日学习统计
     */
    private void updateLearningStats(Long userId, Integer duration, Integer expEarned, boolean isPomodoro) {
        LocalDate today = LocalDate.now();
        
        // 查找今日统计记录
        QueryWrapper<LearningStats> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).eq("stat_date", today);
        LearningStats stats = learningStatsMapper.selectOne(wrapper);

        if (stats == null) {
            // 新建今日统计
            stats = new LearningStats();
            stats.setUserId(userId);
            stats.setStatDate(today);
            stats.setTotalDuration(duration);
            stats.setFocusCount(1);
            stats.setAvgDuration(duration);
            stats.setMaxDuration(duration);
            stats.setExpEarned(expEarned);
            stats.setTomatoCount(isPomodoro ? 1 : 0);
            learningStatsMapper.insert(stats);
        } else {
            // 更新今日统计
            int newTotal = stats.getTotalDuration() + duration;
            int newCount = stats.getFocusCount() + 1;
            int newAvg = newTotal / newCount;
            int newMax = Math.max(stats.getMaxDuration(), duration);
            int newExp = stats.getExpEarned() + expEarned;
            int newTomato = (stats.getTomatoCount() != null ? stats.getTomatoCount() : 0) + (isPomodoro ? 1 : 0);

            stats.setTotalDuration(newTotal);
            stats.setFocusCount(newCount);
            stats.setAvgDuration(newAvg);
            stats.setMaxDuration(newMax);
            stats.setExpEarned(newExp);
            stats.setTomatoCount(newTomato);
            learningStatsMapper.updateById(stats);
        }
    }

    /**
     * 根据专注时长计算经验值
     * 规则：每分钟1点经验，超过30分钟额外奖励50%
     */
    private int calculateExp(int durationSeconds) {
        int minutes = durationSeconds / 60;
        if (minutes >= 30) {
            return (int) (minutes * 1.5);
        }
        return minutes;
    }

    /**
     * 获取用户今日学习统计
     */
    public LearningStats getTodayStats(Long userId) {
        QueryWrapper<LearningStats> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).eq("stat_date", LocalDate.now());
        return learningStatsMapper.selectOne(wrapper);
    }

    /**
     * 获取用户今日所有专注记录
     */
    public List<FocusRecord> getTodayRecords(Long userId) {
        QueryWrapper<FocusRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
               .eq("focus_date", LocalDate.now())
               .orderByDesc("start_time");
        return focusRecordMapper.selectList(wrapper);
    }
}
