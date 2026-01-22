package com.example.qr_code.controller;

import com.example.qr_code.entity.FocusRecord;
import com.example.qr_code.entity.LearningStats;
import com.example.qr_code.entity.User;
import com.example.qr_code.service.FocusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/focus")
public class FocusController {

    @Autowired
    private FocusService focusService;

    /**
     * 保存专注记录（支持幂等性）
     * POST /api/focus/save
     * Body: { "duration": 1800, "type": "free", "clientId": "xxx", "timestamp": 123456789 }
     */
    @PostMapping("/save")
    public Map<String, Object> saveFocusRecord(@RequestBody Map<String, Object> body, HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        // 获取当前登录用户
        User user = (User) session.getAttribute("user");
        if (user == null) {
            result.put("success", false);
            result.put("code", 401);
            result.put("message", "请先登录");
            return result;
        }

        try {
            // 安全地获取 duration，处理 Integer/Long/Number 类型
            Object durationObj = body.get("duration");
            Integer duration = null;
            if (durationObj instanceof Number) {
                duration = ((Number) durationObj).intValue();
            }
            
            if (duration == null || duration <= 0) {
                result.put("success", false);
                result.put("code", 400);
                result.put("message", "无效的专注时长");
                return result;
            }

            // 获取专注类型
            String type = (String) body.get("type");
            if (type == null || type.isEmpty()) {
                type = "free";
            }
            
            // 获取客户端唯一标识（用于幂等性校验）
            String clientId = (String) body.get("clientId");

            // 解析时间（前端可能不传，后端自动计算）
            LocalDateTime endTime = LocalDateTime.now();
            LocalDateTime startTime = endTime.minusSeconds(duration);

            // 保存记录（带幂等性校验）
            FocusRecord record = focusService.saveFocusRecord(user.getId(), duration, startTime, endTime, type, clientId);

            // 获取今日统计
            LearningStats todayStats = focusService.getTodayStats(user.getId());

            result.put("success", true);
            result.put("code", 0);
            result.put("message", "专注记录已保存");
            result.put("recordId", record.getId());
            result.put("expEarned", calculateExp(duration, type));
            result.put("timestamp", System.currentTimeMillis());
            
            if (todayStats != null) {
                result.put("todayTotal", todayStats.getTotalDuration());
                result.put("todayCount", todayStats.getFocusCount());
                result.put("tomatoCount", todayStats.getTomatoCount() != null ? todayStats.getTomatoCount() : 0);
            }

        } catch (Exception e) {
            result.put("success", false);
            result.put("code", 500);
            result.put("message", "保存失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 获取今日学习统计
     * GET /api/focus/today
     */
    @GetMapping("/today")
    public Map<String, Object> getTodayStats(HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        User user = (User) session.getAttribute("user");
        if (user == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }

        LearningStats stats = focusService.getTodayStats(user.getId());
        
        result.put("success", true);
        if (stats != null) {
            result.put("totalDuration", stats.getTotalDuration());
            result.put("focusCount", stats.getFocusCount());
            result.put("avgDuration", stats.getAvgDuration());
            result.put("maxDuration", stats.getMaxDuration());
            result.put("expEarned", stats.getExpEarned());
            result.put("tomatoCount", stats.getTomatoCount() != null ? stats.getTomatoCount() : 0);
        } else {
            result.put("totalDuration", 0);
            result.put("focusCount", 0);
            result.put("avgDuration", 0);
            result.put("maxDuration", 0);
            result.put("expEarned", 0);
            result.put("tomatoCount", 0);
        }

        return result;
    }

    /**
     * 获取今日学习详情（包含所有记录）
     * GET /api/focus/today/details
     */
    @GetMapping("/today/details")
    public Map<String, Object> getTodayDetails(HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        User user = (User) session.getAttribute("user");
        if (user == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }

        // 获取今日统计
        LearningStats stats = focusService.getTodayStats(user.getId());
        
        // 获取今日所有记录
        List<FocusRecord> records = focusService.getTodayRecords(user.getId());
        
        result.put("success", true);
        
        if (stats != null) {
            result.put("totalDuration", stats.getTotalDuration());
            result.put("focusCount", stats.getFocusCount());
            result.put("expEarned", stats.getExpEarned());
            result.put("tomatoCount", stats.getTomatoCount() != null ? stats.getTomatoCount() : 0);
        } else {
            result.put("totalDuration", 0);
            result.put("focusCount", 0);
            result.put("expEarned", 0);
            result.put("tomatoCount", 0);
        }
        
        // 转换记录为前端需要的格式
        List<Map<String, Object>> recordList = new ArrayList<>();
        for (FocusRecord record : records) {
            Map<String, Object> r = new HashMap<>();
            r.put("id", record.getId());
            r.put("duration", record.getDuration());
            r.put("type", record.getType() != null ? record.getType() : "free");
            r.put("startTime", record.getStartTime() != null ? record.getStartTime().toString() : null);
            r.put("endTime", record.getEndTime() != null ? record.getEndTime().toString() : null);
            r.put("exp", calculateExp(record.getDuration(), record.getType()));
            recordList.add(r);
        }
        result.put("records", recordList);

        return result;
    }

    private int calculateExp(int durationSeconds) {
        return calculateExp(durationSeconds, "free");
    }

    private int calculateExp(int durationSeconds, String type) {
        int minutes = durationSeconds / 60;
        int baseExp;
        if (minutes >= 30) {
            baseExp = (int) (minutes * 1.5);
        } else {
            baseExp = minutes;
        }
        
        // 番茄钟额外奖励20%
        if ("pomodoro".equals(type)) {
            baseExp = (int) (baseExp * 1.2);
        }
        
        return baseExp;
    }
}
